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

import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTRanges;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.writer.api.NTBuilderProviderType;
import com.io7m.jnoisetype.writer.api.NTBuilderType;
import com.io7m.jnoisetype.writer.api.NTInstrumentBuilderType;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneGeneratorDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentWriterZoneModulatorDescription;
import com.io7m.jnoisetype.writer.api.NTInstrumentZoneBuilderType;
import com.io7m.jnoisetype.writer.api.NTPresetBuilderType;
import com.io7m.jnoisetype.writer.api.NTPresetWriterDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneGeneratorDescription;
import com.io7m.jnoisetype.writer.api.NTPresetWriterZoneModulatorDescription;
import com.io7m.jnoisetype.writer.api.NTPresetZoneBuilderType;
import com.io7m.jnoisetype.writer.api.NTSampleBuilderDescription;
import com.io7m.jnoisetype.writer.api.NTSampleBuilderType;
import com.io7m.jnoisetype.writer.api.NTSampleDataWriterType;
import com.io7m.jnoisetype.writer.api.NTSampleWriterDescription;
import com.io7m.jnoisetype.writer.api.NTWriterDescriptionType;
import com.io7m.jranges.RangeCheck;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.io7m.jnoisetype.api.NTSampleKind.SAMPLE_KIND_MONO;

/**
 * A builder provider.
 */

public final class NTBuilders implements NTBuilderProviderType
{
  /**
   * Construct a provider.
   */

  public NTBuilders()
  {

  }

  @Override
  public NTBuilderType createBuilder()
  {
    return new Builder();
  }

  private static final class WriterDescription implements NTWriterDescriptionType
  {
    private final NTInfo info;
    private final SortedMap<Integer, NTSampleWriterDescription> description_samples;
    private final SortedMap<Integer, NTInstrumentWriterDescription> description_instruments;
    private final SortedMap<Integer, NTPresetWriterDescription> description_presets;

    WriterDescription(
      final NTInfo in_info,
      final TreeMap<Integer, NTSampleWriterDescription> in_description_samples,
      final TreeMap<Integer, NTInstrumentWriterDescription> in_description_instruments,
      final TreeMap<Integer, NTPresetWriterDescription> in_preset_descriptions)
    {
      this.info =
        Objects.requireNonNull(in_info, "info");

      this.description_samples =
        Collections.unmodifiableSortedMap(
          Objects.requireNonNull(in_description_samples, "description_samples"));
      this.description_instruments =
        Collections.unmodifiableSortedMap(
          Objects.requireNonNull(in_description_instruments, "description_instruments"));
      this.description_presets =
        Collections.unmodifiableSortedMap(
          Objects.requireNonNull(in_preset_descriptions, "description_presets"));
    }

    @Override
    public NTInfo info()
    {
      return this.info;
    }

    @Override
    public SortedMap<Integer, NTSampleWriterDescription> samples()
    {
      return this.description_samples;
    }

    @Override
    public SortedMap<Integer, NTInstrumentWriterDescription> instruments()
    {
      return this.description_instruments;
    }

    @Override
    public SortedMap<Integer, NTPresetWriterDescription> presets()
    {
      return this.description_presets;
    }
  }

  private static final class Builder implements NTBuilderType
  {
    private final AtomicInteger instrument_generator_indices;
    private final AtomicInteger instrument_modulator_indices;
    private final TreeMap<NTSampleName, SampleBuilder> samples;
    private final TreeMap<NTInstrumentName, InstrumentBuilder> instruments;
    private final TreeMap<NTPresetName, PresetBuilder> presets;
    private final AtomicInteger preset_generator_indices;
    private final AtomicInteger preset_modulator_indices;
    private NTInfo info;

    private Builder()
    {
      this.samples = new TreeMap<>();
      this.instruments = new TreeMap<>();
      this.instrument_generator_indices = new AtomicInteger(0);
      this.instrument_modulator_indices = new AtomicInteger(0);
      this.presets = new TreeMap<>();
      this.preset_generator_indices = new AtomicInteger(0);
      this.preset_modulator_indices = new AtomicInteger(0);

      this.info =
        NTInfo.builder()
          .setVersion(NTVersion.of(2, 1))
          .setSoundEngine(NTShortString.of("EMU8000"))
          .setSoftware(NTShortString.of(version()))
          .setName(NTShortString.of(""))
          .build();
    }

    private static String version()
    {
      final var pack = Builder.class.getPackage();
      if (pack != null) {
        final var version = pack.getImplementationVersion();
        if (version != null) {
          return version;
        }
      }
      return "com.io7m.jnoisetype 0.0.0";
    }

    private static TreeMap<Integer, NTInstrumentWriterDescription> buildInstrumentDescriptions(
      final TreeMap<NTInstrumentName, InstrumentBuilder> instruments)
    {
      var bag_index = 0;
      final var instrument_descriptions = new TreeMap<Integer, NTInstrumentWriterDescription>();
      for (final var name : instruments.keySet()) {
        final var instrument = instruments.get(name);

        final var instrument_builder =
          NTInstrumentWriterDescription.builder()
            .setInstrumentIndex(instrument.index)
            .setName(instrument.name);

        var generators = 0;
        var modulators = 0;
        for (final var zone : instrument.zones) {
          final var zone_builder = NTInstrumentWriterZoneDescription.builder();

          for (final var generator : zone.generators.values()) {
            zone_builder.addGenerators(
              NTInstrumentWriterZoneGeneratorDescription.builder()
                .setIndex(generator.index)
                .setAmount(generator.amount)
                .setGenerator(generator.generator)
                .build());
          }

          for (final var modulator : zone.modulators.values()) {
            zone_builder.addModulators(
              NTInstrumentWriterZoneModulatorDescription.builder()
                .setIndex(modulator.index)
                .build());
          }

          instrument_builder.addZones(zone_builder.build());
          generators = Math.addExact(generators, zone.generators.size());
          modulators = Math.addExact(modulators, zone.modulators.size());
        }

        final var description =
          instrument_builder
            .setInstrumentBagIndex(bag_index)
            .build();

        instrument_descriptions.put(Integer.valueOf(instrument.index), description);
        bag_index = Math.addExact(bag_index, instrument.zones.size());
      }
      return instrument_descriptions;
    }

    private static TreeMap<Integer, NTPresetWriterDescription> buildPresetDescriptions(
      final TreeMap<NTPresetName, PresetBuilder> presets)
    {
      var bag_index = 0;
      final var preset_descriptions = new TreeMap<Integer, NTPresetWriterDescription>();
      for (final var name : presets.keySet()) {
        final var preset = presets.get(name);

        final var preset_builder =
          NTPresetWriterDescription.builder()
            .setPresetIndex(preset.index)
            .setBank(preset.bank)
            .setName(preset.name);

        var generators = 0;
        var modulators = 0;
        for (final var zone : preset.zones) {
          final var zone_builder = NTPresetWriterZoneDescription.builder();

          for (final var generator : zone.generators.values()) {
            zone_builder.addGenerators(
              NTPresetWriterZoneGeneratorDescription.builder()
                .setIndex(generator.index)
                .setAmount(generator.amount)
                .setGenerator(generator.generator)
                .build());
          }

          for (final var modulator : zone.modulators.values()) {
            zone_builder.addModulators(
              NTPresetWriterZoneModulatorDescription.builder()
                .setIndex(modulator.index)
                .build());
          }

          preset_builder.addZones(zone_builder.build());
          generators = Math.addExact(generators, zone.generators.size());
          modulators = Math.addExact(modulators, zone.modulators.size());
        }

        final var description =
          preset_builder
            .setPresetBagIndex(bag_index)
            .build();

        preset_descriptions.put(Integer.valueOf(preset.index), description);
        bag_index = Math.addExact(bag_index, preset.zones.size());
      }
      return preset_descriptions;
    }

    private static TreeMap<Integer, NTSampleWriterDescription> buildSampleDescriptions(
      final TreeMap<NTSampleName, SampleBuilder> samples)
    {
      var offset_start = 0L;
      final var sample_descriptions = new TreeMap<Integer, NTSampleWriterDescription>();
      for (final var name : samples.keySet()) {
        final var sample = samples.get(name);

        final var samples_padded =
          Math.addExact(sample.sampleCount(), 46L);
        final var offset_end =
          Math.addExact(offset_start, sample.sampleCount());
        final var offset_loop_start =
          Math.addExact(offset_start, sample.loopStart());
        final var offset_loop_end =
          Math.addExact(offset_start, sample.loopEnd());

        final var description =
          NTSampleWriterDescription.builder()
            .setDescription(sample.description)
            .setSampleIndex(sample.index)
            .setSampleAbsoluteStart(offset_start)
            .setSampleAbsoluteEnd(offset_end)
            .setSampleAbsoluteLoopStart(offset_loop_start)
            .setSampleAbsoluteLoopEnd(offset_loop_end)
            .build();

        sample_descriptions.put(Integer.valueOf(sample.index), description);
        offset_start = Math.addExact(offset_start, samples_padded);
      }
      return sample_descriptions;
    }

    @Override
    public NTWriterDescriptionType build()
    {
      final var sample_descriptions =
        buildSampleDescriptions(this.samples);
      final var instrument_descriptions =
        buildInstrumentDescriptions(this.instruments);
      final var preset_descriptions =
        buildPresetDescriptions(this.presets);

      return new WriterDescription(
        this.info,
        sample_descriptions,
        instrument_descriptions,
        preset_descriptions);
    }

    @Override
    public NTBuilderType setInfo(
      final NTInfo in_info)
    {
      this.info = Objects.requireNonNull(in_info, "info");
      return this;
    }

    @Override
    public NTInfo info()
    {
      return this.info;
    }

    @Override
    public NTSampleBuilderType addSample(
      final NTSampleName name)
    {
      Objects.requireNonNull(name, "name");

      if (this.samples.containsKey(name)) {
        throw new IllegalArgumentException(
          new StringBuilder(64)
            .append("A sample with the given name already exists.")
            .append(System.lineSeparator())
            .append("  Name: ")
            .append(name.value())
            .toString());
      }

      final var description =
        NTSampleBuilderDescription.builder()
          .setDataWriter(channel -> {

          })
          .setKind(SAMPLE_KIND_MONO)
          .setSampleCount(0L)
          .setLoopEnd(0L)
          .setLoopStart(0L)
          .setName(name)
          .setOriginalPitch(60)
          .setPitchCorrection(0)
          .setSampleRate(48000)
          .build();

      final var builder = new SampleBuilder(this.samples.size(), description);
      this.samples.put(name, builder);
      return builder;
    }

    @Override
    public NTInstrumentBuilderType addInstrument(
      final NTInstrumentName name)
    {
      Objects.requireNonNull(name, "name");

      if (this.instruments.containsKey(name)) {
        throw new IllegalArgumentException(
          new StringBuilder(64)
            .append("An instrument with the given name already exists.")
            .append(System.lineSeparator())
            .append("  Name: ")
            .append(name.value())
            .toString());
      }

      final var builder =
        new InstrumentBuilder(
          this.instrument_generator_indices,
          this.instrument_modulator_indices,
          this.instruments.size(),
          name);

      this.instruments.put(name, builder);
      return builder;
    }

    @Override
    public NTPresetBuilderType addPreset(
      final NTPresetName name)
    {
      Objects.requireNonNull(name, "name");

      if (this.presets.containsKey(name)) {
        throw new IllegalArgumentException(
          new StringBuilder(64)
            .append("A preset with the given name already exists.")
            .append(System.lineSeparator())
            .append("  Name: ")
            .append(name.value())
            .toString());
      }

      final var builder =
        new PresetBuilder(
          this.preset_generator_indices,
          this.preset_modulator_indices,
          this.presets.size(),
          name);

      this.presets.put(name, builder);
      return builder;
    }
  }

  private static final class InstrumentBuilder implements NTInstrumentBuilderType
  {
    private final AtomicInteger instrument_generator_indices;
    private final AtomicInteger instrument_modulator_indices;
    private final NTInstrumentName name;
    private final LinkedList<InstrumentZoneBuilder> zones;
    private final int index;

    private InstrumentBuilder(
      final AtomicInteger in_instrument_generator_indices,
      final AtomicInteger in_instrument_modulator_indices,
      final int in_index,
      final NTInstrumentName in_name)
    {
      this.instrument_generator_indices =
        Objects.requireNonNull(in_instrument_generator_indices, "instrument_generator_indices");
      this.instrument_modulator_indices =
        Objects.requireNonNull(in_instrument_modulator_indices, "instrument_modulator_indices");
      this.index = in_index;
      this.name = Objects.requireNonNull(in_name, "name");
      this.zones = new LinkedList<>();
    }

    @Override
    public NTInstrumentName name()
    {
      return this.name;
    }

    @Override
    public NTInstrumentZoneBuilderType addZone()
    {
      final var zone = new InstrumentZoneBuilder(
        this.instrument_generator_indices,
        this.instrument_modulator_indices);
      this.zones.add(zone);
      return zone;
    }

    @Override
    public int instrumentIndex()
    {
      return this.index;
    }
  }

  private static final class PresetBuilder implements NTPresetBuilderType
  {
    private final AtomicInteger preset_generator_indices;
    private final AtomicInteger preset_modulator_indices;
    private final NTPresetName name;
    private final LinkedList<PresetZoneBuilder> zones;
    private final int index;
    private int bank;

    private PresetBuilder(
      final AtomicInteger in_preset_generator_indices,
      final AtomicInteger in_preset_modulator_indices,
      final int in_index,
      final NTPresetName in_name)
    {
      this.preset_generator_indices =
        Objects.requireNonNull(in_preset_generator_indices, "preset_generator_indices");
      this.preset_modulator_indices =
        Objects.requireNonNull(in_preset_modulator_indices, "preset_modulator_indices");
      this.index = in_index;
      this.name = Objects.requireNonNull(in_name, "name");
      this.zones = new LinkedList<>();
    }

    @Override
    public NTPresetName name()
    {
      return this.name;
    }

    @Override
    public NTPresetZoneBuilderType addZone()
    {
      final var zone = new PresetZoneBuilder(
        this.preset_generator_indices,
        this.preset_modulator_indices);
      this.zones.add(zone);
      return zone;
    }

    @Override
    public int bank()
    {
      return this.bank;
    }

    @Override
    public NTPresetBuilderType setBank(
      final int in_bank)
    {
      this.bank =
        RangeCheck.checkIncludedInInteger(
          in_bank,
          "Bank",
          NTRanges.PRESET_INDEX_RANGE,
          "Valid bank index ranges");
      return this;
    }
  }

  private static final class Generator
  {
    private final int index;
    private final NTGenerator generator;
    private final NTGenericAmount amount;

    private Generator(
      final int in_index,
      final NTGenerator in_generator,
      final NTGenericAmount in_amount)
    {
      this.index = in_index;
      this.generator = Objects.requireNonNull(in_generator, "generator");
      this.amount = Objects.requireNonNull(in_amount, "amount");
    }
  }

  private static final class Modulator
  {
    private final int index;

    private Modulator(
      final int in_index)
    {
      this.index = in_index;
    }
  }

  private static final class InstrumentZoneBuilder implements NTInstrumentZoneBuilderType
  {
    private final AtomicInteger instrument_generator_indices;
    private final AtomicInteger instrument_modulator_indices;
    private final TreeMap<Integer, Generator> generators;
    private final TreeMap<Integer, Modulator> modulators;

    private InstrumentZoneBuilder(
      final AtomicInteger in_instrument_generator_indices,
      final AtomicInteger in_instrument_modulator_indices)
    {
      this.instrument_generator_indices =
        Objects.requireNonNull(in_instrument_generator_indices, "instrument_generator_indices");
      this.instrument_modulator_indices =
        Objects.requireNonNull(in_instrument_modulator_indices, "instrument_modulator_indices");
      this.generators = new TreeMap<>();
      this.modulators = new TreeMap<>();
    }

    @Override
    public NTInstrumentZoneBuilderType addGenerator(
      final NTGenerator generator,
      final NTGenericAmount amount)
    {
      final var gen = new Generator(
        this.instrument_generator_indices.getAndIncrement(),
        generator,
        amount);
      this.generators.put(Integer.valueOf(gen.index), gen);
      return this;
    }
  }

  private static final class PresetZoneBuilder implements NTPresetZoneBuilderType
  {
    private final AtomicInteger preset_generator_indices;
    private final AtomicInteger preset_modulator_indices;
    private final TreeMap<Integer, Generator> generators;
    private final TreeMap<Integer, Modulator> modulators;

    private PresetZoneBuilder(
      final AtomicInteger in_preset_generator_indices,
      final AtomicInteger in_preset_modulator_indices)
    {
      this.preset_generator_indices =
        Objects.requireNonNull(in_preset_generator_indices, "preset_generator_indices");
      this.preset_modulator_indices =
        Objects.requireNonNull(in_preset_modulator_indices, "preset_modulator_indices");
      this.generators = new TreeMap<>();
      this.modulators = new TreeMap<>();
    }

    @Override
    public NTPresetZoneBuilderType addGenerator(
      final NTGenerator generator,
      final NTGenericAmount amount)
    {
      final var gen = new Generator(
        this.preset_generator_indices.getAndIncrement(),
        generator,
        amount);
      this.generators.put(Integer.valueOf(gen.index), gen);
      return this;
    }
  }

  private static final class SampleBuilder implements NTSampleBuilderType
  {
    private final int index;
    private NTSampleBuilderDescription description;

    private SampleBuilder(
      final int in_index,
      final NTSampleBuilderDescription in_description)
    {
      this.index = in_index;
      this.description = Objects.requireNonNull(in_description, "description");
    }

    @Override
    public NTSampleName name()
    {
      return this.description.name();
    }

    @Override
    public int sampleIndex()
    {
      return this.index;
    }

    @Override
    public long sampleCount()
    {
      return this.description.sampleCount();
    }

    @Override
    public NTSampleBuilderType setSampleCount(
      final long count)
    {
      this.description = this.description.withSampleCount(count);
      return this;
    }

    @Override
    public int sampleRate()
    {
      return this.description.sampleRate();
    }

    @Override
    public NTSampleBuilderType setSampleRate(
      final int rate)
    {
      this.description = this.description.withSampleRate(rate);
      return this;
    }

    @Override
    public NTSampleKind kind()
    {
      return this.description.kind();
    }

    @Override
    public NTSampleBuilderType setKind(
      final NTSampleKind in_kind)
    {
      this.description = this.description.withKind(in_kind);
      return this;
    }

    @Override
    public long loopStart()
    {
      return this.description.loopStart();
    }

    @Override
    public NTSampleBuilderType setLoopStart(
      final long start)
    {
      this.description = this.description.withLoopStart(start);
      return this;
    }

    @Override
    public long loopEnd()
    {
      return this.description.loopEnd();
    }

    @Override
    public NTSampleBuilderType setLoopEnd(
      final long end)
    {
      this.description = this.description.withLoopEnd(end);
      return this;
    }

    @Override
    public int originalPitch()
    {
      return this.description.originalPitch();
    }

    @Override
    public NTSampleBuilderType setOriginalPitch(
      final int pitch)
    {
      this.description = this.description.withOriginalPitch(pitch);
      return this;
    }

    @Override
    public int pitchCorrection()
    {
      return this.description.pitchCorrection();
    }

    @Override
    public NTSampleBuilderType setPitchCorrection(
      final int pitch)
    {
      this.description = this.description.withPitchCorrection(pitch);
      return this;
    }

    @Override
    public NTSampleBuilderType setDataWriter(
      final NTSampleDataWriterType writer)
    {
      this.description = this.description.withDataWriter(writer);
      return this;
    }
  }
}
