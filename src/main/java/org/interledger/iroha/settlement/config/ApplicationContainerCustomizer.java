package org.interledger.iroha.settlement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContainerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${bind-port:3000}")
  private String port;

  @Override
  public void customize(ConfigurableWebServerFactory factory) {
    try {
      factory.setPort(Integer.parseInt(this.port));
    } catch (NumberFormatException err) {
      this.logger.error("Invalid bind-port: {}", err.getMessage());
      System.exit(1);
    }
  }
}
