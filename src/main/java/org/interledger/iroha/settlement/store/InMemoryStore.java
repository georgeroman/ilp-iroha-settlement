package org.interledger.iroha.settlement.store;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryStore implements Store {
  private Map<String, String> settlementAccounts;
  private Map<String, BigDecimal> leftovers;

  public InMemoryStore() {
    this.settlementAccounts = new HashMap<>();
    this.leftovers = new HashMap<>();
  }

  @Override
  public void savePeerIrohaAccountId(String settlementAccountId, String peerIrohaAccountId) {
    this.settlementAccounts.put(settlementAccountId, peerIrohaAccountId);
  }

  @Override
  public String getPeerIrohaAccountId(String settlementAccountId) {
    return this.settlementAccounts.get(settlementAccountId);
  }

  @Override
  public void saveLeftover(String settlementAccountId, BigDecimal leftover) {
    this.leftovers.put(settlementAccountId, leftover);
  }

  @Override
  public BigDecimal getLeftover(String settlementAccountId) {
    return this.leftovers.getOrDefault(settlementAccountId, BigDecimal.ZERO);
  }

  @Override
  public boolean existsSettlementAccount(String settlementAccountId) {
    return this.settlementAccounts.containsKey(settlementAccountId);
  }

  @Override
  public void deleteSettlementAccount(String settlementAccountId) {
    this.settlementAccounts.remove(settlementAccountId);
    this.leftovers.remove(settlementAccountId);
  }
}
