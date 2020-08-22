package org.interledger.settlement.iroha;

import static java.util.AbstractMap.SimpleImmutableEntry;

import java.math.BigDecimal;
import java.util.Map;

public class Util {
  /**
   * <p>Converts a currency amount from a precision to another, taking into account any precision loss.</p>
   *
   * @param amount    A {@link BigDecimal} denoting the currency amount to be scaled.
   *
   * @param fromScale The initial precision of the amount.
   *
   * @param toScale   The precision to convert the amount to.
   *
   * @return A {@link java.util.Map#Entry} holding the scaled amount as key and the precision loss as value.
   */
  public static Map.Entry<BigDecimal, BigDecimal> scaleWithPrecisionLoss(
      BigDecimal amount,
      int fromScale,
      int toScale
  ) {
    BigDecimal scaled = amount.setScale(toScale);
    if (toScale < fromScale) {
      // Downscaling can result in loss of precision
      BigDecimal upscaled = scaled.setScale(fromScale);
      BigDecimal precisionLoss = upscaled.compareTo(amount) < 0 ? amount.subtract(upscaled) : BigDecimal.ZERO;
      return new SimpleImmutableEntry(scaled, precisionLoss);
    } else {
      return new SimpleImmutableEntry(scaled, BigDecimal.ZERO);
    }
  }
}
