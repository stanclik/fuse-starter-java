package org.galatea.starter.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IexHistoricalPrice {

  @JsonAlias(value = "key")
  private String symbol;
  private String date;
  private BigDecimal open;
  private BigDecimal low;
  private BigDecimal close;
  private BigDecimal high;
  private Integer volume;

}
