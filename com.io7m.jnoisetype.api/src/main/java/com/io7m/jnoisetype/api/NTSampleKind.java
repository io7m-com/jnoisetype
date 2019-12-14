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

/**
 * @see "SoundFontⓡ Technical Specification 2.04, §7.10 The SHDR subchunk"
 */

public enum NTSampleKind
{
  /**
   * A nonexistent sample kind; used in terminal records.
   */

  SAMPLE_KIND_NONE(0),

  /**
   * A mono sample.
   */

  SAMPLE_KIND_MONO(1),

  /**
   * A right stereo sample.
   */

  SAMPLE_KIND_RIGHT(2),

  /**
   * A left stereo sample.
   */

  SAMPLE_KIND_LEFT(4),

  /**
   * A linked sample.
   */

  SAMPLE_KIND_LINKED(8),

  /**
   * A mono sample in a ROM.
   */

  SAMPLE_KIND_ROM_MONO(32769),

  /**
   * A right stereo sample in a ROM.
   */

  SAMPLE_KIND_ROM_RIGHT(32770),

  /**
   * A left stereo sample in a ROM.
   */

  SAMPLE_KIND_ROM_LEFT(32772),

  /**
   * A linked sample in a ROM.
   */

  SAMPLE_KIND_ROM_LINKED(32776);

  private final int value;

  NTSampleKind(
    final int i)
  {
    this.value = i;
  }

  /**
   * @return The integer value of the enumeration
   */

  public int value()
  {
    return this.value;
  }
}
