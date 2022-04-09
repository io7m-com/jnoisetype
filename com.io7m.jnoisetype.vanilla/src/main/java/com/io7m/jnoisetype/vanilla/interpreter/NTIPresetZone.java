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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnoisetype.api.NTPresetType;
import com.io7m.jnoisetype.api.NTPresetZoneGeneratorType;
import com.io7m.jnoisetype.api.NTPresetZoneModulatorType;
import com.io7m.jnoisetype.api.NTPresetZoneType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class NTIPresetZone implements NTPresetZoneType
{
  private final NTIPreset preset;
  private final List<NTIPresetZoneGenerator> generators;
  private final List<NTPresetZoneGeneratorType> generators_read;
  private final List<NTIPresetZoneModulator> modulators;
  private final List<NTPresetZoneModulatorType> modulators_read;
  private final int index;

  NTIPresetZone(
    final NTIPreset in_preset,
    final int in_index)
  {
    Preconditions.checkPreconditionI(
      in_index,
      in_index >= 0,
      x -> "Zone index must be > 0");

    this.preset = Objects.requireNonNull(in_preset, "preset");
    this.index = in_index;
    this.generators = new ArrayList<>();
    this.generators_read = Collections.unmodifiableList(this.generators);
    this.modulators = new ArrayList<>();
    this.modulators_read = Collections.unmodifiableList(this.modulators);
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
    final var that = (NTIPresetZone) o;
    return this.index == that.index
      && Objects.equals(this.generators, that.generators)
      && Objects.equals(this.modulators, that.modulators);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.generators, this.modulators, Integer.valueOf(this.index));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(32)
      .append("[PresetZone ")
      .append(this.index)
      .append(']')
      .toString();
  }

  @Override
  public List<NTPresetZoneGeneratorType> generators()
  {
    return this.generators_read;
  }

  @Override
  public List<NTPresetZoneModulatorType> modulators()
  {
    return this.modulators_read;
  }

  private boolean lastGeneratorIsNotInstrument()
  {
    if (this.generators.isEmpty()) {
      return true;
    }

    final var last_generator = this.generators.get(this.generators.size() - 1);
    return !Objects.equals(last_generator.generatorOperator().name(), "instrument");
  }

  private boolean isFirstZone()
  {
    return this.index == 0;
  }

  @Override
  public NTPresetType preset()
  {
    return this.preset;
  }

  @Override
  public boolean isGlobal()
  {
    return this.isFirstZone() && this.lastGeneratorIsNotInstrument();
  }

  void addGenerator(final NTIPresetZoneGenerator generator)
  {
    this.generators.add(generator);
  }

  void addModulator(final NTIPresetZoneModulator modulator)
  {
    this.modulators.add(modulator);
  }
}
