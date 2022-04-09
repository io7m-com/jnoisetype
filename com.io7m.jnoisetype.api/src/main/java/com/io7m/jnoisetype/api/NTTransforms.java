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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standard transforms.
 *
 * @see "SoundFontⓡ Technical Specification 2.04, §8.3 Modulator Transform Enumerators"
 */

public final class NTTransforms
{
  private static final Map<NTTransformIndex, NTTransform> TRANSFORMS = loadGenerators();

  private NTTransforms()
  {

  }

  private static Map<NTTransformIndex, NTTransform> loadGenerators()
  {
    try (var stream = NTTransforms.class.getResourceAsStream(
      "/com/io7m/jnoisetype/api/transforms.properties")) {

      final var properties = new Properties();
      properties.load(stream);

      return properties.entrySet()
        .stream()
        .map(entry -> {
          final var index = NTTransformIndex.of(Integer.parseInt(entry.getKey().toString()));
          final var name = entry.getValue().toString();
          final var generator = NTTransform.of(index, name);
          return new AbstractMap.SimpleImmutableEntry<>(index, generator);
        })
        .collect(Collectors.toUnmodifiableMap(
          AbstractMap.SimpleImmutableEntry::getKey,
          AbstractMap.SimpleImmutableEntry::getValue));

    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Find a transform with the given index.
   *
   * @param value The index
   *
   * @return The located transform, or a value with "unknown" as the name
   */

  public static NTTransform find(final int value)
  {
    return TRANSFORMS.getOrDefault(
      NTTransformIndex.of(value),
      NTTransform.of(NTTransformIndex.of(value), "unknown"));
  }

  /**
   * @return A read-only map of the known specified transforms
   */

  public static Map<NTTransformIndex, NTTransform> transforms()
  {
    return TRANSFORMS;
  }
}
