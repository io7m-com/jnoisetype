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

import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.parser.api.NTInterpreterProviderType;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jspiel.api.RiffFileBuilderType;
import com.io7m.jspiel.api.RiffFileWriterDescriptionType;
import com.io7m.jspiel.api.RiffWriteException;
import com.io7m.jspiel.vanilla.RiffFileBuilders;
import com.io7m.jspiel.vanilla.RiffWriters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public abstract class NTInterpretersContract
{
  private RiffWriters writers;
  private RiffFileBuilders builders;
  private NTFileParserProviderType parsers;
  private Logger logger;
  private NTInterpreterProviderType interpreters;

  protected abstract NTInterpreterProviderType interpreters();

  protected abstract NTFileParserProviderType parsers();

  protected abstract Logger logger();

  @BeforeEach
  public final void testSetup()
  {
    this.logger = this.logger();
    this.interpreters = this.interpreters();
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
      final var parser = this.parsers.createForByteBuffer(
        map.name.toUri(),
        map.map);
      final var file = parser.parse();
      final var interpreted = this.interpreters.createInterpreter(file).interpret();

      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            file.info(),
            interpreted.info(),
            "Correct info");
        },

        () -> {
          Assertions.assertEquals(
            0,
            interpreted.instruments().size(),
            "Correct instruments");
        },

        () -> {
          Assertions.assertEquals(
            0,
            interpreted.presets().size(),
            "Correct presets");
        },

        () -> {
          Assertions.assertEquals(
            0,
            interpreted.samples().size(),
            "Correct samples");
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
      final var parser = this.parsers.createForByteBuffer(
        map.name.toUri(),
        map.map);
      final var file = parser.parse();
      final var interpreted = this.interpreters.createInterpreter(file).interpret();

      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            file.info(),
            interpreted.info(),
            "Correct info");
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.instruments().size(),
            "Correct instruments");
        },

        () -> {
          final var instrument = interpreted.instruments().get(0);
          Assertions.assertEquals(
            "instrument0",
            instrument.nameText(),
            "Correct instrument name");
          final var zones = instrument.zones();
          Assertions.assertEquals(2, zones.size(), "Correct instrument zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            instrument,
            zone0.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            instrument,
            zone1.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            2,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
        },

        () -> {
          Assertions.assertEquals(
            0,
            interpreted.presets().size(),
            "Correct presets");
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.samples().size(),
            "Correct samples");
        },

        () -> {
          final var sample = interpreted.samples().get(0);
          Assertions.assertEquals(
            "000_60",
            sample.nameText(),
            "Correct sample name");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO,
            sample.description().kind(),
            "Correct sample kind");
        }
      );
    }
  }

  /**
   * Interpret the preset1 soundfont.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPreset1()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("preset1.sf2")) {
      final var parser = this.parsers.createForByteBuffer(
        map.name.toUri(),
        map.map);
      final var file = parser.parse();
      final var interpreted = this.interpreters.createInterpreter(file).interpret();

      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            file.info(),
            interpreted.info(),
            "Correct info");
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.instruments().size(),
            "Correct instruments");
        },

        () -> {
          final var instrument = interpreted.instruments().get(0);
          Assertions.assertEquals(
            "instrument0",
            instrument.nameText(),
            "Correct instrument name");
          final var zones = instrument.zones();
          Assertions.assertEquals(2, zones.size(), "Correct instrument zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            instrument,
            zone0.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            instrument,
            zone1.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            3,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          final var gen2 = zone1.generators().get(2);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen2.zone(), "Correct generator zone");
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.presets().size(),
            "Correct presets");
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.samples().size(),
            "Correct samples");
        },

        () -> {
          final var sample = interpreted.samples().get(0);
          Assertions.assertEquals(
            "000_60",
            sample.nameText(),
            "Correct sample name");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO,
            sample.description().kind(),
            "Correct sample kind");
        }
      );
    }
  }

  /**
   * Interpret the complex0 soundfont.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testComplex0()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("complex0.sf2")) {
      final var parser = this.parsers.createForByteBuffer(
        map.name.toUri(),
        map.map);
      final var file = parser.parse();
      final var interpreted = this.interpreters.createInterpreter(file).interpret();

      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            file.info(),
            interpreted.info(),
            "Correct info");
        },

        () -> {
          Assertions.assertEquals(
            3,
            interpreted.instruments().size(),
            "Correct instruments");
        },

        () -> {
          final var instrument = interpreted.instruments().get(0);
          Assertions.assertEquals(
            "instrument0",
            instrument.nameText(),
            "Correct instrument name");
          final var zones = instrument.zones();
          Assertions.assertEquals(2, zones.size(), "Correct instrument zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            instrument,
            zone0.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            instrument,
            zone1.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            3,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          final var gen2 = zone1.generators().get(2);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen2.zone(), "Correct generator zone");
        },

        () -> {
          final var instrument = interpreted.instruments().get(1);
          Assertions.assertEquals(
            "instrument1",
            instrument.nameText(),
            "Correct instrument name");
          final var zones = instrument.zones();
          Assertions.assertEquals(2, zones.size(), "Correct instrument zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            instrument,
            zone0.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            instrument,
            zone1.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            3,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          final var gen2 = zone1.generators().get(2);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen2.zone(), "Correct generator zone");
        },

        () -> {
          final var instrument = interpreted.instruments().get(2);
          Assertions.assertEquals(
            "instrument2",
            instrument.nameText(),
            "Correct instrument name");
          final var zones = instrument.zones();
          Assertions.assertEquals(3, zones.size(), "Correct instrument zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          final var zone2 = zones.get(2);
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone2.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            instrument,
            zone0.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            instrument,
            zone1.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            instrument,
            zone2.instrument(),
            "Correct zone instrument");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            3,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            3,
            zone2.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone2.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          final var gen2 = zone1.generators().get(2);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen2.zone(), "Correct generator zone");
        },

        () -> {
          Assertions.assertEquals(
            3,
            interpreted.presets().size(),
            "Correct presets");
        },

        () -> {
          final var preset = interpreted.presets().get(0);
          Assertions.assertEquals(
            "preset0",
            preset.nameText(),
            "Correct preset name");
          final var zones = preset.zones();
          Assertions.assertEquals(2, zones.size(), "Correct preset zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertEquals(
            preset,
            zone0.preset(),
            "Correct zone preset");
          Assertions.assertEquals(
            preset,
            zone1.preset(),
            "Correct zone preset");
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            2,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
        },

        () -> {
          final var preset = interpreted.presets().get(1);
          Assertions.assertEquals(
            "preset1",
            preset.nameText(),
            "Correct preset name");
          final var zones = preset.zones();
          Assertions.assertEquals(2, zones.size(), "Correct preset zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertEquals(
            preset,
            zone0.preset(),
            "Correct zone preset");
          Assertions.assertEquals(
            preset,
            zone1.preset(),
            "Correct zone preset");
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            2,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
        },

        () -> {
          final var preset = interpreted.presets().get(2);
          Assertions.assertEquals(
            "preset2",
            preset.nameText(),
            "Correct preset name");
          final var zones = preset.zones();
          Assertions.assertEquals(2, zones.size(), "Correct preset zones");
          final var zone0 = zones.get(0);
          final var zone1 = zones.get(1);
          Assertions.assertEquals(
            preset,
            zone0.preset(),
            "Correct zone preset");
          Assertions.assertEquals(
            preset,
            zone1.preset(),
            "Correct zone preset");
          Assertions.assertTrue(zone0.isGlobal(), "Correct global zone");
          Assertions.assertFalse(zone1.isGlobal(), "Correct global zone");
          Assertions.assertEquals(
            0,
            zone0.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone0.modulators().size(),
            "Correct zone modulators");
          Assertions.assertEquals(
            2,
            zone1.generators().size(),
            "Correct zone generators");
          Assertions.assertEquals(
            0,
            zone1.modulators().size(),
            "Correct zone modulators");

          final var gen0 = zone1.generators().get(0);
          final var gen1 = zone1.generators().get(1);
          Assertions.assertEquals(zone1, gen0.zone(), "Correct generator zone");
          Assertions.assertEquals(zone1, gen1.zone(), "Correct generator zone");
        },

        () -> {
          Assertions.assertEquals(
            2,
            interpreted.samples().size(),
            "Correct samples");
        },

        () -> {
          final var sample = interpreted.samples().get(0);
          Assertions.assertEquals(
            "000_60",
            sample.nameText(),
            "Correct sample");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO,
            sample.description().kind(),
            "Correct sample kind");
        },

        () -> {
          final var sample = interpreted.samples().get(1);
          Assertions.assertEquals(
            "002_60",
            sample.nameText(),
            "Correct sample");
          Assertions.assertEquals(
            NTSampleKind.SAMPLE_KIND_MONO,
            sample.description().kind(),
            "Correct sample kind");
        }
      );
    }
  }

  /**
   * @throws Exception On errors
   * @see "https://github.com/io7m/jnoisetype/issues/5"
   */

  @Test
  public final void testBug5()
    throws Exception
  {
    try (var map = NamedMap.createFromResource("unbolted_min.sf2")) {
      final var parser = this.parsers.createForByteBuffer(
        map.name.toUri(),
        map.map);
      final var file = parser.parse();
      final var interpreted = this.interpreters.createInterpreter(file).interpret();

      Assertions.assertAll(
        () -> {
          Assertions.assertEquals(
            file.info(),
            interpreted.info(),
            "Correct info");
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.instruments().size(),
            "Correct instruments");
        },

        () -> {
          final var instrument = interpreted.instruments().get(0);
          Assertions.assertEquals(17L, instrument.zones().size());

          final var zone0 = instrument.zones().get(0);
          Assertions.assertTrue(zone0.isGlobal());
          Assertions.assertEquals(1, zone0.modulators().size());

          final var modulator0 = zone0.modulators().get(0);
          Assertions.assertEquals(20, modulator0.modulationAmount());
          Assertions.assertEquals(
            0,
            modulator0.modulationAmountSourceOperator());
          Assertions.assertEquals(14, modulator0.sourceOperator());
          Assertions.assertEquals(
            51,
            modulator0.targetOperator().index().value());
          Assertions.assertEquals(
            0,
            modulator0.modulationTransformOperator().index().value());
        },

        () -> {
          Assertions.assertEquals(
            1,
            interpreted.presets().size(),
            "Correct presets");
        },

        () -> {
          Assertions.assertEquals(
            16,
            interpreted.samples().size(),
            "Correct samples");
        }
      );
    }
  }

  /**
   * Try various corrupted soundfonts.
   *
   * @return A list of tests
   */

  @TestFactory
  public final List<DynamicTest> testCorruption()
  {
    return LongStream.range(0L, 10_000L)
      .mapToObj(seed -> DynamicTest.dynamicTest(
        "testCorruptionWithSeed" + seed,
        () -> {
          this.corruptOne(seed);
        }))
      .collect(Collectors.toList());
  }

  /**
   * Test corruption for a single value.
   *
   * @throws IOException On errors
   */

  @Test
  public final void testCorruptOne()
    throws IOException
  {
    this.corruptOne(10_001L);
  }

  private void corruptOne(
    final long seed)
    throws IOException
  {
    final var map = NamedMap.createFromResource("complex0.sf2");

    try {
      final var corrupted_map = corruptMap(this.logger, map, seed);
      final var parser =
        this.parsers.createForByteBuffer(map.name.toUri(), corrupted_map);
      final var file = parser.parse();
      this.interpreters.createInterpreter(file).interpret();
    } catch (final NTParseException e) {
      this.logger.debug("parsing: ", e);
    } catch (final RuntimeException e) {
      Assertions.fail(e);
    } finally {
      try {
        try {
          map.close();
        } catch (final IOException e) {
          // This can happen on Windows
        }

        Files.delete(map.name);
      } catch (final IOException e) {
        // This can happen on Windows
      }
    }
  }

  private static ByteBuffer corruptMap(
    final Logger logger,
    final NamedMap map,
    final long seed)
  {
    final var corrupted_map = ByteBuffer.allocate(map.map.capacity());
    corrupted_map.put(map.map);
    corrupted_map.position(0);

    final var rng = new Random(seed);
    final var corruption = rng.nextDouble() * 0.01;

    logger.debug(
      "seed {}: corrupting {}% of the input bytes",
      Long.valueOf(seed),
      String.format("%.2f", Double.valueOf(corruption * 100.0)));

    final var bytes = new byte[1];
    for (var index = 0; index < corrupted_map.capacity(); ++index) {
      if (rng.nextDouble() < corruption) {
        rng.nextBytes(bytes);
        corrupted_map.put(index, bytes[0]);
      }
    }
    return corrupted_map;
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
    try (final var channel = FileChannel.open(
      path,
      READ,
      WRITE,
      CREATE,
      TRUNCATE_EXISTING)) {
      final var writer = writers.createForChannel(path.toUri(), built, channel);
      writer.write();

      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = parsers.createForByteBuffer(path.toUri(), map);
      parser.parse();
    }
  }

  private static final class NamedMap implements Closeable
  {
    private final Path name;
    private final FileChannel channel;
    private final ByteBuffer map;

    private NamedMap(
      final Path in_name,
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
      try (var input = NTInterpretersContract.class.getResourceAsStream(
        resource_path)) {
        final var path = NTTestDirectories.createTempFile("ntparsers-", ".sf2");
        try (var output = Files.newOutputStream(
          path,
          WRITE,
          TRUNCATE_EXISTING,
          CREATE)) {
          input.transferTo(output);
          output.flush();
        }
        final var channel = FileChannel.open(path, READ);
        final var map = channel.map(READ_ONLY, 0L, channel.size());
        return new NamedMap(path, channel, map);
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