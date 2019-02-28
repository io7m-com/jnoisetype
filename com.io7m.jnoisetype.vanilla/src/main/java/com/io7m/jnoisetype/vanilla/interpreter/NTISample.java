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

import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTSampleDescription;
import com.io7m.jnoisetype.api.NTSampleType;
import com.io7m.jnoisetype.parser.api.NTParsedSample;

import java.util.Objects;

final class NTISample implements NTSampleType
{
  private final NTIFont font;
  private final NTParsedSample sample;

  NTISample(
    final NTIFont in_font,
    final NTParsedSample in_sample)
  {
    this.font = Objects.requireNonNull(in_font, "font");
    this.sample = Objects.requireNonNull(in_sample, "sample");
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
      return false;
    }
    final var sample1 = (NTISample) o;
    return Objects.equals(this.sample, sample1.sample);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.sample);
  }

  @Override
  public String toString()
  {
    final var description = this.sample.description();
    return new StringBuilder(64)
      .append("[Sample '")
      .append(description.name().value())
      .append("' ")
      .append(description.kind())
      .append(']')
      .toString();
  }

  @Override
  public NTFontType font()
  {
    return this.font;
  }

  @Override
  public NTSampleDescription description()
  {
    return this.sample.description();
  }
}
