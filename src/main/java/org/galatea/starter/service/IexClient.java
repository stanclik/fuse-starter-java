package org.galatea.starter.service;

import java.util.List;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A Feign Declarative REST Client to access endpoints from the Free and Open IEX API to get market
 * data. See https://iextrading.com/developer/docs/
 */
@FeignClient(name = "IEX", url = "${spring.rest.iexBasePath}")
public interface IexClient {

  /**
   * Get a list of all stocks supported by IEX. See https://iextrading.com/developer/docs/#symbols.
   * As of July 2019 this returns almost 9,000 symbols, so maybe don't call it in a loop.
   *
   * @return a list of all of the stock symbols supported by IEX.
   */
  @GetMapping("/ref-data/symbols")
  List<IexSymbol> getAllSymbols(
      // Setting required = false is useful for testing.
      @RequestParam(value = "token", required = false) String token);

  /**
   * Get the last traded price for each stock symbol passed in. See https://iextrading.com/developer/docs/#last.
   *
   * @param symbols stock symbols to get last traded price for.
   * @return a list of the last traded price for each of the symbols passed in.
   */
  @GetMapping("/tops/last")
  List<IexLastTradedPrice> getLastTradedPriceForSymbols(@RequestParam("symbols") String[] symbols,
      @RequestParam(value = "token", required = false) String token);

  /**
   * Get historical price data (close, high, low, open, and volume) for the given symbol
   * over a range of time (not a single day). See https://iexcloud.io/docs/api/#historical-prices.
   * @param symbol A string representing a stock symbol for which to retrieve historical data.
   * @param range A string other than "date" representing a range of time. See link.
   * @param token An API token.
   * @return A list of IexHistoricalPrice objects for the symbol for each date in the range of time.
   */

  @GetMapping("stock/{symbol}/chart/{range}")
  List<IexHistoricalPrice> getHistoricalPriceForRange(
      @PathVariable("symbol") String symbol,
      @PathVariable("range") String range,
      @RequestParam(value = "token", required = false) String token);

  /**
   * Get historical price data (close, high, low, open, and volume) for the given symbol
   * for a single date. See https://iexcloud.io/docs/api/#historical-prices.
   * @param symbol A string representing a stock symbol for which to retrieve historical data.
   * @param date A string specifying a date. See link.
   * @param chartByDay true restricts the output to close, high, low, etc. over the course of the
   *                   entire day. false results in outputting data for each minute of the day.
   * @param token An API token.
   * @return A list of IexHistoricalPrice objects for the symbol for the specified date.
   */

  @GetMapping("/stock/{symbol}/chart/date/{date}")
  List<IexHistoricalPrice> getHistoricalPriceForDate(
      @PathVariable("symbol") String symbol,
      @PathVariable("date") String date,
      @RequestParam("chartByDay") Boolean chartByDay,
      @RequestParam(value = "token", required = false) String token);

}
