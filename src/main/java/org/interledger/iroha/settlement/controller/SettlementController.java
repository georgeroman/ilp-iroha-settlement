package org.interledger.iroha.settlement.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import org.interledger.iroha.settlement.IrohaException;
import org.interledger.iroha.settlement.SettlementEngine;
import org.interledger.iroha.settlement.Util;
import org.interledger.iroha.settlement.config.DefaultArgumentValues;
import org.interledger.iroha.settlement.message.PaymentDetailsMessage;
import org.interledger.iroha.settlement.model.SettlementAccount;
import org.interledger.iroha.settlement.model.SettlementQuantity;
import org.interledger.iroha.settlement.store.Store;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@RestController
public class SettlementController {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  @Value("${asset-scale:" + DefaultArgumentValues.ASSET_SCALE + "}")
  private String assetScale;

  @Value("${connector-url:" + DefaultArgumentValues.CONNECTOR_URL + "}")
  private String connectorUrl;

  @Autowired
  private SettlementEngine settlementEngine;

  @Autowired
  private Store store;

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
      consumes = APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Void> setupAccount(
      @RequestBody SettlementAccount settlementAccount
  ) {
    this.logger.info("POST /accounts { id: {} }", settlementAccount.getId());

    // Create a request for payment details for the current ILP account
    PaymentDetailsMessage paymentDetailsRequest = new PaymentDetailsMessage(
        this.settlementEngine.getIrohaAccountId()
    );

    // Only send request for payment details if we don't have that information
    if (this.store.getPeerIrohaAccountId(settlementAccount.getId()) == null) {
      try {
        // TODO: Find a way to abstract away the HTTP request/response handling parts

        this.logger.info(
            "Serialized PaymentDetailsMessage object to be sent to peer: "
            + JSON_FACTORY.toString(paymentDetailsRequest)
        );

        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(
            (HttpRequest request) -> {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        );

        GenericUrl connectorMessageUrl = new GenericUrl(this.connectorUrl);
        connectorMessageUrl.appendRawPath("/accounts/" + settlementAccount.getId() + "/messages");

        HttpRequest request = requestFactory.buildPostRequest(
            connectorMessageUrl,
            ByteArrayContent.fromString(APPLICATION_JSON_VALUE, JSON_FACTORY.toString(paymentDetailsRequest))
        );

        // https://github.com/interledger/rfcs/blob/master/0038-settlement-engines/0038-settlement-engines.md#retry-behavior
        ExponentialBackOff backoff = new ExponentialBackOff.Builder()
            .setInitialIntervalMillis(500)
            .setMaxElapsedTimeMillis(900000)
            .setMaxIntervalMillis(6000)
            .setMultiplier(1.5)
            .setRandomizationFactor(0.5)
            .build();
        request.setUnsuccessfulResponseHandler(
            new HttpBackOffUnsuccessfulResponseHandler(backoff)
        );

        // Send the payment details request and wait for a corresponding response
        PaymentDetailsMessage paymentDetailsResponse = request.execute().parseAs(PaymentDetailsMessage.class);

        this.logger.info(
            "Got peer's Iroha account id ({}) corresponding to settlement account {}",
            paymentDetailsResponse.getIrohaAccountId(),
            settlementAccount.getId()
        );

        // Save peer's Iroha account id
        this.store.savePeerIrohaAccountId(settlementAccount.getId(), paymentDetailsResponse.getIrohaAccountId());

        return new ResponseEntity<>(HttpStatus.CREATED); 
      } catch (MalformedURLException err) {
        // Once this happens, it will always happen
        this.logger.error("Invalid connector-url: {}", err.getMessage());
        this.logger.info("Restart the settlement engine with a valid connector-url value");

        // Fatal error
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      } catch (IOException err) {
        this.logger.error("Error while handling payment details: {}", err.getMessage());

        // Fatal error
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.CREATED); 
    }
  }

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
  public ResponseEntity<Void> deleteAccount(
      @PathVariable String settlementAccountId
  ) {
    this.logger.info("DELETE /accounts/{}", settlementAccountId);

    // Require the settlement account to already exist in the store
    if (!this.store.existsSettlementAccount(settlementAccountId)) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    this.store.deleteSettlementAccount(settlementAccountId);

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

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
  public ResponseEntity<Void> performOutgoingSettlement(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @RequestBody SettlementQuantity quantity,
      @PathVariable String settlementAccountId
  ) {
    this.logger.info("POST /accounts/{}/settlements { Idempotency-Key: {} }", settlementAccountId, idempotencyKey);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);

    try {
      // We need to convert from the scale the connector sent us to our own scale
      int fromScale = quantity.getScale();
      int toScale = Integer.parseInt(this.assetScale);

      // Scale the amount (together with any pre-existing leftovers)
      Map.Entry<BigDecimal, BigDecimal> scalingResult = Util.scaleWithPrecisionLoss(
          quantity.getAmount().add(this.store.getLeftover(settlementAccountId)),
          fromScale,
          toScale
      );
      BigDecimal scaledAmount = scalingResult.getKey();

      // Retrieve the peer's Iroha account id corresponding to the current settlement account
      String peerIrohaAccountId = this.store.getPeerIrohaAccountId(settlementAccountId);
      if (peerIrohaAccountId == null) {
        // Fatal error
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }

      this.logger.info(
          "Performing settlement on settlement account {} "
          + "(from Iroha account {} to Iroha account {}) for an amount of {}",
          settlementAccountId,
          this.settlementEngine.getIrohaAccountId(),
          peerIrohaAccountId,
          scaledAmount.toString()
      );

      // Perform the actual ledger settlement
      this.settlementEngine.transfer(peerIrohaAccountId, scaledAmount);

      // Save any leftovers due to precision loss
      BigDecimal precisionLoss = scalingResult.getValue();
      this.store.saveLeftover(settlementAccountId, precisionLoss);

      return new ResponseEntity<>(headers, HttpStatus.CREATED);
    } catch (NumberFormatException err) {
      // Once this happens, it will always happen
      this.logger.error("Invalid asset-scale: {}", err.getMessage());
      this.logger.info("Restart the settlement engine with a valid asset-scale value");

      // Fatal error
      return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (IrohaException err) {
      this.logger.error("Could not send transfer command to Iroha: {}", err.getMessage());

      // Fatal error
      return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

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
  public ResponseEntity<byte[]> handleIncomingMessage(
      @RequestBody byte[] message,
      @PathVariable String settlementAccountId
  ) {
    String messageString = new String(message, StandardCharsets.UTF_8);

    this.logger.info("POST /accounts/{}/messages {}", settlementAccountId, messageString);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_OCTET_STREAM);

    try {
      PaymentDetailsMessage paymentDetailsRequest = JSON_FACTORY.fromString(messageString, PaymentDetailsMessage.class);

      // Save peer's Iroha account id
      this.store.savePeerIrohaAccountId(settlementAccountId, paymentDetailsRequest.getIrohaAccountId());

      this.logger.info(
          "Got peer's Iroha account id ({}) corresponding to settlement account {}",
          paymentDetailsRequest.getIrohaAccountId(),
          settlementAccountId
      );

      PaymentDetailsMessage paymentDetailsResponse = new PaymentDetailsMessage(
          this.settlementEngine.getIrohaAccountId()
      );

      this.logger.info(
          "Serialized PaymentDetailsMessage object to be sent to peer: " + JSON_FACTORY.toString(paymentDetailsResponse)
      );

      // Respond with our own Iroha account id
      return new ResponseEntity<>(
          JSON_FACTORY.toString(paymentDetailsResponse).getBytes(), headers, HttpStatus.CREATED
      );
    } catch (IOException err) {
      this.logger.error("Invalid payment details message: {}", err.getMessage());
      this.logger.info("Only payment details messages are accepted via /accounts/:id/messages");

      // Fatal error
      return new ResponseEntity<>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
