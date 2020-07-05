package org.interledger.iroha.settlement.store;

import java.math.BigDecimal;

public interface Store {
  /**
   * <p>Saves the peer's Iroha account id for a given settlement account.</p>
   *
   * @param settlementAccountId The settlement account.
   *
   * @param peerIrohaAccountId  The Iroha account id of the peer.
   *
   * @return
   */
  void savePeerIrohaAccountId(String settlementAccountId, String peerIrohaAccountId);

  /**
   * <p>Retrieves the peer's Iroha account for a given settlement account.</p>
   *
   * @param settlementAccountId The settlement account.
   *
   * @return The Iroha account id of the peer (or null if not available).
   */
  String getPeerIrohaAccountId(String settlementAccountId);

  /**
   * <p>Persists currency leftovers for a settlement account resulted from
   * precision loss when performing the settlement.</p>
   *
   * @param settlementAccountId The settlement account.
   *
   * @param leftover            A {@link BigDecimal} representing the leftovers to be persisted.
   *
   * @return
   */
  void saveLeftover(String settlementAccountId, BigDecimal leftover);

  /**
   * <p>Retrieves currency leftovers of a given settlement account.</p>
   *
   * @param settlementAccountId The settlement account.
   *
   * @return A {@link BigDecimal} representing the currency leftovers.
   */
  BigDecimal getLeftover(String settlementAccountId);

  /**
   * <p>Checks whether a given settlement account was registered within the store.</p>
   *
   * @param settlementAccountId The settlement account.
   *
   * @return True if the settlement account exists, false otherwise.
   */
  boolean existsSettlementAccount(String settlementAccountId);

  /**
   * <p>Deletes all information corresponding to the given settlement account.
   * The settlement account is not mandatory to already exist in the store.</p>
   *
   * @param settlementAccountId The settlement account.
   *
   * @return
   */
  void deleteSettlementAccount(String settlementAccountId);
}
