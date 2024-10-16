/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.util.Comparator;

/**
 * A version number.
 *
 * @see "SoundFont® Technical Specification 2.04, §5.1, sfVersionTag"
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTVersionType extends Comparable<NTVersionType>
{
  /**
   * @return The major version number
   */

  @Value.Parameter
  int major();

  /**
   * @return The minor version number
   */

  @Value.Parameter
  int minor();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.major(),
      "Major version",
      NTRanges.VERSION_RANGE,
      "Valid major version range");

    RangeCheck.checkIncludedInInteger(
      this.minor(),
      "Minor version",
      NTRanges.VERSION_RANGE,
      "Valid minor version range");
  }

  @Override
  default int compareTo(final NTVersionType other)
  {
    return Comparator.comparingInt(NTVersionType::major)
      .thenComparingInt(NTVersionType::minor)
      .compare(this, other);
  }
}
