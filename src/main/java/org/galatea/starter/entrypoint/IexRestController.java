package org.galatea.starter.entrypoint;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.service.IexService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
@Validated
@RestController
@RequiredArgsConstructor
public class IexRestController extends BaseRestController {

  @NonNull
  private IexService iexService;

  /**
   * Exposes an endpoint to get all of the symbols available on IEX.
   *
   * @return a list of all IexStockSymbols.
   */
  @GetMapping(value = "${mvc.iex.getAllSymbolsPath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public List<IexSymbol> getAllStockSymbols() {
    return iexService.getAllSymbols();
  }

  /**
   * Get the last traded price for each of the symbols passed in.
   *
   * @param symbols list of symbols to get last traded price for.
   * @return a List of IexLastTradedPrice objects for the given symbols.
   */
  @GetMapping(value = "${mvc.iex.getLastTradedPricePath}", produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public List<IexLastTradedPrice> getLastTradedPrice(
      @RequestParam(value = "symbols") final List<String> symbols) {
    return iexService.getLastTradedPriceForSymbols(symbols);
  }

  /**
   * Get historical price data (close, high, low, open, and volume) for the given symbol
   * over a specified time range. See https://iexcloud.io/docs/api/#historical-prices.
   * @param symbol A string representing a stock symbol for which to retrieve historical data.
   * @param range A string specifying a range of time. See link.
   * @param date A string representing a date in YYYYMMDD format. See link.
   * @return A list of IexHistoricalPrice objects for the symbol for each date in the range of time.
   */

  @GetMapping(value = "${mvc.iex.getHistoricalPricesPath}", produces = {
      MediaType.APPLICATION_JSON_VALUE})
  public List<IexHistoricalPrice> getHistoricalPrices(
      @RequestParam(value = "symbol") final String symbol,
      @RequestParam(value = "range") final String range,
      @RequestParam(value = "date", defaultValue = "") final String date) {
    List<IexHistoricalPrice> historicalPrices = iexService.getHistoricalPricesForSymbol(symbol,
                                                                                        range,
                                                                                        date);
    historicalPrices = insertSymbol(historicalPrices, symbol);
    return historicalPrices;
  }

  /**
   * Takes a list of IexHistoricalPrice objects and sets each object's symbol field to symbol.
   * @param prices A list of IexHistoricalPrice objects.
   * @param symbol A string representing a symbol.
   * @return A list of updated IexHistoricalPrice objects.
   */

  public List<IexHistoricalPrice> insertSymbol(final List<IexHistoricalPrice> prices,
                                               final String symbol) {
    for (IexHistoricalPrice price : prices) {
      price.setSymbol(symbol);
    }
    return prices;
  }

}
