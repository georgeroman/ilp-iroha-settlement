package org.interledger.settlement.iroha;

import iroha.protocol.Endpoint.ToriiResponse;

public class IrohaException extends Exception {
  public IrohaException(String errorMessage) {
    super(errorMessage);
  }

  public IrohaException(Throwable error) {
    super(error);
  }

  public IrohaException(ToriiResponse irohaResponse) {
    super(irohaResponse.getErrOrCmdName());
  }
}
