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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standard generators.
 *
 * @see "SoundFontⓡ Technical Specification 2.04, §8.1 Generator Enumerators Defined"
 */

public final class NTGenerators
{
  private static final Map<Integer, NTGenerator> GENERATORS = loadGenerators();

  private NTGenerators()
  {

  }

  private static Map<Integer, NTGenerator> loadGenerators()
  {
    try (var stream = NTGenerators.class.getResourceAsStream(
      "/com/io7m/jnoisetype/api/generators.properties")) {

      final var properties = new Properties();
      properties.load(stream);

      return properties.entrySet()
        .stream()
        .map(entry -> {
          final var index = Integer.parseInt(entry.getKey().toString());
          final var name = entry.getValue().toString();
          final var generator = NTGenerator.of(NTGeneratorOperatorIndex.of(index), name);
          return new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(index), generator);
        })
        .collect(Collectors.toUnmodifiableMap(
          AbstractMap.SimpleImmutableEntry::getKey,
          AbstractMap.SimpleImmutableEntry::getValue));

    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Find an operator with the given index.
   *
   * @param value The index
   *
   * @return The located operator, or a value with "unknown" as the operator name
   */

  public static NTGenerator find(final int value)
  {
    return GENERATORS.getOrDefault(
      Integer.valueOf(value),
      NTGenerator.of(NTGeneratorOperatorIndex.of(value), "unknown"));
  }

  /**
   * @return A read-only map of the known specified generators
   */

  public static Map<Integer, NTGenerator> generators()
  {
    return GENERATORS;
  }
}
