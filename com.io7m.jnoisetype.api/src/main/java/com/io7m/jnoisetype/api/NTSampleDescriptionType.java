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
 * Data that describes a sample.
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTSampleDescriptionType extends NTNamedType
{
  /**
   * @return The name of the sample
   */

  NTSampleName name();

  @Override
  default String nameText()
  {
    return this.name().value();
  }

  /**
   * @return The kind of the sample
   */

  NTSampleKind kind();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first data point of this sample
   */

  long start();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first of the set of 46 zero valued data points following this sample
   */

  long end();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first data point in the loop of this sample
   */

  long loopStart();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first data point following the loop of this sample. Note that this is the data point
   * “equivalent to” the first loop data point, and that to produce portable artifact free loops,
   * the eight proximal data points surrounding both the Startloop and Endloop points should be
   * identical.
   */

  long loopEnd();

  /**
   * @return The sample rate in hz
   */

  int sampleRate();

  /**
   * @return The original pitch of the sample
   */

  NTPitch originalPitch();

  /**
   * @return The pitch correction value for the sample
   */

  int pitchCorrection();

  /**
   * @return The sample link
   */

  @Value.Default
  default int sampleLink()
  {
    return 0;
  }
}
