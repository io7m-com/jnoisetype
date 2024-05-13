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

package com.io7m.jnoisetype.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jranges.RangeCheck;
import org.immutables.value.Value;

/**
 * @see "SoundFont® Technical Specification 2.04, §7.2 The PHDR subchunk"
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTPresetNameType extends Comparable<NTPresetNameType>
{
  /**
   * @return The actual string value
   */

  @Value.Parameter
  String value();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    final var text = this.value();

    RangeCheck.checkIncludedInInteger(
      text.length(),
      "Preset name length",
      NTRanges.PRESET_NAME_LENGTH_RANGE,
      "Valid preset name lengths");

    if (text.chars().anyMatch(code -> code == 0)) {
      throw new IllegalArgumentException("Strings must not contain codepoint 0x0");
    }
  }

  @Override
  default int compareTo(
    final NTPresetNameType other)
  {
    return this.value().compareTo(other.value());
  }
}
