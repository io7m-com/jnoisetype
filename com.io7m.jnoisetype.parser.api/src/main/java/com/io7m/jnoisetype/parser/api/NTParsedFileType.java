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
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTSource;
import org.immutables.value.Value;

import java.util.List;

/**
 * A parsed SoundFont®.
 *
 * @see "SoundFont® Technical Specification 2.04"
 */

@Value.Immutable
@ImmutablesStyleType
public interface NTParsedFileType
{
  /**
   * @return The information associated with the SoundFont
   */

  NTInfo info();

  /**
   * @return The sample records contained within the SoundFont
   */

  List<NTParsedSample> sampleRecords();

  /**
   * @return The location in the source file from which samples are sourced
   */

  NTSource sampleRecordsSource();

  /**
   * @return The preset records contained within the SoundFont
   */

  List<NTParsedPreset> presetRecords();

  /**
   * @return {@code #presetZoneRecords()}
   */

  default List<NTParsedPreset> phdr()
  {
    return this.presetRecords();
  }

  /**
   * @return The location in the source file from which presets are sourced
   */

  NTSource presetRecordsSource();

  /**
   * @return The preset zones contained within the SoundFont
   */

  List<NTParsedPresetZone> presetZoneRecords();

  /**
   * @return The location in the source file from which preset zones are sourced
   */

  NTSource presetZoneRecordsSource();

  /**
   * @return {@code #presetZoneRecords()}
   */

  default List<NTParsedPresetZone> pbag()
  {
    return this.presetZoneRecords();
  }

  /**
   * @return The preset zone modulators contained within the SoundFont
   */

  List<NTParsedPresetZoneModulator> presetZoneModulatorRecords();

  /**
   * @return The location in the source file from which preset zone modulators are sourced
   */

  NTSource presetZoneModulatorRecordsSource();

  /**
   * @return {@code #presetZoneModulatorRecords()}
   */

  default List<NTParsedPresetZoneModulator> pmod()
  {
    return this.presetZoneModulatorRecords();
  }

  /**
   * @return The preset zone generators contained within the SoundFont
   */

  List<NTParsedPresetZoneGenerator> presetZoneGeneratorRecords();

  /**
   * @return The location in the source file from which preset zone generators are sourced
   */

  NTSource presetZoneGeneratorRecordsSource();

  /**
   * @return {@code #presetZoneGeneratorRecords()}
   */

  default List<NTParsedPresetZoneGenerator> pgen()
  {
    return this.presetZoneGeneratorRecords();
  }

  /**
   * @return The instrument records contained within the SoundFont
   */

  List<NTParsedInstrument> instrumentRecords();

  /**
   * @return The location in the source file from which instrument records are sourced
   */

  NTSource instrumentRecordsSource();

  /**
   * @return {@code #instrumentRecords()}
   */

  default List<NTParsedInstrument> inst()
  {
    return this.instrumentRecords();
  }

  /**
   * @return The instrument zones contained within the SoundFont
   */

  List<NTParsedInstrumentZone> instrumentZoneRecords();

  /**
   * @return The location in the source file from which instrument zone records are sourced
   */

  NTSource instrumentZoneRecordsSource();

  /**
   * @return {@code #instrumentZoneRecords()}
   */

  default List<NTParsedInstrumentZone> ibag()
  {
    return this.instrumentZoneRecords();
  }

  /**
   * @return The instrument zones contained within the SoundFont
   */

  List<NTParsedInstrumentZoneModulator> instrumentZoneModulatorRecords();

  /**
   * @return The location in the source file from which instrument zone modulator records are
   * sourced
   */

  NTSource instrumentZoneModulatorRecordsSource();

  /**
   * @return {@code #instrumentZoneModulatorRecords()}
   */

  default List<NTParsedInstrumentZoneModulator> imod()
  {
    return this.instrumentZoneModulatorRecords();
  }

  /**
   * @return The instrument zone generators contained within the SoundFont
   */

  List<NTParsedInstrumentZoneGenerator> instrumentZoneGeneratorRecords();

  /**
   * @return The location in the source file from which instrument zone generator records are
   * sourced
   */

  NTSource instrumentZoneGeneratorRecordsSource();

  /**
   * @return {@code #instrumentZoneGeneratorRecords()}
   */

  default List<NTParsedInstrumentZoneGenerator> igen()
  {
    return this.instrumentZoneGeneratorRecords();
  }
}
