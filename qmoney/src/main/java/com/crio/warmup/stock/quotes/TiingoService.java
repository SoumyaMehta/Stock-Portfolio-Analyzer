
package com.crio.warmup.stock.quotes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  public static final String TOKEN = "a8b6cfa733d51e64776dc5a5b25b05e956c791f8";
  protected RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuotesServiceException {

    List<Candle> startToEndDate;

    if (from.compareTo(to) >= 0) {
      throw new RuntimeException();
    }

    String url = buildUri(symbol, from, to);
    try {
      String stocks = restTemplate.getForObject(url, String.class);
      ObjectMapper objectMapper = getObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());

      TiingoCandle[] stockStartToEndDateArray = objectMapper.readValue(stocks, TiingoCandle[].class);

      if (stockStartToEndDateArray != null) {
        startToEndDate = Arrays.asList(stockStartToEndDateArray);
      } else {
        startToEndDate = Arrays.asList(new TiingoCandle[0]);
      }
    } catch (NullPointerException e) {

      throw new StockQuotesServiceException("Error occured when requesting response from Tiingo API", e.getCause());

    }
    return startToEndDate;
  }

  private String buildUri(String symbol, LocalDate from, LocalDate to) {
    String urlTemplate = String.format(
        "https://api.tiingo.com/tiingo/daily/%s/prices?" + "startDate=%s&endDate=%s&token=%s", symbol, from, to, TOKEN);
    return urlTemplate;
  }
}
