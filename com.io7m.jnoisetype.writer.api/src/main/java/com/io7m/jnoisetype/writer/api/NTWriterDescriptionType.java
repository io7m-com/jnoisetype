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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnoisetype.api.NTInfo;
import org.immutables.value.Value;

import java.util.SortedMap;

/**
 * An immutable description of a file to be written.
 */

public interface NTWriterDescriptionType
{
  /**
   * @return Information about the file
   */

  NTInfo info();

  /**
   * @return The samples that will be written
   */

  SortedMap<Integer, NTSampleWriterDescription> samples();

  /**
   * @return The instruments that will be written
   */

  SortedMap<Integer, NTInstrumentWriterDescription> instruments();

  /**
   * @return The presets that will be written
   */

  SortedMap<Integer, NTPresetWriterDescription> presets();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    this.samples().forEach((index, description) -> {
      Preconditions.checkPreconditionI(
        index.intValue(),
        description.sampleIndex() == index.intValue(),
        x -> "Sample index must match description index " + description.sampleIndex());
    });

    this.instruments().forEach((index, description) -> {
      Preconditions.checkPreconditionI(
        index.intValue(),
        description.instrumentIndex() == index.intValue(),
        x -> "Instrument index must match description index " + description.instrumentIndex());
    });

    this.presets().forEach((index, description) -> {
      Preconditions.checkPreconditionI(
        index.intValue(),
        description.presetIndex() == index.intValue(),
        x -> "Preset index must match description index " + description.presetIndex());
    });
  }
}
