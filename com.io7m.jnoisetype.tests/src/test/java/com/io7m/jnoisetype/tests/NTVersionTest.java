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

package com.io7m.jnoisetype.tests;

import com.io7m.jnoisetype.api.NTGeneratorIndex;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jranges.RangeCheckException;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class NTVersionTest
{
  @Test
  public void testOutOfRange0()
  {
    Assertions.assertThrows(RangeCheckException.class, () -> {
      NTVersion.of(70000, 0);
    });
  }

  @Test
  public void testOutOfRange1()
  {
    Assertions.assertThrows(RangeCheckException.class, () -> {
      NTVersion.of(0, 70000);
    });
  }

  @Property
  public void testOrdering0(
    final @ForAll @IntRange(min = 0, max = 0xffff) int s0,
    final @ForAll @IntRange(min = 0, max = 0xffff) int s1)
  {
    Assertions.assertEquals(
      Integer.compareUnsigned(s0, s1),
      NTVersion.of(s0, 0).compareTo(NTVersion.of(s1, 0)),
      "Ordering is correct");
  }

  @Property
  public void testOrdering1(
    final @ForAll @IntRange(min = 0, max = 0xffff) int s0,
    final @ForAll @IntRange(min = 0, max = 0xffff) int s1)
  {
    Assertions.assertEquals(
      Integer.compareUnsigned(s0, s1),
      NTVersion.of(0, s0).compareTo(NTVersion.of(0, s1)),
      "Ordering is correct");
  }
}
