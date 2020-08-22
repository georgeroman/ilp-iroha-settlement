package org.interledger.settlement.iroha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContainerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
  @Value("${bind-port:" + DefaultArgumentValues.BIND_PORT + "}")
  private String bindPort;

  @Override
  public void customize(ConfigurableWebServerFactory factory) {
    factory.setPort(Integer.parseInt(this.bindPort));
  }
}
