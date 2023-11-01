
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  protected RestTemplate restTemplate;
  protected StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  @Override

  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws StockQuoteServiceException {
    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (int i = 0; i < portfolioTrades.size(); i++) {
      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i), endDate);
      annualizedReturns.add(annualizedReturn);
    }

    Comparator<AnnualizedReturn> SortByAnnResult = getComparator();
    Collections.sort(annualizedReturns, SortByAnnResult);

    return annualizedReturns;
  }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate)
      throws StockQuoteServiceException {

    // AnnualizedReturn annualizedReturn;
    // String symbol = trade.getSymbol();
    // LocalDate startLocalDate = trade.getPurchaseDate();

    // try{
    // List<Candle> stockStartToEnd = getStockQuote(symbol, startLocalDate,
    // endLocalDate);
    // Candle stockStartDate = stockStartToEnd.get(0);
    // Candle stockEndDate = stockStartToEnd.get(stockStartToEnd.size() -1);

    // Double buyPrice = stockStartDate.getOpen();
    // Double sellPrice = stockEndDate.getClose();

    // Double totalReturn = (sellPrice - buyPrice) / buyPrice;
    // Double numYear = (double) ChronoUnit.DAYS.between(startLocalDate,
    // endLocalDate) / 365.24;
    // Double annulizedResult = Math.pow((1 + totalReturn), (1 / numYear)) -1;

    // annualizedReturn = new AnnualizedReturn(symbol, annulizedResult,
    // totalReturn);

    // }catch (JsonProcessingException e){
    // annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    // }

    // return annualizedReturn;
    LocalDate startDate = trade.getPurchaseDate();
    String symbol = trade.getSymbol();
    Double buyPrice = 0.0, sellPrice = 0.0;

    try {
      LocalDate localStartDate = trade.getPurchaseDate();
      List<Candle> stockStartToEndFull = new ArrayList<>();
      stockStartToEndFull = getStockQuote(symbol, localStartDate, endDate);
      Collections.sort(stockStartToEndFull, Comparator.comparing(Candle::getDate));

      Candle stockStartDate = stockStartToEndFull.get(0);
      Candle stockLatest = stockStartToEndFull.get(stockStartToEndFull.size() - 1);

      buyPrice = stockStartDate.getOpen();
      sellPrice = stockLatest.getClose();
      endDate = stockLatest.getDate();
    } catch (JsonProcessingException e) {
      throw new RuntimeException();
    }

    Double totalReturn = (sellPrice - buyPrice) / buyPrice;

    long daysButweenPurchaseAndSelling = ChronoUnit.DAYS.between(startDate, endDate);
    Double totalYear = (double) (daysButweenPurchaseAndSelling) / 365.24;
    Double annualizedReturn = Math.pow((1 + totalReturn), (1 / totalYear)) - 1;
    return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    return stockQuotesService.getStockQuote(symbol, from, to);

  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token = "a8b6cfa733d51e64776dc5a5b25b05e956c791f8";

    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
        .replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());
    return url;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException, RuntimeException {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
    final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < portfolioTrades.size(); i++) {
      PortfolioTrade trade = portfolioTrades.get(i);
      Callable<AnnualizedReturn> callableTask = () -> {
        return getAnnualizedReturn(trade, endDate);
      };
      Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
      futureReturnsList.add(futureReturns);
    }

    for (int i = 0; i < portfolioTrades.size(); i++) {
      Future<AnnualizedReturn> futureRetuns = futureReturnsList.get(i);
      try {
        AnnualizedReturn returns = futureRetuns.get();
        annualizedReturns.add(returns);
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException("Error when calling the API", e);
      }
    }

    Collections.sort(annualizedReturns, getComparator());

    return annualizedReturns;
  }

}
