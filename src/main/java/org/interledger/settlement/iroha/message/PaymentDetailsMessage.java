package org.interledger.settlement.iroha.message;

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
  // We need @Key for Google's http client's serialization/deserialization
  @Key
  private String irohaAccountId;
}
