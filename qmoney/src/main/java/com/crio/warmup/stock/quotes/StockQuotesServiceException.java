package com.crio.warmup.stock.quotes;

public class StockQuotesServiceException extends RuntimeException {
    public StockQuotesServiceException(String message) {
    }

    public StockQuotesServiceException(String string, NullPointerException e) {
    }

    public StockQuotesServiceException(String string, Throwable cause) {
    }
}