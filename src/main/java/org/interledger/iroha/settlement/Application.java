package org.interledger.iroha.settlement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Application implements ApplicationRunner {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (args.containsOption("-h") || args.containsOption("--help")) {
      this.displayHelp();
      System.exit(0);
    }

    List<String> requiredOptions = Arrays.asList("account-id", "private-key");
    for (String option : requiredOptions) {
      if (!args.containsOption(option) || args.getOptionValues(option).size() != 1) {
        System.err.println(
            String.format("Option %s is required and should take a single value (e.g. --%s=<value>)", option, option)
        );

        this.displayHelp();
        System.exit(1);
      }
    }
  }

  private void displayHelp() {
    System.out.println("ilp-settlement-iroha [OPTION]...");
    System.out.println("Interledger settlement engine for Hyperledger Iroha");
    System.out.println("");
    System.out.println("-h, --help           Display this help and exit");
    System.out.println("    --bind-port      Port to listen to settlement requests");
    System.out.println("                     (defaults to 3000)");
    System.out.println("    --connector-url  Connector settlement API endpoint");
    System.out.println("                     (defaults to 127.0.0.1:7771)");
    System.out.println("    --account-id     Iroha account id");
    System.out.println("                     (required)");
    System.out.println("    --private-key    Iroha account private key");
    System.out.println("                     (required)");
    System.out.println("");
    System.out.println("Please report issues at https://github.com/georgeroman/ilp-iroha-settlement/issues");
  }
}
