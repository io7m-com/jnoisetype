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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Standard generators.
 *
 * @see "SoundFontⓡ Technical Specification 2.04, §8.1 Generator Enumerators Defined"
 */

public final class NTGenerators
{
  private static final GeneratorSet GENERATORS = loadGenerators();

  private NTGenerators()
  {

  }

  private static final class GeneratorSet
  {
    private final SortedMap<Integer, NTGenerator> generators_by_id;
    private final SortedMap<String, NTGenerator> generators_by_name;

    private GeneratorSet(
      final SortedMap<Integer, NTGenerator> in_generators_by_id,
      final SortedMap<String, NTGenerator> in_generators_by_name)
    {
      this.generators_by_id =
        Objects.requireNonNull(in_generators_by_id, "generators_by_id");
      this.generators_by_name =
        Objects.requireNonNull(in_generators_by_name, "generators_by_name");
    }
  }

  private static GeneratorSet loadGenerators()
  {
    try (var stream = NTGenerators.class.getResourceAsStream(
      "/com/io7m/jnoisetype/api/generators.properties")) {

      final var properties = new Properties();
      properties.load(stream);

      final var generators_by_id = new TreeMap<Integer, NTGenerator>();
      final var generators_by_name = new TreeMap<String, NTGenerator>();
      for (final var entry : properties.entrySet()) {
        final var index = Integer.parseInt(entry.getKey().toString());
        final var name = entry.getValue().toString().trim();
        final var generator = NTGenerator.of(NTGeneratorOperatorIndex.of(index), name);
        generators_by_id.put(Integer.valueOf(index), generator);
        generators_by_name.put(name, generator);
      }

      return new GeneratorSet(
        Collections.unmodifiableSortedMap(generators_by_id),
        Collections.unmodifiableSortedMap(generators_by_name));
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
    return GENERATORS.generators_by_id.getOrDefault(
      Integer.valueOf(value),
      NTGenerator.of(NTGeneratorOperatorIndex.of(value), "unknown"));
  }

  /**
   * Find an operator with the given name.
   *
   * @param name The name
   *
   * @return The located operator
   */

  public static Optional<NTGenerator> findForName(final String name)
  {
    return Optional.ofNullable(
      GENERATORS.generators_by_name.get(Objects.requireNonNull(name, "name")));
  }

  /**
   * @return A read-only map of the known specified generators
   */

  public static Map<Integer, NTGenerator> generators()
  {
    return GENERATORS.generators_by_id;
  }
}
