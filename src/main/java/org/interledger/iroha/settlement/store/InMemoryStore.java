package org.interledger.iroha.settlement.store;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryStore implements Store {
  private Map<String, String> settlementAccounts;

  public InMemoryStore() {
    this.settlementAccounts = new HashMap<>();
  }

  @Override
  public void savePeerAccount(String settlementAccountId, String peerAccount) {
    this.settlementAccounts.put(settlementAccountId, peerAccount);
  }

  @Override
  public String getPeerAccount(String settlementAccountId) {
    return this.settlementAccounts.get(settlementAccountId);
  }
}
