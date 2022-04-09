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

package com.io7m.jnoisetype.vanilla.interpreter;

import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentType;
import com.io7m.jnoisetype.api.NTPresetType;
import com.io7m.jnoisetype.api.NTSampleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class NTIFont implements NTFontType
{
  private final NTInfo info;
  private final List<NTIIInstrument> instruments;
  private final List<NTInstrumentType> instruments_read;
  private final List<NTPresetType> presets;
  private final List<NTPresetType> presets_read;
  private final List<NTISample> samples;
  private final List<NTSampleType> samples_read;

  NTIFont(final NTInfo in_info)
  {
    this.info = Objects.requireNonNull(in_info, "info");
    this.instruments = new ArrayList<>();
    this.presets = new ArrayList<>();
    this.samples = new ArrayList<>();

    this.instruments_read =
      Collections.unmodifiableList(this.instruments);
    this.presets_read =
      Collections.unmodifiableList(this.presets);
    this.samples_read =
      Collections.unmodifiableList(this.samples);
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
    final var font = (NTIFont) o;
    return Objects.equals(this.info, font.info)
      && Objects.equals(this.instruments, font.instruments)
      && Objects.equals(this.presets, font.presets)
      && Objects.equals(this.samples, font.samples);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.info, this.instruments, this.presets, this.samples);
  }

  @Override
  public String toString()
  {
    return new StringBuilder(64)
      .append("[Font '")
      .append(this.info.name().value())
      .append("']")
      .toString();
  }

  @Override
  public NTInfo info()
  {
    return this.info;
  }

  @Override
  public List<NTInstrumentType> instruments()
  {
    return this.instruments_read;
  }

  @Override
  public List<NTPresetType> presets()
  {
    return this.presets_read;
  }

  @Override
  public List<NTSampleType> samples()
  {
    return this.samples_read;
  }

  void addPreset(final NTPresetType preset)
  {
    this.presets.add(preset);
  }

  void addInstrument(final NTIIInstrument instrument)
  {
    this.instruments.add(instrument);
  }

  void addSample(final NTISample sample)
  {
    this.samples.add(sample);
  }
}
