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
import org.immutables.value.Value;

/**
 * @see "SoundFontⓡ Technical Specification 2.04, §7.5 The PGEN subchunk"
 */

@NTNativeType(name = "genAmountType")
@ImmutablesStyleType
@Value.Immutable
public interface NTGenericAmountType
{
  /**
   * @return The raw value in the range {@code [0, 0xffff]}
   */

  @Value.Parameter
  int value();

  /**
   * Treat the amount as if it were two separate unsigned bytes, and return the low byte
   *
   * @return The unsigned byte value
   */

  default int asUnsignedBytesLow()
  {
    final var x = this.value();
    return x & 0x00ff;
  }

  /**
   * Treat the amount as if it were two separate unsigned bytes, and return the high byte
   *
   * @return The unsigned byte value
   */

  default int asUnsignedBytesHigh()
  {
    final var x = this.value();
    final var m = x & 0xff00;
    return m >> 8;
  }

  /**
   * Treat the amount as if it was an unsigned 16-bit value, and return it.
   *
   * @return The unsigned 16-bit value
   */

  default int asUnsigned16()
  {
    final var x = this.value();
    return (x & 0xffff);
  }

  /**
   * Treat the amount as if it was a signed 16-bit value, and return it.
   *
   * @return The signed 16-bit value
   */

  default int asSigned16()
  {
    final var x = this.value();
    final var sx = (short) x;
    return (int) sx;
  }
}
