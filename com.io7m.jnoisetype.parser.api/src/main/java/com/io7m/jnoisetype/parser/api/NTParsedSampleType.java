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

package com.io7m.jnoisetype.parser.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jnoisetype.api.NTNamedType;
import com.io7m.jnoisetype.api.NTSampleDescription;
import com.io7m.jranges.RangeHalfOpenL;
import org.immutables.value.Value;

/**
 * @see "SoundFont® Technical Specification 2.04, §7.10 The SHDR subchunk"
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTParsedSampleType extends NTNamedType, NTParsedElementType
{
  /**
   * @return The sample description
   */

  NTSampleDescription description();

  /**
   * @return The byte range of the sample data within the parsed file
   */

  RangeHalfOpenL dataByteRange();

  /**
   * @return The byte range of the sample data within the parsed file including the specification-mandated zero values
   */

  default RangeHalfOpenL dataByteRangeIncludingPadding()
  {
    final var range = this.dataByteRange();
    return RangeHalfOpenL.of(range.lower(), Math.addExact(range.upper(), 46L * 2L));
  }

  @Override
  default String nameText()
  {
    return this.description().nameText();
  }
}
