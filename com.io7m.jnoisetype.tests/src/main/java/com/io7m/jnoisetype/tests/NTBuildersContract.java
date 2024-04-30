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

import com.io7m.jnoisetype.api.NTSampleIndex;
import com.io7m.jnoisetype.writer.api.NTBuilderProviderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

public abstract class NTBuildersContract
{
  private NTBuilderProviderType builders;

  private static void copyWav(
    final String name,
    final SeekableByteChannel channel)
    throws IOException
  {
    final var path = "/com/io7m/jnoisetype/tests/" + name;
    final var url = NTBuildersContract.class.getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }

    try (var stream = AudioSystem.getAudioInputStream(url)) {
      stream.transferTo(Channels.newOutputStream(channel));
    } catch (final UnsupportedAudioFileException e) {
      throw new IOException(e);
    }
  }

  protected abstract NTBuilderProviderType builders();

  @BeforeEach
  public final void testSetup()
  {
    this.builders = this.builders();
  }

  @Test
  public final void testEmpty()
  {
    final var builder =
      this.builders.createBuilder();

    final var description =
      builder.build();

    Assertions.assertEquals(
      "", description.info().name().value(), "Correct name");
    Assertions.assertEquals(
      0, description.samples().size(), "Correct sample count");
  }

  @Test
  public final void testOneSample()
  {
    final var builder =
      this.builders.createBuilder();

    builder.addSample("000_60")
      .setDataWriter(channel -> copyWav("000_60.wav", channel));

    final var description =
      builder.build();

    Assertions.assertEquals(
      "", description.info().name().value(), "Correct name");
    Assertions.assertEquals(
      1, description.samples().size(), "Correct sample count");

    final var sample0 = description.samples().get(NTSampleIndex.of(0));
    Assertions.assertEquals(
      "000_60",
      sample0.description().name().value(), "Correct name");
  }
}
