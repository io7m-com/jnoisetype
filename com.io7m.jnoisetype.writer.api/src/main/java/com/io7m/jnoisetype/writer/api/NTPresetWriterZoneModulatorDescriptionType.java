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

package com.io7m.jnoisetype.writer.api;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTModulatorIndex;
import com.io7m.jnoisetype.api.NTNativeType;
import com.io7m.jnoisetype.api.NTTransform;
import org.immutables.value.Value;

/**
 * The description of a preset zone modulator consumed by a writer.
 */

@ImmutablesStyleType
@Value.Immutable
public interface NTPresetWriterZoneModulatorDescriptionType
{
  /**
   * @return The index of the modulator in the zone
   */

  NTModulatorIndex index();

  /**
   * @return The source data for the modulator
   */

  @NTNativeType(name = "SFModulator")
  int sourceOperator();

  /**
   * @return The destination of the modulator
   */

  @NTNativeType(name = "SFGenerator")
  NTGenerator targetOperator();

  /**
   * @return The degree to which the source modulates the destination. A zero value indicates there
   * is no fixed amount.
   */

  @NTNativeType(name = "short")
  short modulationAmount();

  /**
   * @return The degree to which the source modulates the destination is to be controlled by the
   * specified modulation source
   */

  @NTNativeType(name = "SFModulator")
  int modulationAmountSourceOperator();

  /**
   * @return A value that indicates that a transform of the specified type will be applied to the
   * modulation source before application to the modulator
   */

  @NTNativeType(name = "SFTransform")
  NTTransform modulationTransformOperator();
}
