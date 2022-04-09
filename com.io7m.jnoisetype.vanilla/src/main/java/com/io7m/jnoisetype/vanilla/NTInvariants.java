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

package com.io7m.jnoisetype.vanilla;

import com.io7m.jnoisetype.api.NTNamedType;
import com.io7m.jnoisetype.api.NTSource;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.parser.api.NTParsedElementType;

import java.util.List;
import java.util.Objects;

/**
 * Functions to check various SoundFontⓒ specification invariants.
 */

public final class NTInvariants
{
  private NTInvariants()
  {

  }

  /**
   * Check that a named terminal record exists as the last element in the list of elements. This is
   * required in various places by the specification.
   *
   * @param elements              The list of parsed elements
   * @param name                  The required terminal record name
   * @param specification_section The specification section that documents the requirement
   * @param source                The source information
   * @param extra                 A function that can add extra diagnostic information to any
   *                              exception raised
   * @param <T>                   The type of input elements
   *
   * @return The terminal record
   *
   * @throws NTParseException If the element list does not contain a terminal record
   */

  public static <T extends NTNamedType & NTParsedElementType> T checkNamedTerminalRecordExists(
    final List<T> elements,
    final String name,
    final String specification_section,
    final NTSource source,
    final ExtraInformationType extra)
    throws NTParseException
  {
    Objects.requireNonNull(elements, "elements");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(specification_section, "specification section");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(extra, "extra");

    if (elements.isEmpty()) {
      throw namedTerminalRecordRequired(null, source, name, extra);
    }

    final var last = elements.get(elements.size() - 1);
    if (!Objects.equals(last.nameText(), name)) {
      throw namedTerminalRecordRequired(last, last.source(), name, extra);
    }

    return last;
  }

  /**
   * Check that a named terminal record exists as the last element in the list of elements. This is
   * required in various places by the specification.
   *
   * @param elements              The list of parsed elements
   * @param name                  The required terminal record name
   * @param specification_section The specification section that documents the requirement
   * @param source                The source information
   * @param <T>                   The type of input elements
   *
   * @return The terminal record
   *
   * @throws NTParseException If the element list does not contain a terminal record
   */

  public static <T extends NTNamedType & NTParsedElementType> T checkNamedTerminalRecordExists(
    final List<T> elements,
    final String name,
    final String specification_section,
    final NTSource source)
    throws NTParseException
  {
    return checkNamedTerminalRecordExists(
      elements,
      name,
      specification_section,
      source,
      message -> {

      });
  }

  /**
   * Check that an unnamed terminal record exists as the last element in the list of elements. This
   * is required in various places by the specification.
   *
   * @param elements              The list of parsed elements
   * @param specification_section The specification section that documents the requirement
   * @param source                The source information
   * @param extra                 A function that can add extra diagnostic information to any
   *                              exception raised
   * @param <T>                   The type of input elements
   *
   * @return The terminal record
   *
   * @throws NTParseException If the element list does not contain a terminal record
   */

  public static <T extends NTParsedElementType> T checkUnnamedTerminalRecordExists(
    final List<T> elements,
    final String specification_section,
    final NTSource source,
    final ExtraInformationType extra)
    throws NTParseException
  {
    Objects.requireNonNull(elements, "elements");
    Objects.requireNonNull(specification_section, "specification section");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(extra, "extra");

    if (elements.isEmpty()) {
      throw unnamedTerminalRecordRequired(source, extra);
    }

    return elements.get(elements.size() - 1);
  }

  /**
   * Check that an unnamed terminal record exists as the last element in the list of elements. This
   * is required in various places by the specification.
   *
   * @param elements              The list of parsed elements
   * @param specification_section The specification section that documents the requirement
   * @param source                The source information
   * @param <T>                   The type of input elements
   *
   * @return The terminal record
   *
   * @throws NTParseException If the element list does not contain a terminal record
   */

  public static <T extends NTParsedElementType> T checkUnnamedTerminalRecordExists(
    final List<T> elements,
    final String specification_section,
    final NTSource source)
    throws NTParseException
  {
    return checkUnnamedTerminalRecordExists(
      elements,
      specification_section,
      source,
      message -> {

      });
  }

  private static <T extends NTNamedType & NTParsedElementType> NTParseException namedTerminalRecordRequired(
    final T element,
    final NTSource source,
    final String expected_terminal_name,
    final ExtraInformationType extra_information)
  {
    final var separator = System.lineSeparator();
    final var message =
      new StringBuilder(128)
        .append("A terminal record is required but was not present.")
        .append(separator)
        .append("  Source: ")
        .append(source.source())
        .append(separator)
        .append("  Offset: 0x")
        .append(Long.toUnsignedString(source.offset(), 16))
        .append(separator);

    extra_information.addExtraInformation(message);

    if (expected_terminal_name != null) {
      message.append("  Required terminal record name: ")
        .append(expected_terminal_name)
        .append(separator);

      message.append("  Received terminal record name: ");
      if (element != null) {
        message.append('\'')
          .append(element.nameText())
          .append('\'');
      } else {
        message.append("<none>");
      }
      message.append(separator);
    }

    return new NTParseException(message.toString(), source.source(), source.offset());
  }

  private static NTParseException unnamedTerminalRecordRequired(
    final NTSource source,
    final ExtraInformationType extra_information)
  {
    final var separator = System.lineSeparator();
    final var message =
      new StringBuilder(128)
        .append("A terminal record is required but was not present.")
        .append(separator)
        .append("  Source: ")
        .append(source.source())
        .append(separator)
        .append("  Offset: 0x")
        .append(Long.toUnsignedString(source.offset(), 16));

    extra_information.addExtraInformation(message);

    return new NTParseException(message.toString(), source.source(), source.offset());
  }

  /**
   * A function that can add extra information to an exception message.
   */

  public interface ExtraInformationType
  {
    /**
     * Add extra message to an exception message.
     *
     * @param message The message being built
     */

    void addExtraInformation(StringBuilder message);
  }
}
