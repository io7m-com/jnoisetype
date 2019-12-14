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
import com.io7m.jnoisetype.api.NTPresetZoneModulatorType;
import com.io7m.jnoisetype.api.NTPresetZoneType;
import com.io7m.jnoisetype.api.NTTransform;

import java.util.Objects;

final class NTIPresetZoneModulator implements NTPresetZoneModulatorType
{
  private final NTIPresetZone zone;
  private final int source_operator;
  private final NTGenerator target_operator;
  private final int modulation_amount;
  private final int modulation_amount_source_operator;
  private final NTTransform modulation_transform_operator;

  NTIPresetZoneModulator(
    final NTIPresetZone in_zone,
    final int in_source_operator,
    final NTGenerator in_target_operator,
    final int in_modulation_amount,
    final int in_modulation_amount_source_operator,
    final NTTransform in_modulation_transform_operator)
  {
    this.zone =
      Objects.requireNonNull(in_zone, "zone");
    this.source_operator =
      in_source_operator;
    this.target_operator =
      Objects.requireNonNull(in_target_operator, "target_operator");
    this.modulation_amount =
      in_modulation_amount;
    this.modulation_amount_source_operator =
      in_modulation_amount_source_operator;
    this.modulation_transform_operator =
      Objects.requireNonNull(in_modulation_transform_operator, "modulation_transform_operator");
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
    final var that = (NTIPresetZoneModulator) o;
    return this.source_operator == that.source_operator
      && this.modulation_amount == that.modulation_amount
      && this.modulation_amount_source_operator == that.modulation_amount_source_operator
      && Objects.equals(this.target_operator, that.target_operator)
      && Objects.equals(this.modulation_transform_operator, that.modulation_transform_operator);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(
      Integer.valueOf(this.source_operator),
      this.target_operator,
      Integer.valueOf(this.modulation_amount),
      Integer.valueOf(this.modulation_amount_source_operator),
      this.modulation_transform_operator);
  }

  @Override
  public String toString()
  {
    return "[PresetZoneModulator]";
  }

  @Override
  public NTPresetZoneType zone()
  {
    return this.zone;
  }

  @Override
  public int sourceOperator()
  {
    return this.source_operator;
  }

  @Override
  public NTGenerator targetOperator()
  {
    return this.target_operator;
  }

  @Override
  public int modulationAmount()
  {
    return this.modulation_amount;
  }

  @Override
  public int modulationAmountSourceOperator()
  {
    return this.modulation_amount_source_operator;
  }

  @Override
  public NTTransform modulationTransformOperator()
  {
    return this.modulation_transform_operator;
  }
}
