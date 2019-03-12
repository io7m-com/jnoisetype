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

import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGeneratorOperatorIndex;
import com.io7m.jnoisetype.api.NTGenerators;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTLongString;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTTransforms;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.parser.api.NTInterpreterProviderType;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.writer.api.NTBuilderProviderType;
import com.io7m.jnoisetype.writer.api.NTWriterProviderType;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public abstract class NTWritersContract
{
  private NTBuilderProviderType builders;
  private NTWriterProviderType writers;
  private Logger logger;
  private NTInterpreterProviderType interpreters;
  private NTFileParserProviderType parsers;

  private static void compareFont(
    final NTFontType expected,
    final NTFontType received)
  {
    Assertions.assertEquals(
      expected.info(),
      received.info(),
      "Parsed info matches");

    final var expected_inst = expected.instruments();
    final var received_inst = received.instruments();

    Assertions.assertEquals(
      expected_inst.size(),
      received_inst.size(),
      "Parsed instrument counts match");

    for (var instrument_index = 0; instrument_index < expected_inst.size(); ++instrument_index) {
      final var ei = expected_inst.get(instrument_index);
      final var ri = received_inst.get(instrument_index);

      Assertions.assertEquals(
        ei.name(),
        ri.name(),
        "Instrument name matches");

      Assertions.assertEquals(
        ei.zones().size(),
        ri.zones().size(),
        "Instrument zone counts match");

      for (var zone_index = 0; zone_index < ei.zones().size(); ++zone_index) {
        final var ez = ei.zones().get(zone_index);
        final var rz = ri.zones().get(zone_index);
        Assertions.assertEquals(ez.generators(), rz.generators(), "Zone generators match");
        Assertions.assertEquals(ez.modulators(), rz.modulators(), "Zone modulators match");
      }
    }

    final var expected_samp = expected.samples();
    final var received_samp = received.samples();
    Assertions.assertEquals(
      expected_samp.size(),
      received_samp.size(),
      "Parsed sample counts match");

    for (var sample_index = 0; sample_index < expected_inst.size(); ++sample_index) {
      final var es = expected_samp.get(sample_index);
      final var rs = received_samp.get(sample_index);
      Assertions.assertEquals(es.nameText(), rs.nameText(), "Sample name matches");

      Assertions.assertEquals(
        es.description(),
        rs.description(),
        "Sample description matches");
    }

    final var expected_pre = expected.presets();
    final var received_pre = received.presets();
    Assertions.assertEquals(
      expected_pre.size(),
      received_pre.size(),
      "Parsed preset counts match");

    for (var preset_index = 0; preset_index < expected_pre.size(); ++preset_index) {
      final var es = expected_pre.get(preset_index);
      final var rs = received_pre.get(preset_index);

      Assertions.assertEquals(
        es.nameText(),
        rs.nameText(),
        "Preset name matches");

      Assertions.assertEquals(
        es.zones().size(),
        rs.zones().size(),
        "Preset zone counts match");

      for (var zone_index = 0; zone_index < es.zones().size(); ++zone_index) {
        final var ez = es.zones().get(zone_index);
        final var rz = rs.zones().get(zone_index);
        Assertions.assertEquals(ez.generators(), rz.generators(), "Zone generators match");
        Assertions.assertEquals(ez.modulators(), rz.modulators(), "Zone modulators match");
      }
    }
  }

  private static void copyWav(
    final String name,
    final SeekableByteChannel channel)
    throws IOException
  {
    final var url = resourcePath(name);

    try (var stream = AudioSystem.getAudioInputStream(url)) {
      stream.transferTo(Channels.newOutputStream(channel));
    } catch (final UnsupportedAudioFileException e) {
      throw new IOException(e);
    }
  }

  private static URL resourcePath(
    final String name)
    throws FileNotFoundException
  {
    final var path = "/com/io7m/jnoisetype/tests/" + name;
    final var url = NTWritersContract.class.getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    return url;
  }

  private static long sizeOfWav(
    final String name)
    throws IOException
  {
    try (var stream = AudioSystem.getAudioInputStream(resourcePath(name))) {
      try (var count = new CountingInputStream(stream)) {
        count.transferTo(new NullOutputStream());
        return count.getByteCount() / 2L;
      }
    } catch (final UnsupportedAudioFileException e) {
      throw new IOException(e);
    }
  }

  protected abstract NTInterpreterProviderType interpreters();

  protected abstract NTFileParserProviderType parsers();

  protected abstract NTBuilderProviderType builders();

  protected abstract NTWriterProviderType writers();

  protected abstract Logger logger();

  @BeforeEach
  public final void testSetup()
  {
    this.interpreters = this.interpreters();
    this.parsers = this.parsers();
    this.builders = this.builders();
    this.writers = this.writers();
    this.logger = this.logger();
  }

  /**
   * Attempt to reproduce the empty specimen file.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testEmpty()
    throws Exception
  {
    final var path = Files.createTempFile("nt-writer-", ".sf2");
    this.logger.debug("output: {}", path);
    final var expected = this.parseResource("empty.sf2", path.toUri());

    final var builder =
      this.builders.createBuilder();

    builder.setInfo(
      NTInfo.builder()
        .setName(NTShortString.of("Empty"))
        .setSoundEngine(NTShortString.of("EMU8000"))
        .setVersion(NTVersion.of(2, 1))
        .setEngineers(NTShortString.of("jnoisetype"))
        .setProduct(NTShortString.of("jnoisetype product"))
        .setCopyright(NTShortString.of("Public Domain"))
        .setComment(NTLongString.of("A comment."))
        .setSoftware(NTShortString.of("Polyphone"))
        .build());

    final var description = builder.build();

    try (var channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
      final var writer = this.writers.createForChannel(path.toUri(), description, channel);
      writer.write();
    }

    final var parsed = this.parse(path);
    compareFont(expected, parsed);
  }

  /**
   * Attempt to reproduce the sample0 specimen file.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testSample0()
    throws Exception
  {
    final var path = Files.createTempFile("nt-writer-", ".sf2");
    this.logger.debug("output: {}", path);
    final var expected = this.parseResource("sample0.sf2", path.toUri());

    final var builder = this.builders.createBuilder();

    builder.setInfo(
      NTInfo.builder()
        .setName(NTShortString.of("Sample0"))
        .setSoundEngine(NTShortString.of("EMU8000"))
        .setVersion(NTVersion.of(2, 1))
        .setEngineers(NTShortString.of("jnoisetype"))
        .setProduct(NTShortString.of("jnoisetype product"))
        .setCopyright(NTShortString.of("Public Domain"))
        .setComment(NTLongString.of("A comment."))
        .setSoftware(NTShortString.of("Polyphone"))
        .build());

    builder.addSample("000_60")
      .setSampleCount(sizeOfWav("000_60.wav"))
      .setLoopEnd(8269L)
      .setDataWriter(channel -> copyWav("000_60.wav", channel));

    final var description = builder.build();

    try (var channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
      final var writer = this.writers.createForChannel(path.toUri(), description, channel);
      writer.write();
    }

    final var parsed = this.parse(path);
    compareFont(expected, parsed);
  }

  /**
   * Attempt to reproduce the inst1 specimen file.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testInst1()
    throws Exception
  {
    final var path = Files.createTempFile("nt-writer-", ".sf2");
    this.logger.debug("output: {}", path);
    final var expected = this.parseResource("inst1.sf2", path.toUri());

    final var builder = this.builders.createBuilder();

    builder.setInfo(
      NTInfo.builder()
        .setName(NTShortString.of("Inst1"))
        .setSoundEngine(NTShortString.of("EMU8000"))
        .setVersion(NTVersion.of(2, 1))
        .setEngineers(NTShortString.of("jnoisetype"))
        .setProduct(NTShortString.of("jnoisetype product"))
        .setCopyright(NTShortString.of("Public Domain"))
        .setComment(NTLongString.of("A comment."))
        .setSoftware(NTShortString.of("Polyphone"))
        .build());

    final var sample0 =
      builder.addSample("000_60")
        .setSampleCount(sizeOfWav("000_60.wav"))
        .setSampleRate(22050)
        .setLoopEnd(8269L)
        .setDataWriter(channel -> copyWav("000_60.wav", channel));

    final var instrument0 = builder.addInstrument("instrument0");
    final var zone0 = instrument0.addZone();
    final var zone1 =
      instrument0.addZone()
        .addGenerator(NTGenerator.of(NTGeneratorOperatorIndex.of(17), "pan"), NTGenericAmount.of(0))
        .addSampleGenerator(sample0);

    final var description = builder.build();

    try (var channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
      final var writer = this.writers.createForChannel(path.toUri(), description, channel);
      writer.write();
    }

    final var parsed = this.parse(path);
    compareFont(expected, parsed);
  }

  /**
   * Attempt to reproduce the preset1 specimen file.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPreset1()
    throws Exception
  {
    final var path = Files.createTempFile("nt-writer-", ".sf2");
    this.logger.debug("output: {}", path);
    final var expected = this.parseResource("preset1.sf2", path.toUri());

    final var builder = this.builders.createBuilder();

    builder.setInfo(
      NTInfo.builder()
        .setName(NTShortString.of("Preset1"))
        .setSoundEngine(NTShortString.of("EMU8000"))
        .setVersion(NTVersion.of(2, 1))
        .setEngineers(NTShortString.of("jnoisetype"))
        .setProduct(NTShortString.of("jnoisetype product"))
        .setCopyright(NTShortString.of("Public Domain"))
        .setComment(NTLongString.of("A comment."))
        .setSoftware(NTShortString.of("Polyphone"))
        .build());

    final var sample0 =
      builder.addSample("000_60")
        .setSampleCount(sizeOfWav("000_60.wav"))
        .setSampleRate(22050)
        .setLoopEnd(8269L)
        .setDataWriter(channel -> copyWav("000_60.wav", channel));

    final var instrument0 = builder.addInstrument("instrument0");
    final var zone0 = instrument0.addZone();

    final var zone1 =
      instrument0.addZone()
        .addKeyRangeGenerator(0, 127)
        .addGenerator(NTGenerator.of(NTGeneratorOperatorIndex.of(17), "pan"), NTGenericAmount.of(0))
        .addSampleGenerator(sample0);

    final var preset0 = builder.addPreset("preset0");
    final var preset_zone0 = preset0.addZone();

    final var preset_zone1 =
      preset0.addZone()
        .addKeyRangeGenerator(0, 127)
        .addInstrumentGenerator(instrument0);

    final var description = builder.build();

    try (var channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
      final var writer = this.writers.createForChannel(path.toUri(), description, channel);
      writer.write();
    }

    final var parsed = this.parse(path);
    compareFont(expected, parsed);
  }

  /**
   * Attempt to reproduce the inst1_with_modulator specimen file.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testInst1WithModulator()
    throws Exception
  {
    final var path = Files.createTempFile("nt-writer-", ".sf2");
    this.logger.debug("output: {}", path);
    final var expected = this.parseResource("inst1_with_modulator.sf2", path.toUri());

    final var builder = this.builders.createBuilder();

    builder.setInfo(
      NTInfo.builder()
        .setName(NTShortString.of("Preset1"))
        .setSoundEngine(NTShortString.of("EMU8000"))
        .setVersion(NTVersion.of(2, 1))
        .setEngineers(NTShortString.of("jnoisetype"))
        .setProduct(NTShortString.of("jnoisetype product"))
        .setCopyright(NTShortString.of("Public Domain"))
        .setComment(NTLongString.of("A comment."))
        .setSoftware(NTShortString.of("Polyphone"))
        .build());

    final var sample0 =
      builder.addSample("000_60")
        .setSampleCount(sizeOfWav("000_60.wav"))
        .setSampleRate(22050)
        .setLoopEnd(8269L)
        .setDataWriter(channel -> copyWav("000_60.wav", channel));

    final var instrument0 = builder.addInstrument("instrument0");
    final var zone0 = instrument0.addZone();
    zone0.addModulator(
      14,
      NTGenerators.findForName("coarseTune").get(),
      (short) 20,
      0,
      NTTransforms.find(0));

    final var zone1 =
      instrument0.addZone()
        .addKeyRangeGenerator(0, 127)
        .addGenerator(NTGenerator.of(NTGeneratorOperatorIndex.of(17), "pan"), NTGenericAmount.of(0))
        .addSampleGenerator(sample0);

    final var preset0 = builder.addPreset("preset0");
    final var preset_zone0 = preset0.addZone();

    final var preset_zone1 =
      preset0.addZone()
        .addKeyRangeGenerator(0, 127)
        .addInstrumentGenerator(instrument0);

    final var description = builder.build();

    try (var channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
      final var writer = this.writers.createForChannel(path.toUri(), description, channel);
      writer.write();
    }

    final var parsed = this.parse(path);
    compareFont(expected, parsed);
  }

  /**
   * Attempt to reproduce the preset1_with_modulator specimen file.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testPreset1WithModulator()
    throws Exception
  {
    final var path = Files.createTempFile("nt-writer-", ".sf2");
    this.logger.debug("output: {}", path);
    final var expected = this.parseResource("preset1_with_modulator.sf2", path.toUri());

    final var builder = this.builders.createBuilder();

    builder.setInfo(
      NTInfo.builder()
        .setName(NTShortString.of("Preset1"))
        .setSoundEngine(NTShortString.of("EMU8000"))
        .setVersion(NTVersion.of(2, 1))
        .setEngineers(NTShortString.of("jnoisetype"))
        .setProduct(NTShortString.of("jnoisetype product"))
        .setCopyright(NTShortString.of("Public Domain"))
        .setComment(NTLongString.of("A comment."))
        .setSoftware(NTShortString.of("Polyphone"))
        .build());

    final var sample0 =
      builder.addSample("000_60")
        .setSampleCount(sizeOfWav("000_60.wav"))
        .setSampleRate(22050)
        .setLoopEnd(8269L)
        .setDataWriter(channel -> copyWav("000_60.wav", channel));

    final var instrument0 = builder.addInstrument("instrument0");
    final var zone0 = instrument0.addZone();

    final var zone1 =
      instrument0.addZone()
        .addKeyRangeGenerator(0, 127)
        .addGenerator(NTGenerator.of(NTGeneratorOperatorIndex.of(17), "pan"), NTGenericAmount.of(0))
        .addSampleGenerator(sample0);

    final var preset0 = builder.addPreset("preset0");
    final var preset_zone0 = preset0.addZone();
    preset_zone0.addModulator(
      14,
      NTGenerators.findForName("coarseTune").get(),
      (short) 20,
      0,
      NTTransforms.find(0));

    final var preset_zone1 =
      preset0.addZone()
        .addKeyRangeGenerator(0, 127)
        .addInstrumentGenerator(instrument0);

    final var description = builder.build();

    try (var channel = FileChannel.open(path, CREATE, WRITE, TRUNCATE_EXISTING)) {
      final var writer = this.writers.createForChannel(path.toUri(), description, channel);
      writer.write();
    }

    final var parsed = this.parse(path);
    compareFont(expected, parsed);
  }

  private NTFontType parse(final Path path)
    throws IOException, NTParseException
  {
    try (var channel = FileChannel.open(path, READ)) {
      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = this.parsers.createForByteBuffer(path.toUri(), map);
      final var file = parser.parse();
      return this.interpreters.createInterpreter(file).interpret();
    }
  }

  private NTFontType parseResource(
    final String name,
    final URI fake_uri)
    throws IOException, NTParseException
  {
    final var url = resourcePath(name);

    try (var input = url.openStream()) {
      final var path = Files.createTempFile("ntparsers-", ".sf2");
      try (var output = Files.newOutputStream(path, WRITE, TRUNCATE_EXISTING, CREATE)) {
        input.transferTo(output);
        output.flush();
      }

      final var channel = FileChannel.open(path, READ);
      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = this.parsers.createForByteBuffer(fake_uri, map);
      final var interpreter = this.interpreters.createInterpreter(parser.parse());
      return interpreter.interpret();
    }
  }
}
