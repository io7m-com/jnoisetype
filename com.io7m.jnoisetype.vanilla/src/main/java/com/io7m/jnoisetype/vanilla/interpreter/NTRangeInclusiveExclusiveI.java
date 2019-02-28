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

package com.io7m.jnoisetype.vanilla.interpreter;

import com.io7m.jranges.RangeCheck;

/**
 * A range with an inclusive lower bound and an exclusive upper bound.
 */

final class NTRangeInclusiveExclusiveI
{
  private final int lo;
  private final int hi;

  NTRangeInclusiveExclusiveI(
    final int in_lower,
    final int in_upper)
  {
    RangeCheck.checkLessEqualInteger(
      in_lower,
      "Lower range",
      in_upper,
      "Upper range");

    this.lo = in_lower;
    this.hi = in_upper;
  }

  int lower()
  {
    return this.lo;
  }

  int upper()
  {
    return this.hi;
  }

  int interval()
  {
    return this.hi - this.lo;
  }
}
