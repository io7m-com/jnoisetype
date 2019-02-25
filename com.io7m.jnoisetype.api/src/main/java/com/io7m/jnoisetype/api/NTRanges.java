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

import com.io7m.jranges.RangeInclusiveI;

/**
 * Various ranges defined by the SountFontⓡ specification.
 */

public final class NTRanges
{
  /**
   * The length range of short strings. The SountFontⓡ specification defines many strings as "an
   * ASCII string of 256 or fewer bytes including one or two terminators of value zero, so as to
   * make the total byte count even".
   */

  public static final RangeInclusiveI SHORT_STRING_LENGTH_RANGE =
    RangeInclusiveI.of(0, 255);

  /**
   * The length range of long strings. The SountFontⓡ specification defines many strings as "an
   * ASCII string of 65535 or fewer bytes including one or two terminators of value zero, so as to
   * make the total byte count even".
   */

  public static final RangeInclusiveI LONG_STRING_LENGTH_RANGE =
    RangeInclusiveI.of(0, 65535);

  /**
   * The range of valid value for version numbers. Numbers are encoded as 16-bit unsigned
   * integers.
   */

  public static final RangeInclusiveI VERSION_RANGE =
    RangeInclusiveI.of(0, 65535);

  /**
   * The length range of sample names.
   */

  public static final RangeInclusiveI SAMPLE_NAME_LENGTH_RANGE =
    RangeInclusiveI.of(0, 20);

  /**
   * The length range of preset names.
   */

  public static final RangeInclusiveI PRESET_NAME_LENGTH_RANGE =
    RangeInclusiveI.of(0, 20);

  /**
   * The length range of instrument names.
   */

  public static final RangeInclusiveI INSTRUMENT_NAME_LENGTH_RANGE =
    RangeInclusiveI.of(0, 20);

  private NTRanges()
  {

  }
}
