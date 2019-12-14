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

import com.io7m.jnoisetype.api.NTBankIndex;
import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGeneratorIndex;
import com.io7m.jnoisetype.api.NTGeneratorOperatorIndex;
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
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTSource;
import com.io7m.jnoisetype.api.NTTransform;
import com.io7m.jnoisetype.api.NTTransformIndex;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.parser.api.NTParsedFile;
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
import com.io7m.jnoisetype.writer.api.NTSampleDataWriterType;
import com.io7m.jnoisetype.writer.api.NTSampleWriterDescription;
import com.io7m.jranges.RangeHalfOpenL;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NTBruteForceEqualityTest
{
  private static final Class<?> API_CLASSES[] = {
    NTGenerator.class,
    NTGeneratorOperatorIndex.class,
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
    NTTransformIndex.class,
    NTTransform.class,
    NTVersion.class
  };

  private static final Class<?> PARSER_CLASSES[] = {
    NTParsedFile.class,
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

  private static final class SensibleAnswers implements Answer<Object>
  {
    @Override
    public Object answer(final InvocationOnMock invocation)
      throws Throwable
    {
      final var return_type = invocation.getMethod().getReturnType();
      if (return_type.equals(String.class)) {
        return "xyz";
      }
      if (return_type.equals(URI.class)) {
        return URI.create("xyz");
      }
      if (return_type.equals(NTSource.class)) {
        return NTSource.of(URI.create("xyz"), 23L);
      }
      if (return_type.equals(NTGenericAmount.class)) {
        return NTGenericAmount.of(23);
      }
      if (return_type.equals(NTTransformIndex.class)) {
        return NTTransformIndex.of(23);
      }
      if (return_type.equals(NTBankIndex.class)) {
        return NTBankIndex.of(67);
      }
      if (return_type.equals(NTSampleIndex.class)) {
        return NTSampleIndex.of(23);
      }
      if (return_type.equals(NTSampleName.class)) {
        return NTSampleName.of("xyz");
      }
      if (return_type.equals(NTPresetIndex.class)) {
        return NTPresetIndex.of(23);
      }
      if (return_type.equals(NTPresetName.class)) {
        return NTPresetName.of("xyz");
      }
      if (return_type.equals(NTInstrumentIndex.class)) {
        return NTInstrumentIndex.of(23);
      }
      if (return_type.equals(NTInstrumentName.class)) {
        return NTInstrumentName.of("xyz");
      }
      if (return_type.equals(NTShortString.class)) {
        return NTShortString.of("xyz");
      }
      if (return_type.equals(NTLongString.class)) {
        return NTLongString.of("xyz");
      }
      if (return_type.equals(NTGeneratorOperatorIndex.class)) {
        return NTGeneratorOperatorIndex.of(23);
      }
      if (return_type.equals(NTModulatorIndex.class)) {
        return NTModulatorIndex.of(23);
      }
      if (return_type.equals(NTGeneratorIndex.class)) {
        return NTGeneratorIndex.of(23);
      }
      if (return_type.equals(NTSampleKind.class)) {
        return NTSampleKind.SAMPLE_KIND_MONO;
      }
      if (return_type.equals(RangeHalfOpenL.class)) {
        return RangeHalfOpenL.of(0L, 100L);
      }
      if (return_type.equals(NTPitch.class)) {
        return NTPitch.of(60);
      }
      if (return_type.equals(NTVersion.class)) {
        return NTVersion.of(2, 10);
      }
      if (return_type.equals(NTSampleDataWriterType.class)) {
        return (NTSampleDataWriterType) (c) -> { };
      }
      if (return_type.equals(NTGenerator.class)) {
        return NTGenerator.of(NTGeneratorOperatorIndex.of(32), "what?");
      }
      if (return_type.equals(NTTransform.class)) {
        return NTTransform.of(NTTransformIndex.of(23), "something");
      }
      if (return_type.equals(NTSampleDescription.class)) {
        return NTSampleDescription.builder()
          .setName(NTSampleName.of("xyz"))
          .setEnd(23L)
          .setKind(NTSampleKind.SAMPLE_KIND_MONO)
          .setStart(0L)
          .setLoopStart(0L)
          .setLoopEnd(23L)
          .setSampleRate(48000)
          .setOriginalPitch(NTPitch.of(60))
          .setPitchCorrection(0)
          .build();
      }
      if (return_type.equals(NTSampleBuilderDescription.class)) {
        return NTSampleBuilderDescription.builder()
          .setName(NTSampleName.of("xyz"))
          .setKind(NTSampleKind.SAMPLE_KIND_MONO)
          .setLoopStart(0L)
          .setLoopEnd(23L)
          .setSampleRate(48000)
          .setOriginalPitch(NTPitch.of(60))
          .setPitchCorrection(0)
          .setSampleCount(200L)
          .setDataWriter(channel -> { })
          .build();
      }
      if (return_type.equals(NTInfo.class)) {
        return NTInfo.builder()
          .setVersion(NTVersion.of(2, 10))
          .setName(NTShortString.of("what?"))
          .build();
      }
      return Mockito.RETURNS_DEFAULTS.answer(invocation);
    }
  }

  private static void checkCopyOf(
    final Class<?> clazz)
    throws Exception
  {
    final var interface_type = clazz.getInterfaces()[0];
    final var mock = Mockito.mock(interface_type, new SensibleAnswers());
    final var copy_method = clazz.getMethod("copyOf", interface_type);
    final var copy = copy_method.invoke(clazz, mock);
    Assertions.assertTrue(interface_type.isAssignableFrom(copy.getClass()));
  }

  private static boolean fieldIsOK(
    final Field field)
  {
    if (Objects.equals(field.getName(), "$jacocoData")) {
      return false;
    }

    return !field.getType().isPrimitive();
  }

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

  @TestFactory
  public Stream<DynamicTest> testCopyOf()
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
        "testCopyOf" + clazz.getSimpleName(),
        () -> checkCopyOf(clazz)));
  }
}
