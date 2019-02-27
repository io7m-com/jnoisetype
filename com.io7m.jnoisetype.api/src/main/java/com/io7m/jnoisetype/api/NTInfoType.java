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

package com.io7m.jnoisetype.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * @see "SoundFontⓡ Technical Specification 2.04, §5 The INFO-list Chunk"
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTInfoType
{
  /**
   * @return The version of the specification to which the file is expected to comply
   */

  NTVersion version();

  /**
   * @return The wavetable sound engine for which the file was optimized
   */

  @Value.Default
  default NTShortString soundEngine()
  {
    return NTShortString.of("EMU8000");
  }

  /**
   * @return The name of the SoundFont compatible bank
   */

  NTShortString name();

  /**
   * @return A particular wavetable sound data ROM to which any ROM samples refer
   */

  Optional<NTShortString> rom();

  /**
   * @return The particular wavetable sound data ROM revision to which any ROM samples refer
   */

  Optional<NTVersion> romRevision();

  /**
   * @return The creation date of the SoundFont
   */

  Optional<NTShortString> creationDate();

  /**
   * @return The engineers responsible for the creation of the SoundFont
   */

  Optional<NTShortString> engineers();

  /**
   * @return Any specific product for which the SoundFont compatible bank is intended
   */

  Optional<NTShortString> product();

  /**
   * @return Any copyright assertion string associated with the SoundFont compatible bank
   */

  Optional<NTShortString> copyright();

  /**
   * @return Any comment text associated with the SoundFont compatible bank
   */

  Optional<NTLongString> comment();

  /**
   * @return The SoundFont compatible tools used to create and most recently modify the SoundFont
   * compatible bank
   */

  Optional<NTShortString> software();
}
