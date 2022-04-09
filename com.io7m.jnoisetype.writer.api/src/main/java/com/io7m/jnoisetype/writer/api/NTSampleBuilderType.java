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

package com.io7m.jnoisetype.writer.api;

import com.io7m.jnoisetype.api.NTPitch;
import com.io7m.jnoisetype.api.NTSampleIndex;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;

/**
 * The type of sample builders.
 */

public interface NTSampleBuilderType
{
  /**
   * @return The (unique) name of the sample being built
   */

  NTSampleName name();

  /**
   * @return The unique, monotonically increasing sample index number
   */

  NTSampleIndex sampleIndex();

  /**
   * @return The length of the sample in sample values
   */

  long sampleCount();

  /**
   * Set the length of the sample in sample values.
   *
   * @param count The number of samples
   *
   * @return The current builder
   */

  NTSampleBuilderType setSampleCount(long count);

  /**
   * @return The current sample rate
   */

  int sampleRate();

  /**
   * Set the sampling rate in hz.
   *
   * @param rate The sample rate
   *
   * @return The current builder
   */

  NTSampleBuilderType setSampleRate(int rate);

  /**
   * @return The current sample type
   */

  NTSampleKind kind();

  /**
   * Set the type of the sample data.
   *
   * @param kind The data type
   *
   * @return The current builder
   */

  NTSampleBuilderType setKind(NTSampleKind kind);

  /**
   * @return The sample to which this sample is linked
   *
   * @see "SoundFontⓡ Technical Specification 2.04, §7.10 The SHDR sub-chunk"
   */

  default NTSampleIndex linked()
  {
    return NTSampleIndex.of(0);
  }

  /**
   * Set the sample to which this sample is linked
   *
   * @param index The sample index
   *
   * @return The current builder
   *
   * @see "SoundFontⓡ Technical Specification 2.04, §7.10 The SHDR sub-chunk"
   */

  NTSampleBuilderType setLinked(NTSampleIndex index);

  /**
   * @return The current start point of the loop
   */

  long loopStart();

  /**
   * Set the start position of the loop of the sample in sample values. This value is relative to
   * the start of the sample ({@code 0} being the first sample value).
   *
   * @param start The start point
   *
   * @return The current builder
   */

  NTSampleBuilderType setLoopStart(long start);

  /**
   * @return The current end point of the loop
   */

  long loopEnd();

  /**
   * Set the end position of the loop of the sample in sample values. This value is relative to the
   * start of the sample ({@code 0} being the first sample value).
   *
   * @param end The end point
   *
   * @return The current builder
   */

  NTSampleBuilderType setLoopEnd(long end);

  /**
   * @return The current sample original pitch
   */

  NTPitch originalPitch();

  /**
   * Set the original pitch of the sample as a MIDI key value. Middle C is defined to be 60.
   *
   * @param pitch The pitch
   *
   * @return The current builder
   */

  NTSampleBuilderType setOriginalPitch(NTPitch pitch);

  /**
   * @return The current sample pitch correction
   */

  int pitchCorrection();

  /**
   * Set the amount of pitch correction for the sample.
   *
   * @param pitch The pitch correction
   *
   * @return The current builder
   */

  NTSampleBuilderType setPitchCorrection(int pitch);

  /**
   * Set the data writer for the sample. The writer is expected to produce exactly {@link
   * #sampleCount()} sample values upon request. Writing too many values will cause an exception to
   * be raised. Writing too few values will result in zero-padding.
   *
   * @param writer The writer
   *
   * @return The current builder
   */

  NTSampleBuilderType setDataWriter(NTSampleDataWriterType writer);
}
