package org.interledger.iroha.settlement;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
    if (args.containsOption("help")) {
      displayHelp();
      System.exit(0);
    }

    boolean success = true;

    List<String> requiredOptions = Arrays.asList("account-id", "keypair-name");
    for (String option : requiredOptions) {
      if (!args.containsOption(option) || args.getOptionValues(option).size() != 1) {
        System.err.println(
            String.format("Option %s is required and should take a single value (e.g. --%s=<value>)", option, option)
        );
        success = false;
      }
    }

    if (!success) {
      displayHelp();
      System.exit(1);
    }
  }

  private static void displayHelp() {
    System.out.println("ilp-settlement-iroha [OPTION]...");
    System.out.println("Interledger settlement engine for Hyperledger Iroha");
    System.out.println("");
    System.out.println("  --help             Display this help and exit");
    System.out.println("  --bind-port        Port to listen to settlement requests");
    System.out.println("                     (defaults to 3000)");
    System.out.println("  --connector-url    Connector settlement API endpoint");
    System.out.println("                     (defaults to http://127.0.0.1:7771)");
    System.out.println("  --torii-url        Iroha Torii endpoint");
    System.out.println("                     (defaults to http://127.0.0.1:50051)");
    System.out.println("  --account-id       Iroha account id");
    System.out.println("                     (required)");
    System.out.println("  --keypair-name     Iroha account keypair files name (.pub and .priv)");
    System.out.println("                     (required)");
    System.out.println("");
    System.out.println("Please report issues at https://github.com/georgeroman/ilp-iroha-settlement/issues");
  }
}
