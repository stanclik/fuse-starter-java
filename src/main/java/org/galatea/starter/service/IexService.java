package org.galatea.starter.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;

  private final String key = setKey();

  private String setKey() {
    try {
      File keyFile = ResourceUtils.getFile("classpath:keys.csv");
      Scanner sc = new Scanner(keyFile);
      try {
        return sc.nextLine().split(",")[1];   // Assumes the IEX key is on
      } catch(ArrayIndexOutOfBoundsException ex) {  // the first line of keys.csv
        log.warn(ex.getMessage());
        return null;
      }
    } catch(FileNotFoundException ex) {
      log.warn(ex.getMessage());
      return null;
    }
  }

  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols() {
    return iexClient.getAllSymbols(key);
  }

  /**
   * Get the last traded price for each Symbol that is passed in.
   *
   * @param symbols the list of symbols to get a last traded price for.
   * @return a list of last traded price objects for each Symbol that is passed in.
   */
  public List<IexLastTradedPrice> getLastTradedPriceForSymbols(final List<String> symbols) {
    if (CollectionUtils.isEmpty(symbols)) {
      return Collections.emptyList();
    } else {
      return iexClient.getLastTradedPriceForSymbols(symbols.toArray(new String[0]),
          key);
    }
  }

  /**
   * Get historical price data (close, high, low, open, and volume) for the given symbol
   * over a specified time range. See https://iexcloud.io/docs/api/#historical-prices.
   * Note that, in case a valid date parameter is present, the value of the range parameter is
   * irrelevant; we can assume it is "date".
   * @param symbol A string representing a stock symbol for which to retrieve historical data.
   * @param range A string specifying a range of time. See link.
   * @param date A string representing a date in YYYYMMDD format. Not required. See link.
   * @return A list of IexHistoricalPrice objects for the symbol for each date in the range of time.
   */

  @Cacheable(cacheNames = "historicalPrices")
  public List<IexHistoricalPrice> getHistoricalPrices(
      final String symbol,
      final String range,
      final String date) {

   log.info("Requesting historical prices from IEX.");
    if (symbol.isEmpty()) {
      log.warn("Received historical price request for empty symbol. Returning empty list.");
      return Collections.emptyList();
    }

    if (date != null) {
      // Add a log warning here if range != "date"?
      return iexClient.getHistoricalPriceOnDate(symbol, date, true, key);
      } else {
      return iexClient.getHistoricalPriceRange(symbol, range, key);
    }
  }
}
