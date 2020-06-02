package org.interledger.iroha.settlement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import iroha.protocol.BlockOuterClass;
import iroha.protocol.Primitive.RolePermission;
import iroha.protocol.QryResponses.AccountAsset;
import iroha.protocol.QryResponses.QueryResponse;
import iroha.protocol.Queries;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer;
import jp.co.soramitsu.iroha.testcontainers.PeerConfig;
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SimpleTransferAndQueryTest {
  private static final String ASSET = "asset";
  private static final int ASSET_SCALE = 2;
  private static final String DOMAIN = "domain";
  private static final String ROLE = "role";
  private static final String SENDER = "sender";
  private static final String RECEIVER = "receiver";

  private static final Ed25519Sha3 crypto = new Ed25519Sha3();
  private static final KeyPair PEER_KEYPAIR = crypto.generateKeypair();
  private static final KeyPair SENDER_KEYPAIR = crypto.generateKeypair();
  private static final KeyPair RECEIVER_KEYPAIR = crypto.generateKeypair();

  private static final IrohaContainer iroha = new IrohaContainer()
      .withPeerConfig(getPeerConfig());

  private static String buildUser(String user, String domain) {
    return String.format("%s@%s", user, domain);
  }

  private static String buildAsset(String asset, String domain) {
    return String.format("%s#%s", asset, domain);
  }

  private static BlockOuterClass.Block getGenesisBlock() {
    return new GenesisBlockBuilder()
        .addTransaction(
          Transaction.builder(null)
              .addPeer("0.0.0.0:10001", PEER_KEYPAIR.getPublic())
              .createRole(ROLE,
                Arrays.asList(
                  RolePermission.can_transfer,
                  RolePermission.can_get_my_acc_ast,
                  RolePermission.can_get_my_txs,
                  RolePermission.can_receive
                )
              )
              .createDomain(DOMAIN, ROLE)
              .createAsset(ASSET, DOMAIN, ASSET_SCALE)
              .createAccount(SENDER, DOMAIN, SENDER_KEYPAIR.getPublic())
              .createAccount(RECEIVER, DOMAIN, RECEIVER_KEYPAIR.getPublic())
              .build()
              .build()
          )
          .addTransaction(
            Transaction.builder(buildUser(SENDER, DOMAIN))
                .addAssetQuantity(buildAsset(ASSET, DOMAIN), new BigDecimal("100"))
                .build()
                .build()
          )
          .build();
  }

  private static PeerConfig getPeerConfig() {
    PeerConfig config = PeerConfig.builder()
        .genesisBlock(getGenesisBlock())
        .build();

    config.withPeerKeyPair(PEER_KEYPAIR);

    return config;
  }

  private static int getAccountBalance(IrohaAPI api, String user, KeyPair userKeypair) {
    Queries.Query query = Query.builder(user, 1)
        .getAccountAssets(user)
        .buildSigned(userKeypair);

    QueryResponse response = api.query(query);

    List<AccountAsset> assets = response.getAccountAssetsResponse()
        .getAccountAssetsList();

    Optional<AccountAsset> assetOptional = assets.stream()
        .filter(a -> a.getAssetId().equals(buildAsset(ASSET, DOMAIN)))
        .findFirst();

    return assetOptional.map(a -> Integer.parseInt(a.getBalance()))
        .orElse(0);
  }

  @BeforeAll
  public static void beforeAll() {
    iroha.start();
  }

  @AfterAll
  public static void afterAll() {
    iroha.stop();
  }

  @Test
  public void accountBalanceQueryTest() {
    IrohaAPI api = iroha.getApi();

    TransactionOuterClass.Transaction tx = Transaction.builder(buildUser(SENDER, DOMAIN))
        .transferAsset(
          buildUser(SENDER, DOMAIN),
          buildUser(RECEIVER, DOMAIN),
          buildAsset(ASSET, DOMAIN),
          "Reason",
          "50"
        )
        .sign(SENDER_KEYPAIR)
        .build();

    TransactionStatusObserver txObserver = TransactionStatusObserver.builder()
        .onTransactionFailed(ftx -> System.out.println(String.format(
            "Transaction %s failed: %s",
            ftx.getTxHash(),
            ftx.getErrOrCmdName()
          ))
        )
        .onError(err -> System.out.println("Failed: " + err))
        .onTransactionCommitted(ctx -> System.out.println("Committed"))
        .onComplete(() -> System.out.println("Completed"))
        .build();

    api.transaction(tx)
        .blockingSubscribe(txObserver);

    int senderBalance = getAccountBalance(api, buildUser(SENDER, DOMAIN), SENDER_KEYPAIR);
    int receiverBalance = getAccountBalance(api, buildUser(RECEIVER, DOMAIN), RECEIVER_KEYPAIR);

    assertEquals(50, senderBalance);
    assertEquals(50, receiverBalance);
  }
}
