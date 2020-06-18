package org.interledger.iroha.settlement.store;

public interface Store {
  void savePeerAccount(String settlementAccountId, String peerAccount);

  String getPeerAccount(String settlementAccountId);
}
