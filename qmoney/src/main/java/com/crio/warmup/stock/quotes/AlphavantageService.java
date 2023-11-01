
package com.crio.warmup.stock.quotes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import org.springframework.web.client.RestTemplate;

/*my code end */
public class AlphavantageService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  class StartDateComparator implements Comparator<AlphavantageCandle> {
    @Override
    public int compare(AlphavantageCandle t1, AlphavantageCandle t2) {
      if (t1.getDate().isBefore(t2.getDate()))
        return -1;
      return 1;
    }
  }

  public static String getToken() {

    return "30XCSA8SNZSRZ3LG";

  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    // https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=IBM&outputsize=full&apikey=<your_API_key>
    String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + symbol
        + "&outputsize=full&apikey=" + getToken();
    return url;

  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws StockQuoteServiceException {

    List<Candle> ans = new ArrayList<>();

    try {

      String url = buildUri(symbol, from, to);
      // AlphavantageCandle alphavantageCandle[] = restTemplate
      // .getForObject(url , AlphavantageDailyResponse.class).getCandles();
      // return Arrays.asList(alphavantageCandle);

      Map<LocalDate, AlphavantageCandle> candles = restTemplate
          .getForObject(url, AlphavantageDailyResponse.class).getCandles();

      List<AlphavantageCandle> alphavantageCandleslist = new ArrayList<>();
      for (Map.Entry<LocalDate, AlphavantageCandle> entry : candles.entrySet()) {
        // System.out.println("Key = " + entry.getKey() +
        // ", Value = " + entry.getValue());
        // check the date is between start date and end date or not
        if (entry.getKey().isAfter(from.minusDays(1)) && entry.getKey().isBefore(to.plusDays(1))) {

          AlphavantageCandle t = new AlphavantageCandle();
          t.setOpen(entry.getValue().getOpen());
          t.setClose(entry.getValue().getClose());
          t.setHigh(entry.getValue().getHigh());
          t.setLow(entry.getValue().getLow());
          t.setDate(entry.getKey());
          alphavantageCandleslist.add(t);

        }
      }

      // return
      // tiingoCandleslist.stream().sorted(Comparator.comparing(Candle::getDate)).collect(Collectors.toList());
      Collections.sort(alphavantageCandleslist, new StartDateComparator());

      for (AlphavantageCandle val : alphavantageCandleslist) {
        ans.add(val);
      }

    } catch (Exception e) {
      // System.out.println(e.getMessage());
      throw new StockQuoteServiceException(e.getMessage());
    }

    // return List.of(tiingoCandleslist);
    return ans;

  }

}
