package org.interledger.iroha.settlement;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

import iroha.protocol.Primitive.GrantablePermission;
import iroha.protocol.Primitive.RolePermission;
import iroha.protocol.QryResponses.QueryResponse;
import iroha.protocol.Queries;
import iroha.protocol.TransactionOuterClass;
import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.iroha.java.ErrorResponseException;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Query;
import jp.co.soramitsu.iroha.java.QueryAPI;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.java.TransactionStatusObserver;
import jp.co.soramitsu.iroha.java.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Scanner;

public class App {
  private static final Ed25519Sha3 crypto = new Ed25519Sha3();

  // QueryAPI doesn't support all query types, some of them need to be manually built
  // and in order to do that we need to keep track of the count of previous queries
  private static int counter = 0;

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: mvn exec:java -Dexec.args=\"<accountId> <accountKeypairPath>\"");
      System.exit(1);
    }

    String accountId = args[0];
    String accountKeypairPath = args[1];

    String accountPriv = "";
    String accountPub = "";
    try {
      accountPriv = new String(Files.readAllBytes(
          Paths.get(String.format("%s/%s.priv", accountKeypairPath, accountId))
      ));
      accountPub = new String(Files.readAllBytes(
          Paths.get(String.format("%s/%s.pub", accountKeypairPath, accountId))
      ));
    } catch (IOException err) {
      System.err.println("Failed reading account keys: " + err.getMessage());
      System.err.println("Make sure the path to the keys does not contain a trailing '/'!");
      System.exit(1);
    }

    KeyPair accountKeypair = Ed25519Sha3.keyPairFromBytes(
        parseHexBinary(accountPriv),
        parseHexBinary(accountPub)
    );

    IrohaAPI irohaApi = new IrohaAPI("127.0.0.1", 50051);
    QueryAPI queryApi = new QueryAPI(irohaApi, accountId, accountKeypair);

    mainLoop(irohaApi, queryApi, accountId, accountKeypair);
  }

  private static void mainLoop(IrohaAPI irohaApi, QueryAPI queryApi, String myAccountId, KeyPair myAccountKeypair) {
    Scanner in = new Scanner(System.in);

    System.out.println("Iroha client based on iroha-java");
    while (true) {
      System.out.println("Choose an option:");
      System.out.println("0. Exit");
      System.out.println("1. Query");
      System.out.println("2. Send transaction");

      System.out.print(">>> ");
      try {
        int choice = Integer.parseInt(in.nextLine());
        if (choice == 0) { // Exit
          System.exit(0);
        } else if (choice == 1) { // Query
          query(irohaApi, queryApi, myAccountId, myAccountKeypair);
        } else { // Send transaction
          sendTransaction(irohaApi, myAccountId, myAccountKeypair);
        }
      } catch (NumberFormatException err) {
        System.err.println("Invalid input: " + err.getMessage());
      }
    }
  }

  private static void query(IrohaAPI irohaApi, QueryAPI queryApi, String myAccountId, KeyPair myAccountKeypair) {
    Scanner in = new Scanner(System.in);

    while (true) {
      System.out.println("Choose an option:");
      System.out.println("0. Back");
      System.out.println("1. GetAccount (get the state of an account)");
      System.out.println("2. GetBlock (get the block having a given height)");
      System.out.println("3. GetSignatories (get the signatories of an account)");
      System.out.println("4. GetTransaction (get transaction details)");
      System.out.println("5. GetPendingTransactions (get pending transactions - multisig or batches - of query creator)");
      System.out.println("6. GetAccountTransactions (get the transactions of an account)");
      System.out.println("7. GetAccountAssetTransactions (get the transactions of an account associated with a given asset)");
      System.out.println("8. GetAccountAssets (get the assets of an account)");
      System.out.println("9. GetAccountDetails (get the details of an account)");
      System.out.println("10. GetAssetInfo (get information about an asset)");
      System.out.println("11. GetRoles (get existing roles in the system)");
      System.out.println("12. GetRolePermissions (get the permissions associated to a role)");
      System.out.println("13. GetPeers (get the list of peers in the Iroha network)");

      System.out.print(">>> ");
      try {
        counter++;

        int choice = Integer.parseInt(in.nextLine());
        if (choice == 0) { // Back
          return;
        } else if (choice == 1) { // GetAccount
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.println(queryApi.getAccount(accountId).toString());
        } else if (choice == 2) { // GetBlock
          System.out.print("height: ");
          long height = Long.parseLong(in.nextLine());

          System.out.println(queryApi.getBlock(height).toString());
        } else if (choice == 3) { // GetSignatories
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.println(queryApi.getSignatories(accountId).toString());
        } else if (choice == 4) { // GetTransaction
          System.out.print("tx_hash: ");
          String txHash = in.nextLine();

          System.out.println(queryApi.getTransactions(Arrays.asList(txHash)).toString());
        } else if (choice == 5) { // GetPendingTransactions
          System.out.print("page_size: ");
          int pageSize = Integer.parseInt(in.nextLine());

          System.out.println(queryApi.getPendingTransactions(pageSize).toString());
        } else if (choice == 6) { // GetAccountTransactions
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("page_size: ");
          int pageSize = Integer.parseInt(in.nextLine());

          System.out.println(queryApi.getAccountTransactions(accountId, pageSize).toString());
        } else if (choice == 7) { // GetAccountAssetTransactions
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("asset_id: ");
          String assetId = in.nextLine();

          System.out.print("page_size: ");
          int pageSize = Integer.parseInt(in.nextLine());

          System.out.println(queryApi.getAccountAssetTransactions(accountId, assetId, pageSize).toString());
        } else if (choice == 8) { // GetAccountAssets
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("page_size: ");
          int pageSize = Integer.parseInt(in.nextLine());

          System.out.println(queryApi.getAccountAssets(accountId, pageSize).toString());
        } else if (choice == 9) { // GetAccountDetails
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("page_size: ");
          int pageSize = Integer.parseInt(in.nextLine());

          System.out.println(queryApi.getAccountDetails(accountId, null, null, pageSize).toString());
        } else if (choice == 10) { // GetAssetInfo
          System.out.print("asset_id: ");
          String assetId = in.nextLine();

          System.out.println(queryApi.getAssetInfo(assetId).toString());
        } else if (choice == 11) { // GetRoles
          Queries.Query query = Query.builder(myAccountId, counter)
              .getRoles()
              .buildSigned(myAccountKeypair);

          QueryResponse response = irohaApi.query(query);

          if (response.hasErrorResponse()) {
            throw new ErrorResponseException(response.getErrorResponse());
          }

          System.out.println(response.getRolesResponse().toString());
        } else if (choice == 12) { // GetRolePermissions
          System.out.print("role_id: ");
          String roleId = in.nextLine();

          Queries.Query query = Query.builder(myAccountId, counter)
              .getRolePermissions(roleId)
              .buildSigned(myAccountKeypair);

          QueryResponse response = irohaApi.query(query);

          if (response.hasErrorResponse()) {
            throw new ErrorResponseException(response.getErrorResponse());
          }

          System.out.println(response.getRolePermissionsResponse().toString());
        } else if (choice == 13) { // GetPeers
          System.out.println(queryApi.getPeers().toString());
        } else {
          System.err.println("Invalid option");
          // No query was performed
          counter--;
        }
      } catch (NumberFormatException err) {
        System.err.println("Invalid input: " + err.getMessage());
      } catch (ErrorResponseException err) {
        System.err.println("Error response: " + err.getMessage());
      } catch (ValidationException err) {
        System.err.println("Error response: " + err.getMessage());
      }
    }
  }

  private static void sendTransaction(IrohaAPI irohaApi, String myAccountId, KeyPair myAccountKeypair) {
    Scanner in = new Scanner(System.in);

    TransactionStatusObserver txObserver = TransactionStatusObserver.builder()
        .onTransactionFailed(tx -> System.out.println(String.format(
                "Transaction %s failed: %s",
                tx.getTxHash(),
                tx.getErrOrCmdName()
            ))
        )
        .onTransactionCommitted(tx -> System.out.println(String.format(
                "Transaction %s committed",
                tx.getTxHash()
            ))
        )
        .onError(err -> System.out.println("Error: " + err))
        .build();

    while (true) {
      System.out.println("Choose an option:");
      System.out.println("0. Back");
      System.out.println("1. AddAssetQuantity (increase a given asset's amount for transaction's creator)");
      System.out.println("2. AddPeer (add a new peer to the Iroha network)");
      System.out.println("3. AddSignatory (add a new signatory to an account)");
      System.out.println("4. AppendRole (promote an account to a given role)");
      System.out.println("5. CreateAccount (create a new account)");
      System.out.println("6. CreateAsset (create a new asset)");
      System.out.println("7. CreateDomain (create a new domain)");
      System.out.println("8. CreateRole (create a new role)");
      System.out.println("9. DetachRole (remove a role from an account)");
      System.out.println("10. GrantPermission (grant a permission to an account)");
      System.out.println("11. RemovePeer (remove a peer from the Iroha network)");
      System.out.println("12. RemoveSignatory (remove a signatory from an account)");
      System.out.println("13. RevokePermission (revoke a permission to an account)");
      System.out.println("14. SetAccountDetail (set the details of an account)");
      System.out.println("15. SetAccountQuorum (set the quorum of an account)");
      System.out.println("16. SubtractAssetQuantity (decrease a given asset's amount for transaction's creator)");
      System.out.println("17. TransferAsset (transfer a given asset amount from the source to the destination account)");

      System.out.print(">>> ");
      try {
        int choice = Integer.parseInt(in.nextLine());
        if (choice == 0) { // Back
          return;
        } else if (choice == 1) { // AddAssetQuantity
          System.out.print("asset_id: ");
          String assetId = in.nextLine();

          System.out.print("amount: ");
          String amount = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .addAssetQuantity(assetId, amount)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 2) { // AddPeer
          System.out.print("address: ");
          String address = in.nextLine();

          System.out.print("peer_key: ");
          String peerKey = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .addPeer(address, parseHexBinary(peerKey))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 3) { // AddSignatory
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("public_key: ");
          String publicKey = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .addSignatory(accountId, parseHexBinary(publicKey))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 4) { // AppendRole
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("role_name: ");
          String roleName = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .appendRole(accountId, roleName)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 5) { // CreateAccount
          System.out.print("account_name: ");
          String accountName = in.nextLine();

          System.out.print("domain_id: ");
          String domainId = in.nextLine();

          System.out.print("public_key: ");
          String publicKey = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .createAccount(accountName, domainId, parseHexBinary(publicKey))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 6) { // CreateAsset
          System.out.print("asset_name: ");
          String assetName = in.nextLine();

          System.out.print("domain_id: ");
          String domainId = in.nextLine();

          System.out.print("precision: ");
          int precision = Integer.parseInt(in.nextLine());

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .createAsset(assetName, domainId, precision)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 7) { // CreateDomain
          System.out.print("domain_id: ");
          String domainId = in.nextLine();

          System.out.print("default_role: ");
          String defaultRole = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .createDomain(domainId, defaultRole)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 8) { // CreateRole
          System.out.print("role_name: ");
          String roleName = in.nextLine();

          System.out.print("role_permissions (comma-separated permission ids): ");
          String rolePermissions = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .createRole(
                  roleName,
                  Arrays.asList(rolePermissions.split(","))
                      .stream()
                      .map(id -> RolePermission.forNumber(Integer.parseInt(id)))
                      .collect(Collectors.toList())
              )
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 9) { // DetachRole
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("role_name: ");
          String roleName = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .detachRole(accountId, roleName)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 10) { // GrantPermission
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("permission (grantable permission id): ");
          int permission = Integer.parseInt(in.nextLine());

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .grantPermission(accountId, GrantablePermission.forNumber(permission))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 11) { // RemovePeer
          System.out.print("public_key: ");
          String publicKey = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .removePeer(parseHexBinary(publicKey))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 12) { // RemoveSignatory
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("public_key: ");
          String publicKey = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .removeSignatory(accountId, parseHexBinary(publicKey))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 13) { // RevokePermission
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("permission (grantable permission id): ");
          int permission = Integer.parseInt(in.nextLine());

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .revokePermission(accountId, GrantablePermission.forNumber(permission))
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 14) { // SetAccountDetail
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("key: ");
          String key = in.nextLine();

          System.out.print("value: ");
          String value = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .setAccountDetail(accountId, key, value)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 15) { // SetAccountQuorum
          System.out.print("account_id: ");
          String accountId = in.nextLine();

          System.out.print("quorum: ");
          int quorum = Integer.parseInt(in.nextLine());

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .setAccountQuorum(accountId, quorum)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 16) { // SubtractAssetQuantity
          System.out.print("asset_id: ");
          String assetId = in.nextLine();

          System.out.print("amount: ");
          String amount = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .subtractAssetQuantity(assetId, amount)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else if (choice == 17) { // TransferAsset
          System.out.print("src_account_id: ");
          String srcAccountId = in.nextLine();

          System.out.print("dest_account_id: ");
          String destAccountId = in.nextLine();

          System.out.print("asset_id: ");
          String assetId = in.nextLine();

          System.out.print("description: ");
          String description = in.nextLine();

          System.out.print("amount: ");
          String amount = in.nextLine();

          TransactionOuterClass.Transaction tx = Transaction.builder(myAccountId)
              .transferAsset(srcAccountId, destAccountId, assetId, description, amount)
              .sign(myAccountKeypair)
              .build();

          irohaApi.transaction(tx)
              .blockingSubscribe(txObserver);
        } else {
          System.err.println("Invalid option");
        }
      } catch (NumberFormatException err) {
        System.err.println("Invalid input: " + err.getMessage());
      } catch (ErrorResponseException err) {
        System.err.println("Error response: " + err.getMessage());
      } catch (ValidationException err) {
        System.err.println("Error response: " + err.getMessage());
      }
    }
  }
}
