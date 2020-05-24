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

package com.io7m.jnoisetype.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleType;
import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.parser.api.NTInterpreterProviderType;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static javax.sound.sampled.AudioFileFormat.Type;

@Parameters(commandDescription = "Extract samples from a SoundFontⓡ file")
final class CommandExtractSamples extends CommandRoot
{
  private static final Logger LOG = LoggerFactory.getLogger(CommandExtractSamples.class);

  // CHECKSTYLE:OFF

  @Parameter(
    names = "--file",
    required = true,
    description = "The SoundFontⓡ file to extract")
  Path path;

  @Parameter(
    names = "--output-directory",
    required = true,
    description = "The directory that will contain extracted samples")
  Path output_directory;

  @Parameter(
    names = "--pattern-include",
    required = false,
    description = "A regular expression matched against sample names. " +
      "Matching names will be extracted if they are not subsequently excluded.")
  String regex_include = ".*";

  @Parameter(
    names = "--pattern-exclude",
    required = false,
    description = "A regular expression matched against sample names. " +
      "Matching names will NOT be extracted. Exclusions are checked after inclusions.")
  String regex_exclude = "";

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

    final var pattern_include = Pattern.compile(this.regex_include);
    final var pattern_exclude = Pattern.compile(this.regex_exclude);

    Files.createDirectories(this.output_directory);

    try (var channel = FileChannel.open(this.path, READ)) {
      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = parsers.createForByteBuffer(this.path.toUri(), map);
      final var file_raw = parser.parse();
      final var file = interpreters.createInterpreter(file_raw).interpret();

      for (final var sample : file.samples()) {
        final var name = sample.nameText();
        if (pattern_include.matcher(name).matches()) {
          if (!pattern_exclude.matcher(name).matches()) {
            this.extractSample(sample, map);
          }
        }
      }
    }

    return null;
  }

  private void extractSample(
    final NTSampleType sample,
    final MappedByteBuffer map)
    throws IOException
  {
    final var output_path =
      this.output_directory.resolve(sample.nameText() + ".wav")
        .toAbsolutePath();

    LOG.info("extracting {} -> {}", sample.nameText(), output_path);

    final var range = sample.dataByteRange();

    final var view = map.slice();
    view.limit(Math.toIntExact(range.upper()));
    view.position(Math.toIntExact(range.lower()));
    view.order(map.order());

    try (var stream = new ByteBufferBackedInputStream(view)) {
      final var sample_rate = (float) sample.description().sampleRate();
      final var sample_size_bits = 16;
      final var sample_channels = sampleChannels(sample.description().kind());
      final var sample_frame_size = 2;
      final var sample_frame_rate = sample.description().sampleRate();
      final var sample_big_endian = map.order().equals(ByteOrder.BIG_ENDIAN);
      final var sample_count = sample.description().end() - sample.description().start();

      final var format =
        new AudioFormat(
          AudioFormat.Encoding.PCM_SIGNED,
          sample_rate,
          sample_size_bits,
          sample_channels,
          sample_frame_size,
          sample_frame_rate,
          sample_big_endian);

      try (var input = new AudioInputStream(stream, format, sample_count)) {
        try (var output = Files.newOutputStream(output_path, CREATE, TRUNCATE_EXISTING, WRITE)) {
          AudioSystem.write(input, Type.WAVE, output);
        }
      }
    }
  }

  private static int sampleChannels(final NTSampleKind kind)
  {
    switch (kind) {
      case SAMPLE_KIND_NONE:
        throw new UnreachableCodeException();
      case SAMPLE_KIND_LEFT:
      case SAMPLE_KIND_RIGHT:
      case SAMPLE_KIND_MONO:
        return 1;
      case SAMPLE_KIND_ROM_LINKED:
      case SAMPLE_KIND_ROM_LEFT:
      case SAMPLE_KIND_ROM_RIGHT:
      case SAMPLE_KIND_ROM_MONO:
      case SAMPLE_KIND_LINKED:
        throw new UnimplementedCodeException();
    }

    throw new UnreachableCodeException();
  }

  CommandExtractSamples()
  {

  }
}
