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

import com.io7m.jnoisetype.api.NTLongString;
import com.io7m.jranges.RangeCheckException;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class NTLongStringTest
{
  @Test
  public void testTooLong()
  {
    final var sb = new StringBuilder(70000);
    for (var index = 0; index < 70000; ++index) {
      sb.append('x');
    }

    Assertions.assertThrows(
      RangeCheckException.class,
      () -> NTLongString.builder().setValue(sb.toString()).build());

    Assertions.assertThrows(
      RangeCheckException.class,
      () -> NTLongString.of(sb.toString()));
  }

  @Test
  public void testNull()
  {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> NTLongString.of("\0"));
  }

  @Property
  public void testOrdering(
    final @ForAll @AlphaNumericType @StringLength(min = 0, max = 1000) String s0,
    final @ForAll @AlphaNumericType @StringLength(min = 0, max = 1000) String s1)
  {
    Assertions.assertEquals(
      s0.compareTo(s1),
      NTLongString.of(s0).compareTo(NTLongString.of(s1)),
      "Ordering is correct");
    Assertions.assertEquals(
      s1.compareTo(s0),
      NTLongString.of(s1).compareTo(NTLongString.of(s0)),
      "Ordering is correct");
  }
}
