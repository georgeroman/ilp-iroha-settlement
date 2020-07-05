package org.interledger.iroha.settlement;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

import org.interledger.iroha.settlement.IrohaException;

import io.grpc.StatusRuntimeException;
import iroha.protocol.QryResponses.AccountResponse;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.ErrorResponseException;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.QueryAPI;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.ValidationException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

@Component
public class SettlementEngine {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${torii-url:http://127.0.0.1:50051}")
  private String toriiUrl;

  @Value("${keypair-name}")
  private String keypairName;

  @Getter
  @Value("${iroha-account-id}")
  private String irohaAccountId;

  @Value("${asset}")
  private String asset;

  private KeyPair irohaAccountKeypair;

  private IrohaAPI irohaApi;
  private QueryAPI queryApi;

  /**
   * <p>Initializes the account keys and connection to Iroha.</p>
   */
  @PostConstruct
  public void init() {
    // Read and initialize account keys
    try {
      String privKey = new String(Files.readAllBytes(
          Paths.get(this.keypairName + ".priv")
      ));
      String pubKey = new String(Files.readAllBytes(
          Paths.get(this.keypairName + ".pub")
      ));

      this.irohaAccountKeypair = Ed25519Sha3.keyPairFromBytes(
          parseHexBinary(privKey),
          parseHexBinary(pubKey)
      );
    } catch (IOException err) {
      this.logger.error("Could not read key pair: {}", err.getMessage());
      System.exit(1);
    } catch (IllegalArgumentException err) {
      this.logger.error("One of the keys is invalid: {}", err.getMessage());
      System.exit(1);
    }

    // Initialize Iroha APIs
    try {
      URL irohaUrl = new URL(this.toriiUrl);
      this.irohaApi = new IrohaAPI(irohaUrl.getHost(), irohaUrl.getPort()); 
    } catch (MalformedURLException err) {
      this.logger.error("Invalid torii-url: {}", err.getMessage());
      System.exit(1);
    }

    this.queryApi = new QueryAPI(this.irohaApi, this.irohaAccountId, this.irohaAccountKeypair);

    // Make sure the provided Iroha account is correct by performing a simple query
    try {
      this.queryApi.getAccount(this.irohaAccountId);
    } catch (StatusRuntimeException err) {
      this.logger.error("Error querying Iroha: {}", err.getMessage());
      System.exit(1);
    } catch (ErrorResponseException err) {
      this.logger.error("Error response from Iroha on getAccount: {}", err.getMessage());
      System.exit(1);
    } catch (ValidationException err) {
      this.logger.error("Failed validation: {}", err.getMessage());
      System.exit(1);
    }
  }

  /**
   * <p>Sends a TransferAsset command to Iroha for transferring a given asset amount to a recipient.</p>
   *
   * @param toIrohaAccountId The recipient of the asset transfer.
   *
   * @param amount           The amount that is to be transferred.
   *
   * @return
   */
  public void transfer(String toIrohaAccountId, BigDecimal amount) throws IrohaException {
    TransactionStatusObserver txObserver = TransactionStatusObserver.builder()
        .onError(err -> {
          throw new IrohaException(err);
        })
        .build();

    TransactionOuterClass.Transaction tx = Transaction.builder(this.irohaAccountId)
        .transferAsset(this.irohaAccountId, toIrohaAccountId, this.asset, "Settlement", amount)
        .sign(this.irohaAccountKeypair)
        .build();

    this.irohaApi.transaction(tx)
        .blockingSubscribe(txObserver);
  }
}
