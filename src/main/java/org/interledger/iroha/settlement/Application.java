package org.interledger.iroha.settlement;

import org.interledger.iroha.settlement.config.DefaultArgumentValues;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Application {
  /**
   * <p>Entrypoint for the application. Validates passed arguments and starts the server.</p>
   */
  public static void main(String[] args) {
    // Make sure all required arguments are provided before starting
    handleArgs(new DefaultApplicationArguments(args));

    SpringApplication.run(Application.class, args);
  }

  private static void handleArgs(ApplicationArguments args) {
    // Show help if the user requested
    if (args.containsOption("help")) {
      displayHelp();
      System.exit(0);
    }

    boolean success = true;

    // Make sure all required arguments are provided
    List<String> requiredOptions = Arrays.asList("iroha-account-id", "keypair-name", "asset-id");
    for (String option : requiredOptions) {
      if (!args.containsOption(option)) {
        System.err.println(String.format("Option %s is required (e.g. --%s=<value>)", option, option));
        success = false;
      }
    }

    // Validate URLs
    List<String> urlOptions = Arrays.asList("connector-url", "torii-url", "redis-url");
    for (String option : urlOptions) {
      if (args.containsOption(option)) {
        String url = args.getOptionValues(option).get(0);
        try {
          new URI(url);
        } catch (URISyntaxException err) {
          System.err.println(String.format("Invalid URL for --%s", option));
          success = false;
        }
      }
    }

    // Validate ports
    List<String> portOptions = Arrays.asList("bind-port");
    for (String option : portOptions) {
      if (args.containsOption(option)) {
        String strPort = args.getOptionValues(option).get(0);
        try {
          int port = Integer.parseInt(strPort);
          if (port < 0 || port > 0xFFF) {
            throw new NumberFormatException();
          }
        } catch (NumberFormatException err) {
          System.err.println(String.format("Invalid port for --%s", option));
          success = false;
        }
      }
    }

    // Validate numbers
    List<String> numberOptions = Arrays.asList("asset-scale");
    for (String option : numberOptions) {
      if (args.containsOption(option)) {
        String strNumber = args.getOptionValues(option).get(0);
        try {
          Integer.parseInt(strNumber);
        } catch (NumberFormatException err) {
          System.err.println(String.format("Invalid number for --%s", option));
          success = false;
        }
      }
    }

    // Show help if the provided arguments are wrong
    if (!success) {
      displayHelp();
      System.exit(1);
    }
  }

  private static void displayHelp() {
    System.out.println("ilp-settlement-iroha [OPTION]...");
    System.out.println("Interledger settlement engine for Hyperledger Iroha");
    System.out.println("");
    System.out.println("  --help               Display this help and exit");
    System.out.println("  --bind-port          Port to listen to settlement requests");
    System.out.println("                       (defaults to " + DefaultArgumentValues.BIND_PORT + ")");
    System.out.println("  --connector-url      Connector settlement API endpoint");
    System.out.println("                       (defaults to " + DefaultArgumentValues.CONNECTOR_URL + ")");
    System.out.println("  --torii-url          Iroha Torii endpoint");
    System.out.println("                       (defaults to " + DefaultArgumentValues.TORII_URL + ")");
    System.out.println("  --redis-url          Redis endpoint for storage");
    System.out.println("                       (defaults to " + DefaultArgumentValues.REDIS_URL + ")");
    System.out.println("  --iroha-account-id   Iroha account id");
    System.out.println("                       (required)");
    System.out.println("  --keypair-name       Iroha account keypair files name (.pub and .priv)");
    System.out.println("                       (required)");
    System.out.println("  --asset-id           The asset to be used for payments");
    System.out.println("                       (required)");
    System.out.println("  --asset-scale        The asset scale to be used for payments");
    System.out.println("                       (defaults to " + DefaultArgumentValues.ASSET_SCALE + ")");
    System.out.println("");
    System.out.println("Please report issues at https://github.com/georgeroman/ilp-iroha-settlement/issues");
  }
}
