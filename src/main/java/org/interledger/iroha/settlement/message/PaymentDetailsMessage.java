package org.interledger.iroha.settlement.message;

import com.google.api.client.util.Key;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsMessage {
  @Key
  private String irohaAccountId;
}
