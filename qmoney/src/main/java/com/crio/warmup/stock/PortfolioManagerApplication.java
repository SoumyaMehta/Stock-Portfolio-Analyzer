
package com.crio.warmup.stock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  public static String getToken() {
    return "7f53b4622efccb15af2f52a01831b9c7b72f4bd4";
  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    if (file == null) {
      return new ArrayList<>();
    }
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<String>();
    for (PortfolioTrade t : trades) {
      symbols.add(t.getSymbol());
    }
    return symbols;
  }

  private static void printJsonObject(Object object) throws IOException {
    // Logger logger =
    // Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    // logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/soumyamehta2512-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@1573f9fc";
    String functionNameFromTestFileInStackTrace = "mainReadFile(new String[]{filename});";
    String lineNumberFromTestFileInStackTrace = "29";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace });

  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> finalResult = new ArrayList<TotalReturnsDto>();
    for (PortfolioTrade t : trades) {

      String url = prepareUrl(t, endDate, getToken());
      TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);
      if (result != null) {
        finalResult.add(new TotalReturnsDto(t.getSymbol(), result[result.length - 1].getClose()));
      }
    }
    List<TotalReturnsDto> sortedByClose = finalResult;
    Collections.sort(sortedByClose, TotalReturnsDto.closeComparator);
    List<String> stocks = new ArrayList<String>();

    for (TotalReturnsDto t1 : sortedByClose) {
      // Print all elements of List
      // TiingoCandle response = restTemplate.getForObject(url, TiingoCandle.class);
      stocks.add(t1.getSymbol());

    }

    return stocks;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> result = new ArrayList<PortfolioTrade>();

    for (PortfolioTrade t : trades) {
      result.add(t);
    }

    return result;
  }

  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {

    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
        + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;

    return url;
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Double open = candles.get(0).getOpen();
    return open;
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Double close = candles.get(candles.size() - 1).getClose();
    return close;
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String url = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s",
        trade.getSymbol(), trade.getPurchaseDate().toString(), endDate.toString(), getToken());
    List<Candle> tiingoCandle = new ArrayList<>();
    TiingoCandle[] result = restTemplate.getForObject(url, TiingoCandle[].class);

    for (TiingoCandle t : result) {
      tiingoCandle.add(t);
    }

    return tiingoCandle;
  }

  public static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {
    String ticker = trade.getSymbol();
    LocalDate startDate = trade.getPurchaseDate();

    if (startDate.compareTo(endDate) >= 0) {
      throw new RuntimeException();
    }

    String url = prepareUrl(trade, endDate, getToken());

    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] candle = restTemplate.getForObject(url, TiingoCandle[].class);

    if (candle != null) {
      TiingoCandle stockStartDate = candle[0];
      TiingoCandle stockEndDate = candle[candle.length - 1];

      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stockEndDate.getClose();

      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      return annualizedReturn;

    } else {
      return new AnnualizedReturn(ticker, Double.NaN, Double.NaN);
    }
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException, DateTimeParseException {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    LocalDate endDate = LocalDate.parse(args[1]);

    File trades = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();

    PortfolioTrade[] reaadJson = objectMapper.readValue(trades, PortfolioTrade[].class);

    for (int i = 0; i < reaadJson.length; i++) {
      annualizedReturns.add(getAnnualizedReturn(reaadJson[i], endDate));
    }

    Comparator<AnnualizedReturn> SortByAnnResult = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn)
        .reversed();
    Collections.sort(annualizedReturns, SortByAnnResult);

    return annualizedReturns;

  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    Double absReturn = (sellPrice - buyPrice) / buyPrice;
    String symbol = trade.getSymbol();
    LocalDate purchaseDate = trade.getPurchaseDate();
    Double numYears = (double) (ChronoUnit.DAYS.between(purchaseDate, endDate) / 365.24);
    Double annulizedResult = Math.pow((1 + absReturn), (1 / numYears)) - 1;

    return new AnnualizedReturn(symbol, annulizedResult, absReturn);
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    File contents = resolveFileFromResources(file);
    ;
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));

    printJsonObject(mainReadQuotes(args));

    printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }

}
