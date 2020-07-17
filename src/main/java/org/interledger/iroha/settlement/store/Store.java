package org.interledger.iroha.settlement.store;

import java.math.BigDecimal;
import java.util.List;

public interface Store {
  /**
   * <p>Retrieves the settlement account associated with the given Iroha account id.</p>
   *
   * @param peerIrohaAccountId The Iroha account id of the peer.
   *
   * @return The corresponding settlement account id if any, null otherwise.
   */
  String getSettlementAccountId(String peerIrohaAccountId);

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

  /**
   * <p>Sets the peer's Iroha account id for a given settlement account.</p>
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
   * <p>Marks a transaction as checked for settlement.</p>
   *
   * @param txHash The hash of the transaction to mark as checked.
   *
   * @return
   */
  void saveCheckedTx(String txHash);

  /**
   * <p>Marks a transaction as uncheked for settlement.</p>
   *
   * @param txHash The hash of the transaction to mark as unchecked.
   *
   * @return
   */
  void saveUncheckedTx(String txHash);

  /**
   * <p>Retrieves the last transactions that was checked for settlement.</p>
   *
   * @return The transaction hash of the last checked transaction if any, null otherwise.
   */
  String getLastCheckedTxHash();

  /**
   * <p>Retrieves all transactions that were previously marked as unchecked for settlement.</p>
   *
   * @return A {@link List} consisting of all unchecked transactions' hashes.
   */
  List<String> getUncheckedTxHashes();

  /**
   * <p>Checks whether the given transaction was previously checked for settlements.</p>
   *
   * @param txHash The hash of the transaction to check.
   *
   * @return True if the transaction was already checked, false otherwise.
   */
  boolean wasTxChecked(String txHash);

  /**
   * <p>Saves the HTTP status of the request identified by the given idempotency key.</p>
   *
   * @param idempotencyKey The idempotency key identifying the request.
   *
   * @param status         The HTTP status code of the request.
   *
   * @return
   */
  void saveRequestStatus(String idempotencyKey, Integer status);

  /**
   * <p>Retrieves the status of the already processed request that is identified by the given idempotency key.</p>
   *
   * @param idempotencyKey The idempotency key identifying the request we're interested in.
   *
   * @return The HTTP status code of the request, or null if it wasn't processed before.
   *
   */
  Integer getRequestStatus(String idempotencyKey);
}
