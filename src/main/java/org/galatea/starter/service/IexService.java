package org.galatea.starter.service;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;


  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols() {
    return iexClient.getAllSymbols(System.getenv("API_TOKEN"));
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
          System.getenv("API_TOKEN"));
    }
  }

  /**
   * Get historical price data (close, high, low, open, and volume) for the given symbol
   * over a specified time range. See https://iexcloud.io/docs/api/#historical-prices.
   * @param symbol A string representing a stock symbol for which to retrieve historical data.
   * @param range A string specifying a range of time. See link.
   * @param date A string representing a date in YYYYMMDD format. See link.
   * @return A list of IexHistoricalPrice objects for the symbol for each date in the range of time.
   */

  @Cacheable(cacheNames = "historicalPrices")
  public List<IexHistoricalPrice> getHistoricalPrices(
      final String symbol,
      final String range,
      final String date) {

    System.out.println("Fetching data from IEX.");
    if (symbol.isEmpty()) {
      return Collections.emptyList();
    }

    StringBuilder rangeAndDateBuilder = new StringBuilder(range);
    if (!(date.isEmpty())) {
      rangeAndDateBuilder.append("/").append(date);
    }
    String rangeAndDate = rangeAndDateBuilder.toString();

    return iexClient.getHistoricalPrices(symbol,
        rangeAndDate,
        true,
        System.getenv("API_TOKEN"));
  }

}
