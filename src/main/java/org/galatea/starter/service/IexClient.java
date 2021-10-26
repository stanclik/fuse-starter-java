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
  List<IexSymbol> getAllSymbols(@RequestParam("token") String token);

  /**
   * Get the last traded price for each stock symbol passed in. See https://iextrading.com/developer/docs/#last.
   *
   * @param symbols stock symbols to get last traded price for.
   * @return a list of the last traded price for each of the symbols passed in.
   */
  @GetMapping("/tops/last")
  List<IexLastTradedPrice> getLastTradedPriceForSymbols(@RequestParam("symbols") String[] symbols,
      @RequestParam("token") String token);

  /**
   * Get historical price data (close, high, low, open, and volume) for the given symbol
   * over a specified time range. See https://iexcloud.io/docs/api/#historical-prices.
   * @param symbol A string representing a stock symbol for which to retrieve historical data.
   * @param range A string specifying a range and date. See link.
   * @return A list of IexHistoricalPrice objects for the symbol for each date in the range of time.
   */

  @GetMapping("/stock/{symbol}/chart/{rangeAndDate}")
  List<IexHistoricalPrice> getHistoricalPrices(
      @PathVariable("symbol") String symbol,
      @PathVariable("rangeAndDate") String range,
      @RequestParam("chartByDay") Boolean chartByDay,
      @RequestParam("token") String token);

}
