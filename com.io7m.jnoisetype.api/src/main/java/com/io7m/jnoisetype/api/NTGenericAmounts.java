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

/**
 * Functions to construct generic amounts.
 */

public final class NTGenericAmounts
{
  private NTGenericAmounts()
  {

  }

  /**
   * Construct an amount from the given pair of values.
   *
   * @param lo The low value
   * @param hi The high value
   *
   * @return A generic amount
   */

  public static NTGenericAmount ofPair(
    final byte lo,
    final byte hi)
  {
    final var msb = ((int) hi << 8);
    final var lsb = (int) lo & 0xff;
    return NTGenericAmount.of(msb | lsb);
  }
}
