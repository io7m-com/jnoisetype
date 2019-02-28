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

import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGeneratorOperatorIndex;
import com.io7m.jnoisetype.api.NTGenericAmount;

/**
 * The type of preset zone builders.
 */

public interface NTPresetZoneBuilderType
{
  /**
   * Add a generator to the zone.
   *
   * @param generator The generator
   * @param amount    The amount
   *
   * @return The current zone builder
   */

  NTPresetZoneBuilderType addGenerator(
    NTGenerator generator,
    NTGenericAmount amount);

  /**
   * Add a key range generator to the zone.
   *
   * @param low  The lower bound of the key range
   * @param high The upper bound of the key range
   *
   * @return The current zone builder
   */

  default NTPresetZoneBuilderType addKeyRangeGenerator(
    final int low,
    final int high)
  {
    final var msb = (high << 8);
    final var lsb = low & 0xff;
    return this.addGenerator(
      NTGenerator.of(NTGeneratorOperatorIndex.of(43), "keyRange"),
      NTGenericAmount.of(msb | lsb));
  }

  /**
   * Add an instrument generator to the zone.
   *
   * @param instrument The instrument builder
   *
   * @return The current zone builder
   */

  default NTPresetZoneBuilderType addInstrumentGenerator(
    final NTInstrumentBuilderType instrument)
  {
    return this.addGenerator(
      NTGenerator.of(NTGeneratorOperatorIndex.of(41), "instrument"),
      NTGenericAmount.of(instrument.instrumentIndex().value()));
  }
}
