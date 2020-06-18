package org.interledger.iroha.settlement;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

import io.grpc.StatusRuntimeException;
import iroha.protocol.QryResponses.AccountResponse;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.ErrorResponseException;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.QueryAPI;
import jp.co.soramitsu.iroha.java.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

  @Value("${account-id}")
  private String accountId;

  private KeyPair accountKeypair;

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

      this.accountKeypair = Ed25519Sha3.keyPairFromBytes(
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

    this.queryApi = new QueryAPI(this.irohaApi, this.accountId, this.accountKeypair);

    // Make sure the provided account is correct by performing a simple query
    try {
      this.queryApi.getAccount(this.accountId);
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
}
