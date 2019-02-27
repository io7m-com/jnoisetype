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

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTRanges;
import com.io7m.jranges.RangeCheck;
import org.immutables.value.Value;

import java.util.List;

/**
 * The description of an instrument consumed by a writer.
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTInstrumentWriterDescriptionType
{
  /**
   * @return The unique index of the instrument, monotonically increasing and starting at {@code 0}
   */

  int instrumentIndex();

  /**
   * @return The name of the instrument
   */

  NTInstrumentName name();

  /**
   * @return The index into the instrument bag
   */

  int instrumentBagIndex();

  /**
   * @return The zones in the instrument
   */

  List<NTInstrumentWriterZoneDescription> zones();

  /**
   * @return The predicted index of the next instrument's bag
   */

  default int instrumentNextBagIndex()
  {
    return this.instrumentBagIndex() + this.zones().size();
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    RangeCheck.checkIncludedInInteger(
      this.instrumentIndex(),
      "Instrument index",
      NTRanges.INSTRUMENT_INDEX_RANGE,
      "Valid instrument indices");

    RangeCheck.checkIncludedInInteger(
      this.instrumentIndex(),
      "Instrument bag index",
      NTRanges.INSTRUMENT_BAG_INDEX_RANGE,
      "Valid instrument bag indices");
  }
}
