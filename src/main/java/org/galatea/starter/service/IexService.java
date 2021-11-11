package org.galatea.starter.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import java.util.List;
import java.util.Scanner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.domain.rpsy.IIexHistoricalPriceRpsy;
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
  IexClient iexClient;

  @NonNull
  private IIexHistoricalPriceRpsy priceRpsy;

  private LocalDate earliestRequestedRangeStart;

  private final String key = setKey();

  private String setKey() {
    try {
      File keyFile = ResourceUtils.getFile("classpath:keys.csv");
      Scanner sc = new Scanner(keyFile);
      try {
        return sc.nextLine().split(",")[1];   // Assumes the IEX key is on
      } catch (ArrayIndexOutOfBoundsException ex) {  // the first line of keys.csv
        log.warn(ex.getMessage());
        return null;
      }
    } catch (FileNotFoundException ex) {
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
   * Formats a date from YYYYMMDD format to YYYY-MM-DD format. This is to accommodate the historical
   * price repository which automatically does this conversion.
   * @param date A string representing a date in YYYYMMDD format.
   * @return A string representing the same date in YYYY-MM-DD format.
   */

  private String formatDate(final String date) {
    return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
  }

  /**
   * Gets historical price data for the given symbol and date from the IEX API. Saves the new
   * IexHistoricalPrice object to the price repository as a side effect.
   */

  private List<IexHistoricalPrice> getHistoricalPriceForDate(
      final String symbol,
      final String date) {

    String searchDate = formatDate(date);
    log.info("Searching repository for symbol {} and date {}", symbol, searchDate);

    if (priceRpsy.existsBySymbolAndDate(symbol, searchDate)) {
      log.info("Historical price for symbol {}, date{} found in repository.", symbol, date);
      return priceRpsy.findBySymbolAndDate(symbol, searchDate);
    } else {
      log.info("Requesting historical price for symbol {}, date {} from IEX.", symbol, date);
      List<IexHistoricalPrice> prices;
      prices = iexClient.getHistoricalPriceForDate(symbol, date, true, key);
      Iterable<IexHistoricalPrice> saved = priceRpsy.saveAll(prices);
      log.info("Saved historical price to repository: {}", saved);
      return prices;
    }
  }

  private LocalDate rangeToStartDateHelper(final String range, final LocalDate today) {

    ChronoUnit unit = ChronoUnit.DAYS;
    String rangeSuffix = range.substring(range.length() - 1);

    if (rangeSuffix.equals("m")) {
      unit = ChronoUnit.MONTHS;
    } else if (rangeSuffix.equals("y")) {
      unit = ChronoUnit.YEARS;
    } else if (!rangeSuffix.equals("d")) {
      log.warn("Argument range must end with \"d\", \"m\", or \"y\"; received {}.", rangeSuffix);
      log.warn("Assuming units of DAYS.");
    }

    int amountToSubtract = Integer.parseInt(range.substring(0, range.length() - 1));
    return today.minus(amountToSubtract, unit);
  }

  /**
   * Takes an IEX range and returns the first date in the range.
   * @param range A string representing an IEX range for some number of days, months, or years.
   * @return A LocalDate object representing the first date in the range.
   */
  public LocalDate rangeToStartDate(final String range) {

    return rangeToStartDateHelper(range, LocalDate.now());
  }

  /**
   * Gets historical price data for the given symbol and range from the IEX API. Saves new
   * IexHistoricalPrice objects to the price repository as a side effect.
   */

  private List<IexHistoricalPrice> getHistoricalPricesForRange(
      final String symbol,
      final String range) {

    LocalDate rangeStartDate = rangeToStartDate(range);
    if (earliestRequestedRangeStart != null
        && !rangeStartDate.isBefore(earliestRequestedRangeStart)) {
      log.info("Historical prices for range {} found in repository.", range);
      return priceRpsy.findBySymbolAndDateGreaterThanEqual(symbol,
          rangeStartDate.toString());
    } else {
      earliestRequestedRangeStart = rangeStartDate; // Update for future calls.
      log.info("Requesting historical prices for range {} from IEX.", range);
      List<IexHistoricalPrice> prices = iexClient.getHistoricalPriceForRange(symbol, range, key);
      log.info("{}", prices);
      Iterable<IexHistoricalPrice> saved = priceRpsy.saveAll(prices);
      log.info("Saved historical prices to repository: {}", saved);
      return prices;
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

  public List<IexHistoricalPrice> getHistoricalPrices(
      final String symbol,
      final String range,
      final String date) {

    if (symbol.isEmpty()) {
      log.warn("Received historical price request for empty symbol. Returning empty list.");
      return Collections.emptyList();
    }

    if (range.equals("date")) {
      if (date == null) {
        log.warn("Received range = \"date\" and date = \"null\". Returning empty list.");
        return Collections.emptyList();
      } else {
        return getHistoricalPriceForDate(symbol, date);
      }
    } else {
      if (date != null) {
        log.warn("Received range = \"{}\" and date = \"{}\". Returning historical price for date.",
            range,
            date);
        return getHistoricalPriceForDate(symbol, date);
      } else {
        return getHistoricalPricesForRange(symbol, range);
      }
    }
  }

  /**
   * Resets instance variables. Useful for testing.
   */

  public void resetService() {
    earliestRequestedRangeStart = null;
    priceRpsy.deleteAll();
  }
}
