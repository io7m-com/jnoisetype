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

package com.io7m.jnoisetype.writer.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jnoisetype.api.NTPitch;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.Ranges;
import org.immutables.value.Value;

/**
 * A description of a sample.
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTSampleBuilderDescriptionType
{
  /**
   * @return The (unique) name of the sample being built
   */

  NTSampleName name();

  /**
   * @return The length of the sample in sample values
   *
   * @see NTSampleBuilderType#setSampleCount(long)
   */

  long sampleCount();

  /**
   * @return The current sample rate
   *
   * @see NTSampleBuilderType#setSampleRate(int)
   */

  int sampleRate();

  /**
   * @return The current sample type
   *
   * @see NTSampleBuilderType#setKind(NTSampleKind)
   */

  NTSampleKind kind();

  /**
   * @return The current start point of the loop
   *
   * @see NTSampleBuilderType#setLoopStart(long)
   */

  long loopStart();

  /**
   * @return The current end point of the loop
   *
   * @see NTSampleBuilderType#setLoopEnd(long)
   */

  long loopEnd();

  /**
   * @return The current sample original pitch
   *
   * @see NTSampleBuilderType#setOriginalPitch(NTPitch)
   */

  NTPitch originalPitch();

  /**
   * @return The current sample pitch correction
   *
   * @see NTSampleBuilderType#setPitchCorrection(int)
   */

  int pitchCorrection();

  /**
   * @return The data writer function
   *
   * @see NTSampleBuilderType#setDataWriter(NTSampleDataWriterType)
   */

  NTSampleDataWriterType dataWriter();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInLong(
      this.sampleCount(),
      "Sample count",
      Ranges.NATURAL_LONG,
      "Valid sample counts");

    RangeCheck.checkIncludedInLong(
      this.loopStart(),
      "Loop start",
      Ranges.NATURAL_LONG,
      "Valid loop starts");

    RangeCheck.checkIncludedInLong(
      this.loopEnd(),
      "Loop end",
      Ranges.NATURAL_LONG,
      "Valid loop ends");

    RangeCheck.checkLessEqualLong(
      this.loopStart(),
      "Loop start",
      this.sampleCount(),
      "Sample count");

    RangeCheck.checkLessEqualLong(
      this.loopEnd(),
      "Loop end",
      this.sampleCount(),
      "Sample count");

    RangeCheck.checkLessEqualLong(
      this.loopStart(),
      "Loop start",
      this.loopEnd(),
      "Loop end");
  }
}
