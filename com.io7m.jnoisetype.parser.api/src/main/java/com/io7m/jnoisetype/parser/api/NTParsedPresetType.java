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

package com.io7m.jnoisetype.parser.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jnoisetype.api.NTNamedType;
import com.io7m.jnoisetype.api.NTPresetName;
import org.immutables.value.Value;

/**
 * @see "SoundFont® Technical Specification 2.04, §7.2 The PHDR subchunk"
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTParsedPresetType extends NTNamedType, NTParsedElementType
{
  /**
   * @return The name of the preset
   */

  NTPresetName name();

  /**
   * @return The number of the preset
   */

  int preset();

  /**
   * @return The number of the preset bank
   */

  int bank();

  /**
   * @return The index into the {@link NTParsedFileType#pbag()} array
   */

  int presetBagIndex();

  /**
   * @return The library value (always 0 in current specifications)
   */

  long library();

  /**
   * @return The genre value (always 0 in current specifications)
   */

  long genre();

  /**
   * @return The morphology value (always 0 in current specifications)
   */

  long morphology();

  @Override
  default String nameText()
  {
    return this.name().value();
  }
}
