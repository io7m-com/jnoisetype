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

package com.io7m.jnoisetype.tests;

import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGeneratorIndex;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentIndex;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTLongString;
import com.io7m.jnoisetype.api.NTModulatorIndex;
import com.io7m.jnoisetype.api.NTPitch;
import com.io7m.jnoisetype.api.NTPresetIndex;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTSampleDescription;
import com.io7m.jnoisetype.api.NTSampleIndex;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTSource;
import com.io7m.jnoisetype.api.NTTransform;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.parser.api.NTParsedFile;
import com.io7m.jnoisetype.parser.api.NTParsedGenericAmount;
import com.io7m.jnoisetype.parser.api.NTParsedInstrument;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZone;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZoneGenerator;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZoneModulator;
import com.io7m.jnoisetype.parser.api.NTParsedPreset;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZone;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZoneGenerator;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZoneModulator;
import com.io7m.jnoisetype.parser.api.NTParsedSample;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneGeneratorDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneModulatorDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneGeneratorDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneModulatorDescription;
import com.io7m.jnoisetype.writer.api.NTSampleBuilderDescription;
import com.io7m.jnoisetype.writer.api.NTSampleWriterDescription;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NTBruteForceEqualityTest
{
  private static final Class<?> API_CLASSES[] = {
    NTGenerator.class,
    NTGeneratorIndex.class,
    NTGenericAmount.class,
    NTInfo.class,
    NTInstrumentIndex.class,
    NTInstrumentName.class,
    NTLongString.class,
    NTModulatorIndex.class,
    NTPitch.class,
    NTPresetIndex.class,
    NTPresetName.class,
    NTSampleDescription.class,
    NTSampleIndex.class,
    NTSampleName.class,
    NTShortString.class,
    NTSource.class,
    NTTransform.class,
    NTVersion.class
  };

  private static final Class<?> PARSER_CLASSES[] = {
    NTParsedFile.class,
    NTParsedGenericAmount.class,
    NTParsedInstrument.class,
    NTParsedInstrumentZone.class,
    NTParsedInstrumentZoneGenerator.class,
    NTParsedInstrumentZoneModulator.class,
    NTParsedPreset.class,
    NTParsedPresetZone.class,
    NTParsedPresetZoneGenerator.class,
    NTParsedPresetZoneModulator.class,
    NTParsedSample.class,
  };

  private static final Class<?> WRITER_CLASSES[] = {
    NTInstrumentWriterDescription.class,
    NTInstrumentWriterZoneDescription.class,
    NTInstrumentWriterZoneGeneratorDescription.class,
    NTInstrumentWriterZoneModulatorDescription.class,
    NTPresetWriterDescription.class,
    NTPresetWriterZoneDescription.class,
    NTPresetWriterZoneGeneratorDescription.class,
    NTPresetWriterZoneModulatorDescription.class,
    NTSampleBuilderDescription.class,
    NTSampleWriterDescription.class,
  };

  @TestFactory
  public Stream<DynamicTest> testEquals()
  {
    final var api_classes =
      Stream.of(API_CLASSES);
    final var parser_classes =
      Stream.of(PARSER_CLASSES);
    final var writer_classes =
      Stream.of(WRITER_CLASSES);

    final var all_classes =
      Stream.concat(api_classes, Stream.concat(parser_classes, writer_classes));

    return all_classes
      .map(clazz -> DynamicTest.dynamicTest(
        "testEquals" + clazz.getSimpleName(),
        () -> checkClassEquality(clazz)));
  }

  private static void checkClassEquality(
    final Class<?> clazz)
  {
    final var fields =
      Stream.of(clazz.getDeclaredFields())
        .filter(NTBruteForceEqualityTest::fieldIsOK)
        .map(Field::getName)
        .collect(Collectors.toList());

    final var field_names = new String[fields.size()];
    fields.toArray(field_names);

    EqualsVerifier.forClass(clazz)
      .withNonnullFields(field_names)
      .verify();
  }

  private static boolean fieldIsOK(
    final Field field)
  {
    if (Objects.equals(field.getName(), "$jacocoData")) {
      return false;
    }

    return !field.getType().isPrimitive();
  }
}
