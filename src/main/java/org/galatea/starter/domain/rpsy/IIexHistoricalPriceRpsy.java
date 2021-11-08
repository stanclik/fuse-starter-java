package org.galatea.starter.domain.rpsy;

import java.util.List;
import java.util.Optional;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface IIexHistoricalPriceRpsy extends CrudRepository<IexHistoricalPrice, Long> {

  /**
   * Retrieves all entities with the given symbol and date.
   */

  List<IexHistoricalPrice> findBySymbolAndDate(String symbol, String date);

  /**
   * Checks for the presence of an entity with the given data.
   * @param symbol A string representing a symbol.
   * @param date A string representing a date in YYYYMMDD format.
   * @return True if the repository contains an entity with the given symbol and date; else, false.
   */

  boolean existsBySymbolAndDate(String symbol, String date);

  @Override
  @Cacheable(cacheNames = "historicalPrices", sync = true)
  Optional<IexHistoricalPrice> findById(Long id);

  @Override
  @CacheEvict(cacheNames = "historicalPrices")
  void deleteById(Long id);

  /**
   * 'p0' required in key because java does not retain parameter names during compilation unless
   * specified. You must use position parameter bindings otherwise.
   */

  @Override
  @CacheEvict(cacheNames = "historicalPrices", key = "#p0.getId()")
  <S extends IexHistoricalPrice> S save(S entity);

}
