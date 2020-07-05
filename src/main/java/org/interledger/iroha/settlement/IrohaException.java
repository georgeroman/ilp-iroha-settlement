package org.interledger.iroha.settlement;

public class IrohaException extends Exception {
  public IrohaException(String errorMessage) {
    super(errorMessage);
  }

  public IrohaException(Throwable error) {
    super(error);
  }
}
