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

package com.io7m.jnoisetype.vanilla.interpreter;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTPresetType;
import com.io7m.jnoisetype.api.NTPresetZoneType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class NTIPreset implements NTPresetType
{
  private final NTPresetName name;
  private final NTFontType font;
  private final int index;
  private final List<NTPresetZoneType> zones_read;
  private final List<NTIPresetZone> zones;

  NTIPreset(
    final NTFontType in_font,
    final int preset_index,
    final NTPresetName in_name)
  {
    Preconditions.checkPreconditionI(
      preset_index,
      preset_index >= 0,
      i -> "Index must be non-negative");

    this.font = Objects.requireNonNull(in_font, "font");
    this.index = preset_index;
    this.name = Objects.requireNonNull(in_name, "name");
    this.zones = new ArrayList<>();
    this.zones_read = Collections.unmodifiableList(this.zones);
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
    final var preset = (NTIPreset) o;
    return this.index == preset.index
      && Objects.equals(this.name, preset.name)
      && Objects.equals(this.zones, preset.zones);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.name, Integer.valueOf(this.index), this.zones);
  }

  @Override
  public String toString()
  {
    return new StringBuilder(64)
      .append("[Preset ")
      .append(this.index)
      .append('\'')
      .append(this.name.value())
      .append('\'')
      .append("']")
      .toString();
  }

  @Override
  public NTPresetName name()
  {
    return this.name;
  }

  @Override
  public List<NTPresetZoneType> zones()
  {
    return this.zones_read;
  }

  @Override
  public NTFontType font()
  {
    return this.font;
  }

  @Override
  public String nameText()
  {
    return this.name.value();
  }

  void addZone(final NTIPresetZone zone)
  {
    this.zones.add(zone);
  }
}
