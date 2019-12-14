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

import com.io7m.jnoisetype.api.NTBankIndex;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTSampleName;

/**
 * A builder of writer descriptions for SoundFontⓡ files.
 */

public interface NTBuilderType
{
  /**
   * @return An immutable description of a file to be written
   */

  NTWriterDescriptionType build();

  /**
   * Set the info for the font.
   *
   * @param info The info
   *
   * @return The current builder
   */

  NTBuilderType setInfo(NTInfo info);

  /**
   * @return The current info for the font
   */

  NTInfo info();

  /**
   * Add a new sample.
   *
   * @param name The sample name
   *
   * @return A new sample builder
   */

  NTSampleBuilderType addSample(NTSampleName name);

  /**
   * Add a new sample.
   *
   * @param name The sample name
   *
   * @return A new sample builder
   */

  default NTSampleBuilderType addSample(
    final String name)
  {
    return this.addSample(NTSampleName.of(name));
  }

  /**
   * Add a new instrument.
   *
   * @param name The instrument name
   *
   * @return A new instrument builder
   */

  NTInstrumentBuilderType addInstrument(
    NTInstrumentName name);

  /**
   * Add a new instrument.
   *
   * @param name The instrument name
   *
   * @return A new instrument builder
   */

  default NTInstrumentBuilderType addInstrument(
    final String name)
  {
    return this.addInstrument(
      NTInstrumentName.of(name));
  }

  /**
   * Add a new preset.
   *
   * @param bank The bank to which the preset belongs
   * @param name The preset name
   *
   * @return A new preset builder
   */

  NTPresetBuilderType addPreset(
    NTBankIndex bank,
    NTPresetName name);

  /**
   * Add a new preset.
   *
   * @param bank The bank to which the preset belongs
   * @param name The preset name
   *
   * @return A new preset builder
   */

  default NTPresetBuilderType addPreset(
    final NTBankIndex bank,
    final String name)
  {
    return this.addPreset(bank, NTPresetName.of(name));
  }
}
