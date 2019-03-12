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

package com.io7m.jnoisetype.vanilla;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnoisetype.api.NTGeneratorIndex;
import com.io7m.jnoisetype.api.NTInstrumentIndex;
import com.io7m.jnoisetype.api.NTLongString;
import com.io7m.jnoisetype.api.NTModulatorIndex;
import com.io7m.jnoisetype.api.NTPresetIndex;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneGeneratorDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneModulatorDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneGeneratorDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneModulatorDescription;
import com.io7m.jnoisetype.writer.api.NTSampleWriterDescription;
import com.io7m.jnoisetype.writer.api.NTWriteException;
import com.io7m.jnoisetype.writer.api.NTWriterDescriptionType;
import com.io7m.jnoisetype.writer.api.NTWriterProviderType;
import com.io7m.jnoisetype.writer.api.NTWriterType;
import com.io7m.jspiel.api.RiffBuilderException;
import com.io7m.jspiel.api.RiffChunkBuilderType;
import com.io7m.jspiel.api.RiffChunkID;
import com.io7m.jspiel.api.RiffFileBuilderProviderType;
import com.io7m.jspiel.api.RiffFileWriterProviderType;
import com.io7m.jspiel.api.RiffWriteException;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import java.util.SortedMap;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * A writer provider.
 */

public final class NTWriters implements NTWriterProviderType
{
  private final RiffFileWriterProviderType riff_writers;
  private final RiffFileBuilderProviderType riff_builders;

  private NTWriters(
    final RiffFileWriterProviderType in_riff_writers,
    final RiffFileBuilderProviderType in_riff_builders)
  {
    this.riff_writers =
      Objects.requireNonNull(in_riff_writers, "riff_writers");
    this.riff_builders =
      Objects.requireNonNull(in_riff_builders, "riff_builders");
  }

  /**
   * Create a new writer provider.
   *
   * @param in_riff_writers  A RIFF writer provider
   * @param in_riff_builders A RIFF file builder
   *
   * @return A new provider
   */

  public static NTWriterProviderType create(
    final RiffFileWriterProviderType in_riff_writers,
    final RiffFileBuilderProviderType in_riff_builders)
  {
    return new NTWriters(in_riff_writers, in_riff_builders);
  }

  @Override
  public NTWriterType createForChannel(
    final URI source,
    final NTWriterDescriptionType description,
    final SeekableByteChannel channel)
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(channel, "channel");

    return new Writer(this.riff_writers, this.riff_builders, source, description, channel);
  }

  private static final class Writer implements NTWriterType
  {
    private final RiffFileWriterProviderType riff_writers;
    private final RiffFileBuilderProviderType riff_builders;
    private final URI source;
    private final NTWriterDescriptionType description;
    private final SeekableByteChannel channel;

    private Writer(
      final RiffFileWriterProviderType in_riff_writers,
      final RiffFileBuilderProviderType in_riff_builders,
      final URI in_source,
      final NTWriterDescriptionType in_description,
      final SeekableByteChannel in_channel)
    {
      this.riff_writers =
        Objects.requireNonNull(in_riff_writers, "riff_writers");
      this.riff_builders =
        Objects.requireNonNull(in_riff_builders, "riff_builders");
      this.source =
        Objects.requireNonNull(in_source, "source");
      this.description =
        Objects.requireNonNull(in_description, "description");
      this.channel =
        Objects.requireNonNull(in_channel, "channel");
    }

    private static void writeSDTA(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      chunk.setForm("sdta");

      /*
       * 6.1 Sample Data Format in the smpl Sub-chunk
       *
       * The smpl sub-chunk, if present, contains one or more “samples” of digital audio information
       * in the form of linearly coded sixteen bit, signed, little endian (least significant byte
       * first) words. Each sample is followed by a minimum of forty-six zero valued sample data
       * points. These zero valued data points are necessary to guarantee that any reasonable
       * upward pitch shift using any reasonable interpolator can loop on zero data at the end of
       * the sound.
       */

      final var padding = ByteBuffer.allocate(46 * 2);

      try (var smpl = chunk.addSubChunk(RiffChunkID.of("smpl"))) {
        final var samples = description.samples();

        smpl.setDataWriter(w_channel -> {
          for (final var sample_index : samples.keySet()) {
            final var sample = samples.get(sample_index);
            sample.description().dataWriter().write(w_channel);

            padding.position(0);
            w_channel.write(padding);
          }
        });
      }
    }

    private static void writeInfo(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      chunk.setForm("INFO");

      final var info = description.info();

      try (var sc = chunk.addSubChunk(RiffChunkID.of("ifil"))) {
        sc.setDataWriter(w_channel -> writeVersion(w_channel, info.version()));
      }

      try (var sc = chunk.addSubChunk(RiffChunkID.of("isng"))) {
        sc.setDataWriter(w_channel -> writeShortString(w_channel, info.soundEngine()));
      }

      try (var sc = chunk.addSubChunk(RiffChunkID.of("INAM"))) {
        sc.setDataWriter(w_channel -> writeShortString(w_channel, info.name()));
      }

      info.rom().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("irom"))) {
          sc.setDataWriter(w_channel -> writeShortString(w_channel, text));
        }
      });

      info.romRevision().ifPresent(version -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("iver"))) {
          sc.setDataWriter(w_channel -> writeVersion(w_channel, version));
        }
      });

      info.creationDate().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("ICRD"))) {
          sc.setDataWriter(w_channel -> writeShortString(w_channel, text));
        }
      });

      info.engineers().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("IENG"))) {
          sc.setDataWriter(w_channel -> writeShortString(w_channel, text));
        }
      });

      info.product().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("IPRD"))) {
          sc.setDataWriter(w_channel -> writeShortString(w_channel, text));
        }
      });

      info.copyright().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("ICOP"))) {
          sc.setDataWriter(w_channel -> writeShortString(w_channel, text));
        }
      });

      info.comment().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("ICMT"))) {
          sc.setDataWriter(w_channel -> writeLongString(w_channel, text));
        }
      });

      info.software().ifPresent(text -> {
        try (var sc = chunk.addSubChunk(RiffChunkID.of("ISFT"))) {
          sc.setDataWriter(w_channel -> writeShortString(w_channel, text));
        }
      });
    }

    private static void writePDTA(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      chunk.setForm("pdta");

      writePHDR(description, chunk);
      writePBAG(description, chunk);
      writePMOD(description, chunk);
      writePGEN(description, chunk);
      writeINST(description, chunk);
      writeIBAG(description, chunk);
      writeIMOD(description, chunk);
      writeIGEN(description, chunk);
      writeSHDR(description, chunk);
    }

    private static void writeIGEN(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);
      final var instruments = description.instruments();
      final var generators = countRequiredInstrumentGeneratorRecords(instruments);

      try (var igen = chunk.addSubChunk(RiffChunkID.of("igen"))) {
        igen.setSize(Math.multiplyExact(generators, 4L));
        igen.setDataWriter(w_channel -> {

          for (final var instrument_index : instruments.keySet()) {
            final var instrument = instruments.get(instrument_index);

            for (final var zone : instrument.zones()) {
              for (final var generator : zone.generators()) {
                packIGENRecord(buffer, generator);
                w_channel.write(buffer);
              }
            }
          }

          packIGENTerminalRecord(buffer);
          w_channel.write(buffer);
        });
      }
    }

    private static void packIGENTerminalRecord(
      final ByteBuffer buffer)
    {
      buffer.position(0);
      buffer.putChar((char) 0);
      buffer.putChar((char) 0);

      checkAndFlipBuffer(buffer);
    }

    private static void packIGENRecord(
      final ByteBuffer buffer,
      final NTInstrumentWriterZoneGeneratorDescription generator)
    {
      buffer.position(0);
      buffer.putChar(generator.generator().index().asUnsigned16());
      buffer.putChar(generator.amount().asUnsigned16());

      checkAndFlipBuffer(buffer);
    }

    private static void packPMODTerminalRecord(
      final ByteBuffer buffer)
    {
      buffer.position(0);
      buffer.putChar((char) 0);
      buffer.putChar((char) 0);
      buffer.putShort((short) 0);
      buffer.putChar((char) 0);
      buffer.putChar((char) 0);

      checkAndFlipBuffer(buffer);
    }

    private static void packPMODRecord(
      final ByteBuffer buffer,
      final NTPresetWriterZoneModulatorDescription modulator)
    {
      buffer.position(0);
      buffer.putChar((char) (modulator.sourceOperator() & 0xffff));
      buffer.putChar(modulator.targetOperator().index().asUnsigned16());
      buffer.putShort(modulator.modulationAmount());
      buffer.putChar((char) (modulator.modulationAmountSourceOperator() & 0xffff));
      buffer.putChar(modulator.modulationTransformOperator().index().asUnsigned16());

      checkAndFlipBuffer(buffer);
    }

    private static void packIMODTerminalRecord(
      final ByteBuffer buffer)
    {
      buffer.position(0);
      buffer.putChar((char) 0);
      buffer.putChar((char) 0);
      buffer.putShort((short) 0);
      buffer.putChar((char) 0);
      buffer.putChar((char) 0);

      checkAndFlipBuffer(buffer);
    }

    private static void packIMODRecord(
      final ByteBuffer buffer,
      final NTInstrumentWriterZoneModulatorDescription modulator)
    {
      buffer.position(0);
      buffer.putChar((char) (modulator.sourceOperator() & 0xffff));
      buffer.putChar(modulator.targetOperator().index().asUnsigned16());
      buffer.putShort(modulator.modulationAmount());
      buffer.putChar((char) (modulator.modulationAmountSourceOperator() & 0xffff));
      buffer.putChar(modulator.modulationTransformOperator().index().asUnsigned16());

      checkAndFlipBuffer(buffer);
    }

    private static void packPGENTerminalRecord(
      final ByteBuffer buffer)
    {
      buffer.position(0);
      buffer.putChar((char) 0);
      buffer.putChar((char) 0);

      checkAndFlipBuffer(buffer);
    }

    private static void packPGENRecord(
      final ByteBuffer buffer,
      final NTPresetWriterZoneGeneratorDescription generator)
    {
      buffer.position(0);
      buffer.putChar(generator.generator().index().asUnsigned16());
      buffer.putChar(generator.amount().asUnsigned16());

      checkAndFlipBuffer(buffer);
    }

    private static long countRequiredInstrumentGeneratorRecords(
      final SortedMap<NTInstrumentIndex, NTInstrumentWriterDescription> instruments)
    {
      var generators = 1L;
      for (final var instrument : instruments.values()) {
        for (final var zone : instrument.zones()) {
          for (final var ignored : zone.generators()) {
            ++generators;
          }
        }
      }
      return generators;
    }

    private static long countRequiredPresetGeneratorRecords(
      final SortedMap<NTPresetIndex, NTPresetWriterDescription> presets)
    {
      var generators = 1L;
      for (final var preset : presets.values()) {
        for (final var zone : preset.zones()) {
          for (final var ignored : zone.generators()) {
            ++generators;
          }
        }
      }
      return generators;
    }

    private static long countRequiredPresetModulatorRecords(
      final SortedMap<NTPresetIndex, NTPresetWriterDescription> presets)
    {
      var modulators = 1L;
      for (final var preset : presets.values()) {
        for (final var zone : preset.zones()) {
          for (final var ignored : zone.modulators()) {
            ++modulators;
          }
        }
      }
      return modulators;
    }

    private static long countRequiredInstrumentModulatorRecords(
      final SortedMap<NTInstrumentIndex, NTInstrumentWriterDescription> instruments)
    {
      var modulators = 1L;
      for (final var instrument : instruments.values()) {
        for (final var zone : instrument.zones()) {
          for (final var ignored : zone.modulators()) {
            ++modulators;
          }
        }
      }
      return modulators;
    }

    private static void writeIMOD(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var buffer = ByteBuffer.allocate(10).order(LITTLE_ENDIAN);
      final var instruments = description.instruments();
      final var modulators = countRequiredInstrumentModulatorRecords(instruments);

      try (var imod = chunk.addSubChunk(RiffChunkID.of("imod"))) {
        imod.setSize(Math.multiplyExact(modulators, 10L));
        imod.setDataWriter(w_channel -> {

          for (final var instrument_index : instruments.keySet()) {
            final var instrument = instruments.get(instrument_index);

            for (final var zone : instrument.zones()) {
              for (final var modulator : zone.modulators()) {
                packIMODRecord(buffer, modulator);
                w_channel.write(buffer);
              }
            }
          }

          packIMODTerminalRecord(buffer);
          w_channel.write(buffer);
        });
      }
    }

    private static void writeIBAG(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var instruments = description.instruments();
      final var zones = countRequiredInstrumentZoneRecords(instruments);

      try (var ibag = chunk.addSubChunk(RiffChunkID.of("ibag"))) {
        ibag.setSize(Math.multiplyExact(zones, 4L));
        ibag.setDataWriter(w_channel -> {
          final var buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);

          var gen_index = NTGeneratorIndex.of(0);
          var mod_index = NTModulatorIndex.of(0);

          for (final var instrument_index : instruments.keySet()) {
            final var instrument = instruments.get(instrument_index);

            for (final var zone : instrument.zones()) {
              if (!zone.generators().isEmpty()) {
                gen_index = zone.generators().get(0).index();
              }
              if (!zone.modulators().isEmpty()) {
                mod_index = zone.modulators().get(0).index();
              }

              packIBAGRecord(buffer, gen_index, mod_index);
              w_channel.write(buffer);

              gen_index = NTGeneratorIndex.of(gen_index.value() + zone.generators().size());
              mod_index = NTModulatorIndex.of(mod_index.value() + zone.modulators().size());
            }
          }

          packIBAGRecord(buffer, gen_index, mod_index);
          w_channel.write(buffer);
        });
      }
    }

    private static void packIBAGRecord(
      final ByteBuffer buffer,
      final NTGeneratorIndex gen_index,
      final NTModulatorIndex mod_index)
    {
      buffer.position(0);
      buffer.putChar(gen_index.asUnsigned16());
      buffer.putChar(mod_index.asUnsigned16());

      checkAndFlipBuffer(buffer);
    }

    private static long countRequiredPresetZoneRecords(
      final SortedMap<NTPresetIndex, NTPresetWriterDescription> presets)
    {
      var zones = 1L;
      for (final var preset : presets.values()) {
        zones = zones + (long) preset.zones().size();
      }
      return zones;
    }

    private static long countRequiredInstrumentZoneRecords(
      final SortedMap<NTInstrumentIndex, NTInstrumentWriterDescription> instruments)
    {
      var zones = 1L;
      for (final var instrument : instruments.values()) {
        zones = zones + (long) instrument.zones().size();
      }
      return zones;
    }

    private static void writeINST(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var instruments = description.instruments();

      try (var inst = chunk.addSubChunk(RiffChunkID.of("inst"))) {
        inst.setSize(((long) instruments.size() + 1L) * 22L);
        inst.setDataWriter(w_channel -> {

          final var buffer =
            ByteBuffer.allocate(22).order(LITTLE_ENDIAN);
          final var buffer_name =
            ByteBuffer.allocate(20).order(LITTLE_ENDIAN);

          var bag_end_index = 0;
          for (final var instrument_index : instruments.keySet()) {
            final var instrument = instruments.get(instrument_index);
            packINSTRecord(buffer, buffer_name, instrument);
            w_channel.write(buffer);
            bag_end_index = instrument.instrumentNextBagIndex();
          }

          packINSTTerminalRecord(buffer, buffer_name, (short) bag_end_index);
          w_channel.write(buffer);
        });
      }
    }

    private static void packINSTTerminalRecord(
      final ByteBuffer buffer,
      final ByteBuffer buffer_name,
      final short bag_end_index)
    {
      buffer.position(0);
      packName(buffer_name, "EOI");
      buffer.put(buffer_name);
      buffer.putShort(bag_end_index);

      checkAndFlipBuffer(buffer);
    }

    private static void packINSTRecord(
      final ByteBuffer buffer,
      final ByteBuffer buffer_name,
      final NTInstrumentWriterDescription instrument)
    {
      packName(buffer_name, instrument.name().value());
      buffer.position(0);
      buffer.put(buffer_name);
      buffer.putShort((short) instrument.instrumentBagIndex());

      checkAndFlipBuffer(buffer);
    }

    private static void writePGEN(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);
      final var presets = description.presets();
      final var generators = countRequiredPresetGeneratorRecords(presets);

      try (var pgen = chunk.addSubChunk(RiffChunkID.of("pgen"))) {
        pgen.setSize(Math.multiplyExact(generators, 4L));
        pgen.setDataWriter(w_channel -> {

          for (final var preset_index : presets.keySet()) {
            final var preset = presets.get(preset_index);

            for (final var zone : preset.zones()) {
              for (final var generator : zone.generators()) {
                packPGENRecord(buffer, generator);
                w_channel.write(buffer);
              }
            }
          }

          packPGENTerminalRecord(buffer);
          w_channel.write(buffer);
        });
      }
    }

    private static void writePMOD(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var buffer = ByteBuffer.allocate(10).order(LITTLE_ENDIAN);
      final var presets = description.presets();
      final var generators = countRequiredPresetModulatorRecords(presets);

      try (var pmod = chunk.addSubChunk(RiffChunkID.of("pmod"))) {
        pmod.setSize(Math.multiplyExact(generators, 10));
        pmod.setDataWriter(w_channel -> {

          for (final var preset_index : presets.keySet()) {
            final var preset = presets.get(preset_index);

            for (final var zone : preset.zones()) {
              for (final var modulator : zone.modulators()) {
                packPMODRecord(buffer, modulator);
                w_channel.write(buffer);
              }
            }
          }

          packPMODTerminalRecord(buffer);
          w_channel.write(buffer);
        });
      }
    }

    private static void writePBAG(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var presets = description.presets();
      final var zones = countRequiredPresetZoneRecords(presets);

      try (var pbag = chunk.addSubChunk(RiffChunkID.of("pbag"))) {
        pbag.setSize(Math.multiplyExact(zones, 4L));
        pbag.setDataWriter(w_channel -> {
          final var buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);

          var gen_index = NTGeneratorIndex.of(0);
          var mod_index = NTModulatorIndex.of(0);

          for (final var preset_index : presets.keySet()) {
            final var preset = presets.get(preset_index);

            for (final var zone : preset.zones()) {
              if (!zone.generators().isEmpty()) {
                gen_index = zone.generators().get(0).index();
              }
              if (!zone.modulators().isEmpty()) {
                mod_index = zone.modulators().get(0).index();
              }

              packIBAGRecord(buffer, gen_index, mod_index);
              w_channel.write(buffer);

              gen_index = NTGeneratorIndex.of(gen_index.value() + zone.generators().size());
              mod_index = NTModulatorIndex.of(mod_index.value() + zone.modulators().size());
            }
          }

          packIBAGRecord(buffer, gen_index, mod_index);
          w_channel.write(buffer);
        });
      }
    }

    private static void writeSHDR(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var samples = description.samples();

      try (var shdr = chunk.addSubChunk(RiffChunkID.of("shdr"))) {
        final var records_size = ((long) samples.size() + 1L) * 46L;
        shdr.setSize(records_size);
        shdr.setDataWriter(w_channel -> {

          final var buffer =
            ByteBuffer.allocate(46).order(LITTLE_ENDIAN);
          final var buffer_name =
            ByteBuffer.allocate(20).order(LITTLE_ENDIAN);

          for (final var name : samples.keySet()) {
            packSHDRRecord(buffer, buffer_name, samples.get(name));
            w_channel.write(buffer);
          }

          packSHDRTerminalRecord(buffer, buffer_name);
          w_channel.write(buffer);
        });
      }
    }

    private static void packSHDRRecord(
      final ByteBuffer buffer,
      final ByteBuffer buffer_name,
      final NTSampleWriterDescription writer_description)
    {
      final var description = writer_description.description();

      packName(buffer_name, description.name().value());

      buffer.position(0);
      buffer.put(buffer_name);
      buffer.putInt(Math.toIntExact(writer_description.sampleAbsoluteStart()));
      buffer.putInt(Math.toIntExact(writer_description.sampleAbsoluteEnd()));
      buffer.putInt(Math.toIntExact(writer_description.sampleAbsoluteLoopStart()));
      buffer.putInt(Math.toIntExact(writer_description.sampleAbsoluteLoopEnd()));
      buffer.putInt(description.sampleRate());
      buffer.put((byte) description.originalPitch().value());
      buffer.put((byte) description.pitchCorrection());
      buffer.putShort((short) 0);
      buffer.putShort((short) description.kind().value());

      checkAndFlipBuffer(buffer);
    }

    private static void packSHDRTerminalRecord(
      final ByteBuffer buffer,
      final ByteBuffer buffer_name)
    {
      packName(buffer_name, "EOS");

      buffer.position(0);
      buffer.put(buffer_name);
      buffer.putInt(0);
      buffer.putInt(0);
      buffer.putInt(0);
      buffer.putInt(0);
      buffer.putInt(0);
      buffer.put((byte) 0);
      buffer.put((byte) 0);
      buffer.putShort((short) 0);
      buffer.putShort((short) 0);

      checkAndFlipBuffer(buffer);
    }

    private static void packName(
      final ByteBuffer buffer_name,
      final String name)
    {
      buffer_name.position(0);
      buffer_name.put(name.getBytes(US_ASCII));
      while (buffer_name.remaining() > 0) {
        buffer_name.put((byte) 0x0);
      }
      buffer_name.flip();
    }

    private static void writePHDR(
      final NTWriterDescriptionType description,
      final RiffChunkBuilderType chunk)
    {
      final var presets = description.presets();

      try (var phdr = chunk.addSubChunk(RiffChunkID.of("phdr"))) {
        phdr.setSize(((long) presets.size() + 1L) * 38L);
        phdr.setDataWriter(w_channel -> {

          final var buffer =
            ByteBuffer.allocate(38).order(LITTLE_ENDIAN);
          final var buffer_name =
            ByteBuffer.allocate(20).order(LITTLE_ENDIAN);

          var bag_end_index = 0;
          for (final var preset_index : presets.keySet()) {
            final var preset = presets.get(preset_index);
            packPHDRRecord(buffer, buffer_name, preset);
            w_channel.write(buffer);
            bag_end_index = preset.presetNextBagIndex();
          }

          packPHDRTerminalRecord(buffer, buffer_name, (short) bag_end_index);
          w_channel.write(buffer);
        });
      }
    }

    private static void packPHDRRecord(
      final ByteBuffer buffer,
      final ByteBuffer buffer_name,
      final NTPresetWriterDescription preset)
    {
      packName(buffer_name, preset.name().value());

      buffer.position(0);
      buffer.put(buffer_name);
      buffer.putChar(preset.presetIndex().asUnsigned16());
      buffer.putShort((short) preset.bank());
      buffer.putShort((short) preset.presetBagIndex());
      buffer.putInt(0);
      buffer.putInt(0);
      buffer.putInt(0);

      checkAndFlipBuffer(buffer);
    }

    private static void packPHDRTerminalRecord(
      final ByteBuffer buffer,
      final ByteBuffer buffer_name,
      final short bag_end_index)
    {
      packName(buffer_name, "EOP");

      buffer.position(0);
      buffer.put(buffer_name);
      buffer.putShort((short) 0);
      buffer.putShort((short) 0);
      buffer.putShort(bag_end_index);
      buffer.putInt(0);
      buffer.putInt(0);
      buffer.putInt(0);

      checkAndFlipBuffer(buffer);
    }

    private static void checkAndFlipBuffer(
      final ByteBuffer buffer)
    {
      Invariants.checkInvariantI(
        buffer.remaining(),
        buffer.remaining() == 0,
        x -> "Must have consumed entire buffer");

      buffer.flip();

      Invariants.checkInvariantI(
        buffer.remaining(),
        buffer.remaining() == buffer.capacity(),
        x -> "Must have rewound buffer");
    }

    private static void writeShortString(
      final SeekableByteChannel writer,
      final NTShortString text)
      throws IOException
    {
      final var bytes = text.value().getBytes(US_ASCII);

      Preconditions.checkPreconditionI(
        bytes.length + 2,
        bytes.length + 2 < 256,
        x -> "Short string length plus terminators must be < 256");

      final var buffer = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
      writer.write(buffer);
      addStringTerminators(writer, bytes);
    }

    private static void addStringTerminators(
      final SeekableByteChannel writer,
      final byte[] bytes)
      throws IOException
    {
      final var terminator = ByteBuffer.allocate(1);
      terminator.position(0);
      writer.write(terminator);
      if ((bytes.length + 1) % 2 != 0) {
        terminator.position(0);
        writer.write(terminator);
      }
    }

    private static void writeLongString(
      final SeekableByteChannel writer,
      final NTLongString text)
      throws IOException
    {
      final var bytes = text.value().getBytes(US_ASCII);

      Preconditions.checkPreconditionI(
        bytes.length + 2,
        bytes.length + 2 < 65536,
        x -> "Long string length plus terminators must be < 65536");

      final var buffer = ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN);
      writer.write(buffer);
      addStringTerminators(writer, bytes);
    }

    private static void writeVersion(
      final SeekableByteChannel writer,
      final NTVersion version)
      throws IOException
    {
      final var buffer = ByteBuffer.allocate(4).order(LITTLE_ENDIAN);
      buffer.putShort((short) version.major());
      buffer.putShort((short) version.minor());
      buffer.flip();
      writer.write(buffer);
    }

    @Override
    public void write()
      throws NTWriteException
    {
      try {
        final var riff_builder =
          this.riff_builders.create(LITTLE_ENDIAN);

        try (var root = riff_builder.setRootChunk(RiffChunkID.of("RIFF"), "sfbk")) {
          try (var info = root.addSubChunk(RiffChunkID.of("LIST"))) {
            writeInfo(this.description, info);
          }
          try (var sdta = root.addSubChunk(RiffChunkID.of("LIST"))) {
            writeSDTA(this.description, sdta);
          }
          try (var pdta = root.addSubChunk(RiffChunkID.of("LIST"))) {
            writePDTA(this.description, pdta);
          }
        }

        final var riff_description =
          riff_builder.build();
        final var writer =
          this.riff_writers.createForChannel(this.source, riff_description, this.channel);

        writer.write();
      } catch (final RiffBuilderException e) {
        throw new NTWriteException(e, this.source, 0L);
      } catch (final RiffWriteException e) {
        throw new NTWriteException(e, e.source(), e.offset());
      }
    }
  }
}
