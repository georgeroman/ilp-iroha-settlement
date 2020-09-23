package org.interledger.settlement.iroha.store;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryStore implements Store {
  private Map<String, String> settlementAccounts;
  private Map<String, BigDecimal> leftovers;
  private Map<String, Integer> requestStatuses;
  private List<String> checkedTxs;
  private List<String> uncheckedTxs;

  /**
   * <p>In-memory store constructor.</p>
   */
  public InMemoryStore() {
    this.settlementAccounts = new HashMap<>();
    this.leftovers = new HashMap<>();
    this.requestStatuses = new HashMap<>();
    this.checkedTxs = new ArrayList<>();
    this.uncheckedTxs = new ArrayList<>();
  }

  @Override
  public String getSettlementAccountId(String peerIrohaAccountId) {
    for (String settlementAccountId : settlementAccounts.keySet()) {
      if (this.settlementAccounts.get(settlementAccountId).equals(peerIrohaAccountId)) {
        return settlementAccountId;
      }
    }
    return null;
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
  public void saveCheckedTx(String txHash) {
    this.checkedTxs.add(txHash);
  }

  @Override
  public void saveUncheckedTx(String txHash) {
    this.uncheckedTxs.add(txHash);
  }

  @Override
  public String getLastCheckedTxHash() {
    if (this.checkedTxs.size() > 0) {
      return this.checkedTxs.get(this.checkedTxs.size() - 1);
    } else {
      return null;
    }
  }

  @Override
  public List<String> getUncheckedTxHashes() {
    return this.uncheckedTxs;
  }

  @Override
  public boolean wasTxChecked(String txHash) {
    return this.checkedTxs.contains(txHash); 
  }

  @Override
  public void saveRequestStatus(String idempotencyKey, Integer status) {
    this.requestStatuses.put(idempotencyKey, status);
  }

  @Override
  public Integer getRequestStatus(String idempotencyKey) {
    return this.requestStatuses.get(idempotencyKey);
  }
}
