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

package com.io7m.jnoisetype.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentType;
import com.io7m.jnoisetype.api.NTPresetType;
import com.io7m.jnoisetype.api.NTSampleType;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.parser.api.NTInterpreterProviderType;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

@Parameters(commandDescription = "Display the contents of a SoundFontⓡ file")
final class CommandShow extends CommandRoot
{
  // CHECKSTYLE:OFF

  @Parameter(
    names = "--file",
    required = true,
    description = "The SoundFontⓡ file to display")
  Path path;

  // CHECKSTYLE:ON

  @Override
  public Void call()
    throws Exception
  {
    super.call();

    final var parsers =
      ServiceLoader.load(NTFileParserProviderType.class)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "No SoundFontⓡ file parser service available"));

    final var interpreters =
      ServiceLoader.load(NTInterpreterProviderType.class)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "No SoundFontⓡ file interpreter service available"));

    try (var channel = FileChannel.open(this.path, READ)) {
      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = parsers.createForByteBuffer(this.path.toUri(), map);
      final var file_raw = parser.parse();
      final var file = interpreters.createInterpreter(file_raw).interpret();

      final var info = file.info();
      showInfo(info);
      showStats(file);
      showSamples(file.samples());
      showInstruments(file.instruments());
      showPresets(file.presets());
    }

    return null;
  }

  private static void showPresets(final List<NTPresetType> presets)
  {
    for (var index = 0; index < presets.size(); ++index) {
      final var preset = presets.get(index);

      final var presetIndex =
        Integer.toUnsignedString(preset.index().value());
      final var bankIndex =
        Integer.toUnsignedString(preset.bank().value());

      System.out.printf(
        "preset %s %s %s : %s\n",
        bankIndex,
        presetIndex,
        "name",
        preset.name().value());

      System.out.printf(
        "preset %s %s %s : %d\n",
        bankIndex,
        presetIndex,
        "zones",
        Integer.valueOf(preset.zones().size()));

      for (var zone_index = 0; zone_index < preset.zones().size(); ++zone_index) {
        final var zone = preset.zones().get(zone_index);

        final String zoneIndex =
          Integer.toUnsignedString(zone_index);

        System.out.printf(
          "preset %s %s zone %s %s : %s\n",
          bankIndex,
          presetIndex,
          zoneIndex,
          "generators",
          Integer.valueOf(zone.generators().size()));

        for (var generator_index = 0; generator_index < zone.generators().size(); ++generator_index) {
          final var generator = zone.generators().get(generator_index);

          System.out.printf(
            "preset %s %s zone %s generator %d index : %d\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(generator_index),
            Integer.valueOf(generator.generatorOperator().index().value()));

          System.out.printf(
            "preset %s %s zone %s generator %d name : %s\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(generator_index),
            generator.generatorOperator().name());

          System.out.printf(
            "preset %s %s zone %s generator %d amount : %d | 0x%s | (0x%x, 0x%x)\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(generator_index),
            Integer.valueOf(generator.amount().value()),
            Integer.toUnsignedString(generator.amount().asUnsigned16(), 16),
            Integer.valueOf(generator.amount().asUnsignedBytesLow()),
            Integer.valueOf(generator.amount().asUnsignedBytesHigh()));
        }

        System.out.printf(
          "preset %s %s zone %s %s : %s\n",
          bankIndex,
          presetIndex,
          zoneIndex,
          "modulators",
          Integer.valueOf(zone.modulators().size()));

        for (var modulator_index = 0; modulator_index < zone.modulators().size(); ++modulator_index) {
          final var modulator = zone.modulators().get(modulator_index);

          System.out.printf(
            "preset %s %s zone %s modulator %d source-operator : %d (0x%s)\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.sourceOperator()),
            Integer.toUnsignedString(modulator.sourceOperator(), 16));

          System.out.printf(
            "preset %s %s zone %s modulator %d target-operator : %d (0x%s)\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.targetOperator().index().value()),
            Integer.toUnsignedString(modulator.targetOperator().index().value(), 16));

          System.out.printf(
            "preset %s %s zone %s modulator %d amount : %d (0x%s)\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.modulationAmount()),
            Integer.toUnsignedString(modulator.modulationAmount(), 16));

          System.out.printf(
            "preset %s %s zone %s modulator %d amount-source-operator : %d (0x%s)\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.modulationAmountSourceOperator()),
            Integer.toUnsignedString(modulator.modulationAmountSourceOperator(), 16));

          System.out.printf(
            "preset %s %s zone %s modulator %d transform-operator : %d (0x%s)\n",
            bankIndex,
            presetIndex,
            zoneIndex,
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.modulationTransformOperator().index().value()),
            Integer.toUnsignedString(modulator.modulationTransformOperator().index().value(), 16));
        }
      }

      System.out.println();
    }
  }

  private static void showInstruments(
    final List<NTInstrumentType> instruments)
  {
    for (var index = 0; index < instruments.size(); ++index) {
      final var instrument = instruments.get(index);

      System.out.printf(
        "instrument %d %s : %s\n",
        Integer.valueOf(index),
        "name",
        instrument.name().value());

      System.out.printf(
        "instrument %d %s : %d\n",
        Integer.valueOf(index),
        "zones",
        Integer.valueOf(instrument.zones().size()));

      for (var zone_index = 0; zone_index < instrument.zones().size(); ++zone_index) {
        final var zone = instrument.zones().get(zone_index);

        System.out.printf(
          "instrument %d zone %d %s : %s\n",
          Integer.valueOf(index),
          Integer.valueOf(zone_index),
          "generators",
          Integer.valueOf(zone.generators().size()));

        for (var generator_index = 0; generator_index < zone.generators().size(); ++generator_index) {
          final var generator = zone.generators().get(generator_index);

          System.out.printf(
            "instrument %d zone %d generator %d index : %d\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(generator_index),
            Integer.valueOf(generator.generatorOperator().index().value()));

          System.out.printf(
            "instrument %d zone %d generator %d name : %s\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(generator_index),
            generator.generatorOperator().name());

          System.out.printf(
            "instrument %d zone %d generator %d amount : %d | 0x%s | (0x%x, 0x%x)\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(generator_index),
            Integer.valueOf(generator.amount().value()),
            Integer.toUnsignedString(generator.amount().asUnsigned16(), 16),
            Integer.valueOf(generator.amount().asUnsignedBytesLow()),
            Integer.valueOf(generator.amount().asUnsignedBytesHigh()));
        }
        
        System.out.printf(
          "instrument %d zone %d %s : %s\n",
          Integer.valueOf(index),
          Integer.valueOf(zone_index),
          "modulators",
          Integer.valueOf(zone.modulators().size()));

        for (var modulator_index = 0; modulator_index < zone.modulators().size(); ++modulator_index) {
          final var modulator = zone.modulators().get(modulator_index);

          System.out.printf(
            "instrument %d zone %d modulator %d source-operator : %d (0x%s)\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.sourceOperator()),
            Integer.toUnsignedString(modulator.sourceOperator(), 16));

          System.out.printf(
            "instrument %d zone %d modulator %d target-operator : %d (0x%s)\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.targetOperator().index().value()),
            Integer.toUnsignedString(modulator.targetOperator().index().value(), 16));

          System.out.printf(
            "instrument %d zone %d modulator %d amount : %d (0x%s)\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.modulationAmount()),
            Integer.toUnsignedString(modulator.modulationAmount(), 16));

          System.out.printf(
            "instrument %d zone %d modulator %d amount-source-operator : %d (0x%s)\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.modulationAmountSourceOperator()),
            Integer.toUnsignedString(modulator.modulationAmountSourceOperator(), 16));

          System.out.printf(
            "instrument %d zone %d modulator %d transform-operator : %d (0x%s)\n",
            Integer.valueOf(index),
            Integer.valueOf(zone_index),
            Integer.valueOf(modulator_index),
            Integer.valueOf(modulator.modulationTransformOperator().index().value()),
            Integer.toUnsignedString(modulator.modulationTransformOperator().index().value(), 16));
        }
      }

      System.out.println();
    }
  }

  private static void showStats(
    final NTFontType file)
  {
    System.out.printf(
      "%s : %s\n",
      "samples",
      Integer.valueOf(file.samples().size()));

    System.out.printf(
      "%s : %s\n",
      "instruments",
      Integer.valueOf(file.instruments().size()));

    System.out.printf(
      "%s : %s\n",
      "presets",
      Integer.valueOf(file.presets().size()));

    System.out.println();
  }

  private static void showSamples(
    final List<NTSampleType> samples)
  {
    for (var index = 0; index < samples.size(); ++index) {
      final var sample = samples.get(index);
      final var description = sample.description();

      System.out.printf(
        "sample %d %s : %s\n",
        Integer.valueOf(index),
        "name",
        description.name().value());

      System.out.printf(
        "sample %d %s : %s\n",
        Integer.valueOf(index),
        "type",
        description.kind());

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "sample rate",
        Integer.valueOf(description.sampleRate()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "start",
        Long.valueOf(description.start()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "end",
        Long.valueOf(description.end()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "loop start",
        Long.valueOf(description.loopStart()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "loop end",
        Long.valueOf(description.loopEnd()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "original pitch",
        Integer.valueOf(description.originalPitch().value()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "pitch correction",
        Integer.valueOf(description.pitchCorrection()));

      System.out.printf(
        "sample %d %s : %d\n",
        Integer.valueOf(index),
        "link",
        Integer.valueOf(description.sampleLink()));

      System.out.println();
    }
  }

  private static void showInfo(
    final NTInfo info)
  {
    System.out.printf(
      "%s : %s\n",
      "name",
      info.name().value());
    System.out.printf(
      "%s : %d.%d\n",
      "format version",
      Integer.valueOf(info.version().major()),
      Integer.valueOf(info.version().minor()));
    System.out.printf(
      "%s : %s\n",
      "sound engine",
      info.soundEngine().value());

    info.comment().ifPresent(
      comment -> System.out.printf("%s : %s\n", "comment", comment.value()));
    info.copyright().ifPresent(
      copyright -> showShortString("copyright", copyright));
    info.creationDate().ifPresent(
      creation -> showShortString("creation date", creation));
    info.engineers().ifPresent(
      engineers -> showShortString("engineers", engineers));
    info.product().ifPresent(
      product -> showShortString("product", product));
    info.rom().ifPresent(
      rom -> showShortString("rom", rom));

    info.romRevision().ifPresent(
      rom_revision ->
        System.out.printf(
          "%s : %d.%d\n",
          "rom revision",
          Integer.valueOf(rom_revision.major()),
          Integer.valueOf(rom_revision.minor())));

    info.software().ifPresent(
      software -> showShortString("software", software));

    System.out.println();
  }

  private static void showShortString(
    final String name,
    final NTShortString text)
  {
    System.out.printf("%s : %s\n", name, text.value());
  }

  CommandShow()
  {

  }
}
