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

package com.io7m.jnoisetype.tests;

import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.parser.api.NTParsedFile;
import com.io7m.jspiel.api.RiffChunkID;
import com.io7m.jspiel.api.RiffFileBuilderType;
import com.io7m.jspiel.api.RiffFileWriterDescriptionType;
import com.io7m.jspiel.api.RiffWriteException;
import com.io7m.jspiel.vanilla.RiffFileBuilders;
import com.io7m.jspiel.vanilla.RiffWriters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public abstract class NTParsersContract
{
  private RiffWriters writers;
  private RiffFileBuilders builders;
  private NTFileParserProviderType parsers;
  private Logger logger;

  private static Executable[] standardInfoChecks(
    final NTParsedFile file,
    final String expected_name)
  {
    return new Executable[]{
      () -> {
        Assertions.assertEquals(
          expected_name, file.info().name().value(), "Correct name");
      },
      () -> {
        Assertions.assertEquals(
          "jnoisetype", file.info().engineers().get().value(), "Correct eng");
      },
      () -> {
        Assertions.assertEquals(
          NTVersion.of(2, 1), file.info().version(), "Correct version");
      },
      () -> {
        Assertions.assertEquals(
          Optional.empty(), file.info().rom(), "No ROM");
      },
      () -> {
        Assertions.assertEquals(
          Optional.empty(), file.info().romRevision(), "No ROM revision");
      },
      () -> {
        Assertions.assertEquals(
          Optional.empty(), file.info().creationDate(), "No creation date");
      },
      () -> {
        Assertions.assertEquals(
          "jnoisetype product", file.info().product().get().value(), "Correct product");
      },
      () -> {
        Assertions.assertEquals(
          "Public Domain", file.info().copyright().get().value(), "Correct copyright");
      },
      () -> {
        Assertions.assertEquals(
          "Polyphone", file.info().software().get().value(), "No software");
      },
    };
  }

  protected abstract NTFileParserProviderType parsers();

  protected abstract Logger logger();

  @BeforeEach
  public final void testSetup()
  {
    this.logger = this.logger();
    this.parsers = this.parsers();
    this.writers = new RiffWriters();
    this.builders = new RiffFileBuilders();
  }

  /**
   * Parse the empty soundfont.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testEmpty()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("empty.sf2")) {
      final var parser = this.parsers.createForByteBuffer(map.name, map.map);
      final var file = parser.parse();

      Assertions.assertAll(standardInfoChecks(file, "Empty"));
      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            1, file.sampleRecords().size(), "1 sample record");

          final var record = file.sampleRecords().get(0);
          final var description = record.description();
          Assertions.assertEquals("EOS", description.name().value(), "Correct sample record name");
          Assertions.assertEquals(0, description.kind().value(), "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(0L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(0L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            0,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(0, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");
        },

        () -> {
          Assertions.assertEquals(1, file.instrumentRecords().size(), "1 instrument record");
          final var record = file.instrumentRecords().get(0);
          Assertions.assertEquals("EOI", record.name().value(), "Correct instrument record name");
          Assertions.assertEquals(0, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          Assertions.assertEquals(1, file.presetRecords().size(), "1 preset records");
          final var record = file.presetRecords().get(0);
          Assertions.assertEquals("EOP", record.name().value(), "Correct name");
          Assertions.assertEquals(0, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(0, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");
          Assertions.assertEquals(0L, record.genre(), "Correct genre");
          Assertions.assertEquals(0L, record.library(), "Correct library");
          Assertions.assertEquals(0L, record.morphology(), "Correct morphology");
        },

        () -> {
          Assertions.assertEquals(
            1,
            file.instrumentZoneRecords().size(),
            "1 instrument zone record");
          final var record = file.instrumentZoneRecords().get(0);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          Assertions.assertEquals(
            1,
            file.instrumentZoneGeneratorRecords().size(),
            "1 instrument zone generator record");
          final var record = file.instrumentZoneGeneratorRecords().get(0);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(0, record.generatorOperator(), "Correct operator");
        },

        () -> {
          Assertions.assertEquals(
            1,
            file.instrumentZoneModulatorRecords().size(),
            "1 instrument zone modulator record");
          final var record = file.instrumentZoneModulatorRecords().get(0);
          Assertions.assertEquals(0, record.modulationAmount(), "Correct amount");
          Assertions.assertEquals(0, record.modulationAmountSourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.modulationTransformOperator(), "Correct operator");
          Assertions.assertEquals(0, record.sourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.targetOperator(), "Correct operator");
        }
      );
    }
  }

  /**
   * Parse the inst1 soundfont.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testInst1()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("inst1.sf2")) {
      final var parser = this.parsers.createForByteBuffer(map.name, map.map);
      final var file = parser.parse();

      Assertions.assertAll(standardInfoChecks(file, "Inst1"));
      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            2, file.sampleRecords().size(), "2 sample records");
        },

        () -> {
          final var record = file.sampleRecords().get(0);
          final var description = record.description();
          Assertions.assertEquals(
            "000_60",
            description.name().value(),
            "Correct sample record name");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO.value(),
            description.kind().value(),
            "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(8269L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(8270L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            60,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(22050, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");

          Assertions.assertEquals(0xC2L, record.dataByteRange().lower());
          Assertions.assertEquals(0x415EL, record.dataByteRange().upper());
        },

        () -> {
          final var record = file.sampleRecords().get(1);
          final var description = record.description();
          Assertions.assertEquals("EOS", description.name().value(), "Correct sample record name");
          Assertions.assertEquals(0, description.kind().value(), "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(0L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(0L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            0,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(0, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");

          Assertions.assertEquals(0L, record.dataByteRange().lower());
          Assertions.assertEquals(0L, record.dataByteRange().upper());
        },

        () -> {
          Assertions.assertEquals(2, file.instrumentRecords().size(), "2 instrument records");
        },

        () -> {
          final var record = file.instrumentRecords().get(0);
          Assertions.assertEquals(
            "instrument0",
            record.name().value(),
            "Correct instrument record name");
          Assertions.assertEquals(0, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          final var record = file.instrumentRecords().get(1);
          Assertions.assertEquals("EOI", record.name().value(), "Correct instrument record name");
          Assertions.assertEquals(2, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          Assertions.assertEquals(1, file.presetRecords().size(), "1 preset records");
          final var record = file.presetRecords().get(0);
          Assertions.assertEquals("EOP", record.name().value(), "Correct name");
          Assertions.assertEquals(0, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(0, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");
          Assertions.assertEquals(0L, record.genre(), "Correct genre");
          Assertions.assertEquals(0L, record.library(), "Correct library");
          Assertions.assertEquals(0L, record.morphology(), "Correct morphology");
        },

        () -> {
          Assertions.assertEquals(
            3,
            file.instrumentZoneRecords().size(),
            "3 instrument zone records");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(0);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(1);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(2);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(2, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          Assertions.assertEquals(
            3,
            file.instrumentZoneGeneratorRecords().size(),
            "1 instrument zone generator record");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(0);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(17, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(1);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(53, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(2);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(0, record.generatorOperator(), "Correct operator");
        },

        () -> {
          Assertions.assertEquals(
            1,
            file.instrumentZoneModulatorRecords().size(),
            "1 instrument zone modulator record");
        },

        () -> {
          final var record = file.instrumentZoneModulatorRecords().get(0);
          Assertions.assertEquals(0, record.modulationAmount(), "Correct amount");
          Assertions.assertEquals(0, record.modulationAmountSourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.modulationTransformOperator(), "Correct operator");
          Assertions.assertEquals(0, record.sourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.targetOperator(), "Correct operator");
        }
      );
    }
  }

  /**
   * Parse the preset1 soundfont.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPreset1()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("preset1.sf2")) {
      final var parser = this.parsers.createForByteBuffer(map.name, map.map);
      final var file = parser.parse();

      Assertions.assertAll(standardInfoChecks(file, "Preset1"));
      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            2, file.sampleRecords().size(), "2 sample records");
        },

        () -> {
          final var record = file.sampleRecords().get(0);
          final var description = record.description();
          Assertions.assertEquals(
            "000_60",
            description.name().value(),
            "Correct sample record name");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO.value(),
            description.kind().value(),
            "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(8269L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(8270L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            60,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(22050, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");
        },

        () -> {
          final var record = file.sampleRecords().get(1);
          final var description = record.description();
          Assertions.assertEquals("EOS", description.name().value(), "Correct sample record name");
          Assertions.assertEquals(0, description.kind().value(), "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(0L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(0L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            0,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(0, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");
        },

        () -> {
          Assertions.assertEquals(2, file.instrumentRecords().size(), "2 instrument records");
        },

        () -> {
          final var record = file.instrumentRecords().get(0);
          Assertions.assertEquals(
            "instrument0",
            record.name().value(),
            "Correct instrument record name");
          Assertions.assertEquals(0, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          final var record = file.instrumentRecords().get(1);
          Assertions.assertEquals("EOI", record.name().value(), "Correct instrument record name");
          Assertions.assertEquals(2, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          Assertions.assertEquals(2, file.presetRecords().size(), "2 preset records");
        },

        () -> {
          final var record = file.presetRecords().get(0);
          Assertions.assertEquals("preset0", record.name().value(), "Correct name");
          Assertions.assertEquals(0, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(0, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");
          Assertions.assertEquals(0L, record.genre(), "Correct genre");
          Assertions.assertEquals(0L, record.library(), "Correct library");
          Assertions.assertEquals(0L, record.morphology(), "Correct morphology");
        },

        () -> {
          final var record = file.presetRecords().get(1);
          Assertions.assertEquals("EOP", record.name().value(), "Correct name");
          Assertions.assertEquals(2, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(0, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");
          Assertions.assertEquals(0L, record.genre(), "Correct genre");
          Assertions.assertEquals(0L, record.library(), "Correct library");
          Assertions.assertEquals(0L, record.morphology(), "Correct morphology");
        },

        () -> {
          Assertions.assertEquals(
            3,
            file.instrumentZoneRecords().size(),
            "3 instrument zone records");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(0);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(1);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(2);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(3, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          Assertions.assertEquals(
            4,
            file.instrumentZoneGeneratorRecords().size(),
            "4 instrument zone generator records");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(0);
          Assertions.assertEquals(32512, record.amount().value(), "Correct amount");
          Assertions.assertEquals(43, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(1);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(17, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(2);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(53, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(3);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(0, record.generatorOperator(), "Correct operator");
        },

        () -> {
          Assertions.assertEquals(
            1,
            file.instrumentZoneModulatorRecords().size(),
            "1 instrument zone modulator record");
        },

        () -> {
          final var record = file.instrumentZoneModulatorRecords().get(0);
          Assertions.assertEquals(0, record.modulationAmount(), "Correct amount");
          Assertions.assertEquals(0, record.modulationAmountSourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.modulationTransformOperator(), "Correct operator");
          Assertions.assertEquals(0, record.sourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.targetOperator(), "Correct operator");
        }
      );
    }
  }

  /**
   * Parse the complex0 soundfont.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testComplex0()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("complex0.sf2")) {
      final var parser = this.parsers.createForByteBuffer(map.name, map.map);
      final var file = parser.parse();

      Assertions.assertAll(standardInfoChecks(file, "Complex0"));
      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(3, file.sampleRecords().size(), "3 sample records");
        },

        () -> {
          final var record = file.sampleRecords().get(0);
          final var description = record.description();
          Assertions.assertEquals(
            "000_60",
            description.name().value(),
            "Correct sample record name");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO.value(),
            description.kind().value(),
            "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(8269L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(8270L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            60,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(22050, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");
        },

        () -> {
          final var record = file.sampleRecords().get(1);
          final var description = record.description();
          Assertions.assertEquals(
            "002_60",
            description.name().value(),
            "Correct sample record name");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO.value(),
            description.kind().value(),
            "Correct sample kind");
          Assertions.assertEquals(8316L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(16585L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(8316L, description.start(), "Correct sample start");
          Assertions.assertEquals(16586L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            60,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(22050, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");
        },

        () -> {
          final var record = file.sampleRecords().get(2);
          final var description = record.description();
          Assertions.assertEquals("EOS", description.name().value(), "Correct sample record name");
          Assertions.assertEquals(0, description.kind().value(), "Correct sample kind");
          Assertions.assertEquals(0L, description.loopStart(), "Correct sample loopStart");
          Assertions.assertEquals(0L, description.loopEnd(), "Correct sample loopEnd");
          Assertions.assertEquals(0L, description.start(), "Correct sample start");
          Assertions.assertEquals(0L, description.end(), "Correct sample end");
          Assertions.assertEquals(
            0,
            description.originalPitch().value(),
            "Correct sample originalPitch");
          Assertions.assertEquals(
            0,
            description.pitchCorrection(),
            "Correct sample pitchCorrection");
          Assertions.assertEquals(0, description.sampleRate(), "Correct sample sampleRate");
          Assertions.assertEquals(0, description.sampleLink(), "Correct sample sampleLink");
        },

        () -> {
          Assertions.assertEquals(4, file.instrumentRecords().size(), "2 instrument records");
        },

        () -> {
          final var record = file.instrumentRecords().get(0);
          Assertions.assertEquals(
            "instrument0",
            record.name().value(),
            "Correct instrument record name");
          Assertions.assertEquals(0, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          final var record = file.instrumentRecords().get(1);
          Assertions.assertEquals(
            "instrument1",
            record.name().value(),
            "Correct instrument record name");
          Assertions.assertEquals(2, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          final var record = file.instrumentRecords().get(2);
          Assertions.assertEquals(
            "instrument2",
            record.name().value(),
            "Correct instrument record name");
          Assertions.assertEquals(4, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          final var record = file.instrumentRecords().get(3);
          Assertions.assertEquals("EOI", record.name().value(), "Correct instrument record name");
          Assertions.assertEquals(7, record.instrumentZoneIndex(), "Correct instrument zone index");
        },

        () -> {
          Assertions.assertEquals(4, file.presetRecords().size(), "2 preset records");
        },

        () -> {
          final var record = file.presetRecords().get(0);
          Assertions.assertEquals("preset0", record.name().value(), "Correct name");
          Assertions.assertEquals(0, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(0, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");

          Assertions.assertEquals(0L, record.genre(), "Correct genre");
          Assertions.assertEquals(0L, record.library(), "Correct library");
          Assertions.assertEquals(0L, record.morphology(), "Correct morphology");
        },

        () -> {
          final var record = file.presetRecords().get(1);
          Assertions.assertEquals("preset1", record.name().value(), "Correct name");
          Assertions.assertEquals(2, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(1, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");

          // XXX: This appears to be a quirk of Polyphone (they should be 0)
          Assertions.assertEquals(2L, record.genre(), "Correct genre");
          Assertions.assertEquals(2L, record.library(), "Correct library");
          Assertions.assertEquals(2L, record.morphology(), "Correct morphology");
        },

        () -> {
          final var record = file.presetRecords().get(2);
          Assertions.assertEquals("preset2", record.name().value(), "Correct name");
          Assertions.assertEquals(4, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(2, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");

          // XXX: This appears to be a quirk of Polyphone (they should be 0)
          Assertions.assertEquals(4L, record.genre(), "Correct genre");
          Assertions.assertEquals(4L, record.library(), "Correct library");
          Assertions.assertEquals(4L, record.morphology(), "Correct morphology");
        },

        () -> {
          final var record = file.presetRecords().get(3);
          Assertions.assertEquals("EOP", record.name().value(), "Correct name");
          Assertions.assertEquals(6, record.presetBagIndex(), "Correct bag index");
          Assertions.assertEquals(0, record.preset(), "Correct preset");
          Assertions.assertEquals(0, record.bank(), "Correct bank");
          Assertions.assertEquals(0L, record.genre(), "Correct genre");
          Assertions.assertEquals(0L, record.library(), "Correct library");
          Assertions.assertEquals(0L, record.morphology(), "Correct morphology");
        },

        () -> {
          Assertions.assertEquals(
            8,
            file.instrumentZoneRecords().size(),
            "8 instrument zone records");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(0);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(1);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(0, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(2);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(3, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(3);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(3, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(4);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(6, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(5);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(6, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(6);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(9, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          final var record = file.instrumentZoneRecords().get(7);
          Assertions.assertEquals(0, record.modulatorIndex(), "Correct modulator index");
          Assertions.assertEquals(12, record.generatorIndex(), "Correct generator index");
        },

        () -> {
          Assertions.assertEquals(
            13,
            file.instrumentZoneGeneratorRecords().size(),
            "13 instrument zone generator records");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(0);
          Assertions.assertEquals(32512, record.amount().value(), "Correct amount");
          Assertions.assertEquals(43, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(1);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(17, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(2);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(53, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(3);
          Assertions.assertEquals(32512, record.amount().value(), "Correct amount");
          Assertions.assertEquals(43, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(4);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(17, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(5);
          Assertions.assertEquals(1, record.amount().value(), "Correct amount");
          Assertions.assertEquals(53, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(6);
          Assertions.assertEquals(32512, record.amount().value(), "Correct amount");
          Assertions.assertEquals(43, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(7);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(17, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(8);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(53, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(9);
          Assertions.assertEquals(32512, record.amount().value(), "Correct amount");
          Assertions.assertEquals(43, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(10);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(17, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(11);
          Assertions.assertEquals(1, record.amount().value(), "Correct amount");
          Assertions.assertEquals(53, record.generatorOperator(), "Correct operator");
        },

        () -> {
          final var record = file.instrumentZoneGeneratorRecords().get(12);
          Assertions.assertEquals(0, record.amount().value(), "Correct amount");
          Assertions.assertEquals(0, record.generatorOperator(), "Correct operator");
        },

        () -> {
          Assertions.assertEquals(
            1,
            file.instrumentZoneModulatorRecords().size(),
            "1 instrument zone modulator record");
        },

        () -> {
          final var record = file.instrumentZoneModulatorRecords().get(0);
          Assertions.assertEquals(0, record.modulationAmount(), "Correct amount");
          Assertions.assertEquals(0, record.modulationAmountSourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.modulationTransformOperator(), "Correct operator");
          Assertions.assertEquals(0, record.sourceOperator(), "Correct operator");
          Assertions.assertEquals(0, record.targetOperator(), "Correct operator");
        }
      );
    }
  }

  /**
   * The form type is wrong.
   */

  @Test
  public final void testWrongFormType()
  {
    final var builder = this.builders.create(ByteOrder.LITTLE_ENDIAN);
    try (var root = builder.setRootChunk(RiffChunkID.of("RIFF"), "badx")) {
      root.addSubChunk(RiffChunkID.of("xyza"));
    }

    this.expectThrows(builder, "RIFF form type must be sfbk");
  }

  @Test
  public final void testMissingChunkLIST_INFO()
  {
    final var builder = this.builders.create(ByteOrder.LITTLE_ENDIAN);
    try (var root = builder.setRootChunk(RiffChunkID.of("RIFF"), "sfbk")) {
      root.addSubChunk(RiffChunkID.of("zzzz"));
    }
    this.expectThrows(builder, "Expected: LIST (form INFO)");
  }

  @Test
  public final void testMissingChunkLIST_pdta()
  {
    final var builder = this.builders.create(ByteOrder.LITTLE_ENDIAN);
    try (var root = builder.setRootChunk(RiffChunkID.of("RIFF"), "sfbk")) {
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("INFO");
        sc.addSubChunk(RiffChunkID.of("zzzz"));
      }
    }
    this.expectThrows(builder, "Expected: LIST (form pdta) as a subchunk of RIFF");
  }

  @Test
  public final void testMissingChunkLIST_sdta()
  {
    final var builder = this.builders.create(ByteOrder.LITTLE_ENDIAN);
    try (var root = builder.setRootChunk(RiffChunkID.of("RIFF"), "sfbk")) {
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("INFO");
        sc.addSubChunk(RiffChunkID.of("zzzz"));
      }
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("pdta");
        sc.addSubChunk(RiffChunkID.of("zzzz"));
      }
    }
    this.expectThrows(builder, "Expected: LIST (form sdta) as a subchunk of RIFF");
  }

  @Test
  public final void testMissingChunkLIST_pdta_phdr()
  {
    final var builder = this.builders.create(ByteOrder.LITTLE_ENDIAN);
    try (var root = builder.setRootChunk(RiffChunkID.of("RIFF"), "sfbk")) {
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("INFO");
        sc.addSubChunk(RiffChunkID.of("zzzz"));
      }
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("pdta");
        sc.addSubChunk(RiffChunkID.of("zzzz"));
      }
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("sdta");
        sc.addSubChunk(RiffChunkID.of("smpl"));
      }
    }
    this.expectThrows(builder, "Chunk name: phdr");
  }

  @Test
  public final void testChunkPhdrTerminal()
  {
    final var builder = this.builders.create(ByteOrder.LITTLE_ENDIAN);
    try (var root = builder.setRootChunk(RiffChunkID.of("RIFF"), "sfbk")) {
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("INFO");
        sc.addSubChunk(RiffChunkID.of("zzzz"));
      }
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("pdta");
        sc.addSubChunk(RiffChunkID.of("phdr"));
      }
      try (var sc = root.addSubChunk(RiffChunkID.of("LIST"))) {
        sc.setForm("sdta");
        sc.addSubChunk(RiffChunkID.of("smpl"));
      }
    }
    this.expectThrows(builder, "A terminal record is required but was not present");
  }

  private void expectThrows(
    final RiffFileBuilderType builder,
    final String expected)
  {
    final var ex = Assertions.assertThrows(NTParseException.class, () -> {
      this.serializeAndParseFont(this.parsers, this.writers, builder.build());
    });

    this.logger.debug("expectThrows: ", ex);
    Assertions.assertTrue(ex.getMessage().contains(expected));
  }

  private void serializeAndParseFont(
    final NTFileParserProviderType parsers,
    final RiffWriters writers,
    final RiffFileWriterDescriptionType built)
    throws IOException, RiffWriteException, NTParseException
  {
    final var path = NTTestDirectories.createTempFile("ntparsers-", ".sf2");
    try (final var channel = FileChannel.open(path, READ, WRITE, CREATE, TRUNCATE_EXISTING)) {
      final var writer = writers.createForChannel(path.toUri(), built, channel);
      writer.write();

      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = parsers.createForByteBuffer(path.toUri(), map);
      parser.parse();
    }
  }

  private static final class NamedMap implements Closeable
  {
    private final URI name;
    private final FileChannel channel;
    private final ByteBuffer map;

    private NamedMap(
      final URI in_name,
      final FileChannel in_channel,
      final ByteBuffer in_map)
    {
      this.name = Objects.requireNonNull(in_name, "name");
      this.channel = Objects.requireNonNull(in_channel, "channel");
      this.map = Objects.requireNonNull(in_map, "map");
    }

    public static NamedMap createFromResource(
      final String name)
      throws IOException
    {
      final var resource_path = "/com/io7m/jnoisetype/tests/" + name;
      try (var input = NTParsersContract.class.getResourceAsStream(resource_path)) {
        final var path = NTTestDirectories.createTempFile("ntparsers-", ".sf2");
        try (var output = Files.newOutputStream(path, WRITE, TRUNCATE_EXISTING, CREATE)) {
          input.transferTo(output);
          output.flush();
        }

        final var channel = FileChannel.open(path, READ);
        final var map = channel.map(READ_ONLY, 0L, channel.size());
        return new NamedMap(path.toUri(), channel, map);
      }
    }

    @Override
    public void close()
      throws IOException
    {
      this.channel.close();
    }
  }
}