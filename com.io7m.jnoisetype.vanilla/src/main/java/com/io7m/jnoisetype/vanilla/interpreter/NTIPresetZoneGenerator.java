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

import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTPresetZoneGeneratorType;
import com.io7m.jnoisetype.api.NTPresetZoneType;

import java.util.Objects;

final class NTIPresetZoneGenerator implements NTPresetZoneGeneratorType
{
  private final NTIPresetZone zone;
  private final NTGenerator generator;
  private final NTGenericAmount amount;

  NTIPresetZoneGenerator(
    final NTIPresetZone in_zone,
    final NTGenerator in_generator,
    final NTGenericAmount in_amount)
  {
    this.zone = Objects.requireNonNull(in_zone, "zone");
    this.generator = Objects.requireNonNull(in_generator, "generator");
    this.amount = Objects.requireNonNull(in_amount, "amount");
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
    final var that = (NTIPresetZoneGenerator) o;
    return Objects.equals(this.generator, that.generator)
      && Objects.equals(this.amount, that.amount);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.generator, this.amount);
  }

  @Override
  public String toString()
  {
    return new StringBuilder(64)
      .append("[PresetZoneGenerator ")
      .append(this.generator)
      .append(' ')
      .append(this.amount)
      .append(' ')
      .append(this.zone)
      .append(']')
      .toString();
  }

  @Override
  public NTPresetZoneType zone()
  {
    return this.zone;
  }

  @Override
  public NTGenerator generatorOperator()
  {
    return this.generator;
  }

  @Override
  public NTGenericAmount amount()
  {
    return this.amount;
  }
}
