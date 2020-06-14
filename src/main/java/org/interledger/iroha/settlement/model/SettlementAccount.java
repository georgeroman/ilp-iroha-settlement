package org.interledger.iroha.settlement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Models an Interledger account (representing conditional IOUs between peers).
 *
 * @see "https://github.com/interledger/rfcs/blob/master/0038-settlement-engines/0038-settlement-engines.md#accounts-and-identifiers"
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementAccount {
  private String id;
}
