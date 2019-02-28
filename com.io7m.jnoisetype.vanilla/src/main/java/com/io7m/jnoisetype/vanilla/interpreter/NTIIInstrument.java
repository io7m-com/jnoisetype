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
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTInstrumentType;
import com.io7m.jnoisetype.api.NTInstrumentZoneType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class NTIIInstrument implements NTInstrumentType
{
  private final NTIFont font;
  private final NTInstrumentName name;
  private final List<NTInstrumentZoneType> zones_read;
  private final List<NTIInstrumentZone> zones;
  private final int index;

  NTIIInstrument(
    final NTIFont in_font,
    final int in_index,
    final NTInstrumentName in_name)
  {
    Preconditions.checkPreconditionI(in_index, in_index >= 0, i -> "Index must be non-negative");

    this.font = Objects.requireNonNull(in_font, "font");
    this.index = in_index;
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
    final var that = (NTIIInstrument) o;
    return this.index == that.index
      && Objects.equals(this.name, that.name)
      && Objects.equals(this.zones, that.zones);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.name, this.zones, Integer.valueOf(this.index));
  }

  @Override
  public String toString()
  {
    return new StringBuilder(64)
      .append("[Instrument ")
      .append(this.index)
      .append('\'')
      .append(this.name.value())
      .append('\'')
      .append("']")
      .toString();
  }

  @Override
  public NTFontType font()
  {
    return this.font;
  }

  @Override
  public NTInstrumentName name()
  {
    return this.name;
  }

  @Override
  public List<NTInstrumentZoneType> zones()
  {
    return this.zones_read;
  }

  @Override
  public String nameText()
  {
    return this.name().value();
  }

  void addZone(final NTIInstrumentZone zone)
  {
    this.zones.add(zone);
  }
}
