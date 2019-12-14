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
import com.io7m.jnoisetype.api.NTSampleIndex;
import com.io7m.jranges.RangeCheck;
import org.immutables.value.Value;

/**
 * The description of a sample consumed by a writer.
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTSampleWriterDescriptionType
{
  /**
   * @return The description provided to the sample builder
   */

  NTSampleBuilderDescription description();

  /**
   * @return The unique index of the sample, monotonically increasing and starting at {@code 0}
   */

  NTSampleIndex sampleIndex();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first data point of this sample.
   */

  long sampleAbsoluteStart();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first of the set of 46 zero valued data points following this sample.
   */

  long sampleAbsoluteEnd();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first data point in the loop of this sample.
   */

  long sampleAbsoluteLoopStart();

  /**
   * @return The index, in sample data points, from the beginning of the sample data field to the
   * first data point following the loop of this sample. Note that this is the data point
   * “equivalent to” the first loop data point, and that to produce portable artifact free loops,
   * the eight proximal data points surrounding both the sampleAbsoluteLoopStart and
   * sampleAbsoluteLoopEnd points should be identical.
   */

  long sampleAbsoluteLoopEnd();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkLessEqualLong(
      this.sampleAbsoluteStart(),
      "Sample start",
      this.sampleAbsoluteEnd(),
      "Sample end");

    RangeCheck.checkLessEqualLong(
      this.sampleAbsoluteLoopStart(),
      "Loop start",
      this.sampleAbsoluteLoopEnd(),
      "Loop end");

    RangeCheck.checkLessEqualLong(
      this.sampleAbsoluteLoopStart(),
      "Loop start",
      this.sampleAbsoluteEnd(),
      "Sample end");

    RangeCheck.checkLessEqualLong(
      this.sampleAbsoluteLoopEnd(),
      "Loop end",
      this.sampleAbsoluteEnd(),
      "Sample end");
  }
}
