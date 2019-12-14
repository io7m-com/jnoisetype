/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jnoisetype.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jranges.RangeCheck;
import org.immutables.value.Value;

/**
 * The valid values for bank indices.
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTBankIndexType extends Comparable<NTBankIndexType>
{
  /**
   * @return The raw value in the range {@code [0, 0xffff]}
   */

  @Value.Parameter
  int value();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.value(),
      "Bank    index   value",
      NTRanges.BANK_INDEX_RANGE,
      "Valid bank index values");
  }

  /**
   * @return The value as an unsigned 16-bit integer
   */

  default char asUnsigned16()
  {
    return (char) this.value();
  }

  @Override
  default int compareTo(final NTBankIndexType other)
  {
    return Integer.compareUnsigned(this.value(), other.value());
  }
}
