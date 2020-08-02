package org.interledger.iroha.settlement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SettlementQuantity {
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal amount;

  private int scale;
}
