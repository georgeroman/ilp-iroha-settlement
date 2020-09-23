package org.interledger.settlement.common.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import org.interledger.settlement.common.model.SettlementAccount;
import org.interledger.settlement.common.model.SettlementQuantity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

public interface SettlementController {
  /**
   * <p>Called by the Connector to inform the Settlement Engine that a new account was created within
   * the accounting system using the given account identifier.</p>
   *
   * @param settlementAccount The account identifier as supplied by the Connector.
   *
   * @return
   */
  @RequestMapping(
      path = "/accounts",
      method = RequestMethod.POST,
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  ResponseEntity<SettlementAccount> setupAccount(
      @RequestBody SettlementAccount settlementAccount
  );

  /**
   * <p>Called by the Connector to inform the Settlement Engine that an account was deleted.</p>
   *
   * @param settlementAccountId The account identifier as supplied by the Connector.
   *
   * @return
   */
  @RequestMapping(
      path = "/accounts/{settlementAccountId}",
      method = RequestMethod.DELETE
  )
  ResponseEntity<Void> deleteAccount(
      @PathVariable String settlementAccountId
  );

  /**
   * <p>Called by the Connector to asynchronously trigger a settlement in the Settlement Engine.</p>
   *
   * @param idempotencyKey      The idempotence identifier defined in the Settlement Engine RFC
   *                            (typed as a {@link String}, but should always be a Type4 UUID).
   *
   * @param settlementAccountId The account identifier as supplied by the Connector.
   *
   * @return
   */
  @RequestMapping(
      path = "/accounts/{settlementAccountId}/settlements",
      method = RequestMethod.POST,
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  ResponseEntity<Void> performOutgoingSettlement(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @RequestBody SettlementQuantity quantity,
      @PathVariable String settlementAccountId
  );

  /**
   * <p>Called by the Connector to process and respond to an incoming message from the peer's
   * Settlement Engine.</p>
   *
   * @param message             A byte array of opaque data that was sent by the peer's Settlement Engine.
   *
   * @param settlementAccountId The account identifier as supplied by the Connector.
   *
   * @return A byte array representing the response message to be sent to the peer's Settlement Engine.
   */
  @RequestMapping(
      path = "/accounts/{settlementAccountId}/messages",
      method = RequestMethod.POST,
      consumes = APPLICATION_OCTET_STREAM_VALUE,
      produces = APPLICATION_OCTET_STREAM_VALUE
  )
  ResponseEntity<byte[]> handleIncomingMessage(
      @RequestBody byte[] message,
      @PathVariable String settlementAccountId
  );
}
