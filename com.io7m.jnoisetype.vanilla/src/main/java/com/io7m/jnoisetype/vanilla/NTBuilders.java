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

package com.io7m.jnoisetype.vanilla;

import com.io7m.jnoisetype.api.NTBankIndex;
import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGeneratorIndex;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentIndex;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTModulatorIndex;
import com.io7m.jnoisetype.api.NTPitch;
import com.io7m.jnoisetype.api.NTPresetIndex;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTSampleIndex;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTTransform;
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
    private final SortedMap<NTSampleIndex, NTSampleWriterDescription> description_samples;
    private final SortedMap<NTInstrumentIndex, NTInstrumentWriterDescription> description_instruments;
    private final SortedMap<NTPresetIndex, NTPresetWriterDescription> description_presets;

    WriterDescription(
      final NTInfo in_info,
      final TreeMap<NTSampleIndex, NTSampleWriterDescription> in_description_samples,
      final TreeMap<NTInstrumentIndex, NTInstrumentWriterDescription> in_description_instruments,
      final TreeMap<NTPresetIndex, NTPresetWriterDescription> in_preset_descriptions)
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
    public SortedMap<NTSampleIndex, NTSampleWriterDescription> samples()
    {
      return this.description_samples;
    }

    @Override
    public SortedMap<NTInstrumentIndex, NTInstrumentWriterDescription> instruments()
    {
      return this.description_instruments;
    }

    @Override
    public SortedMap<NTPresetIndex, NTPresetWriterDescription> presets()
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

    private static TreeMap<NTInstrumentIndex, NTInstrumentWriterDescription> buildInstrumentDescriptions(
      final TreeMap<NTInstrumentName, InstrumentBuilder> instruments)
    {
      var bag_index = 0;
      final var instrument_descriptions = new TreeMap<NTInstrumentIndex, NTInstrumentWriterDescription>();
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
                .setModulationAmount(modulator.modulation_amount)
                .setModulationAmountSourceOperator(modulator.modulation_amount_source_operator)
                .setModulationTransformOperator(modulator.modulation_transform_operator)
                .setSourceOperator(modulator.source_operator)
                .setTargetOperator(modulator.target_operator)
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

        instrument_descriptions.put(instrument.index, description);
        bag_index = Math.addExact(bag_index, instrument.zones.size());
      }
      return instrument_descriptions;
    }

    private static TreeMap<NTPresetIndex, NTPresetWriterDescription> buildPresetDescriptions(
      final TreeMap<NTPresetName, PresetBuilder> presets)
    {
      var bag_index = 0;
      final var preset_descriptions = new TreeMap<NTPresetIndex, NTPresetWriterDescription>();
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
                .setModulationAmount(modulator.modulation_amount)
                .setModulationAmountSourceOperator(modulator.modulation_amount_source_operator)
                .setModulationTransformOperator(modulator.modulation_transform_operator)
                .setSourceOperator(modulator.source_operator)
                .setTargetOperator(modulator.target_operator)
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

        preset_descriptions.put(preset.index, description);
        bag_index = Math.addExact(bag_index, preset.zones.size());
      }
      return preset_descriptions;
    }

    private static TreeMap<NTSampleIndex, NTSampleWriterDescription> buildSampleDescriptions(
      final TreeMap<NTSampleName, SampleBuilder> samples)
    {
      var offset_start = 0L;
      final var sample_descriptions = new TreeMap<NTSampleIndex, NTSampleWriterDescription>();
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

        sample_descriptions.put(sample.index, description);
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
          .setOriginalPitch(NTPitch.of(60))
          .setPitchCorrection(0)
          .setSampleRate(48000)
          .build();

      final var builder = new SampleBuilder(NTSampleIndex.of(this.samples.size()), description);
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
          NTInstrumentIndex.of(this.instruments.size()),
          name);

      this.instruments.put(name, builder);
      return builder;
    }

    @Override
    public NTPresetBuilderType addPreset(
      final NTBankIndex bank,
      final NTPresetName name)
    {
      Objects.requireNonNull(bank, "bank");
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
          bank,
          this.preset_generator_indices,
          this.preset_modulator_indices,
          NTPresetIndex.of(this.presets.size()),
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
    private final NTInstrumentIndex index;

    private InstrumentBuilder(
      final AtomicInteger in_instrument_generator_indices,
      final AtomicInteger in_instrument_modulator_indices,
      final NTInstrumentIndex in_index,
      final NTInstrumentName in_name)
    {
      this.instrument_generator_indices =
        Objects.requireNonNull(in_instrument_generator_indices, "instrument_generator_indices");
      this.instrument_modulator_indices =
        Objects.requireNonNull(in_instrument_modulator_indices, "instrument_modulator_indices");
      this.index =
        Objects.requireNonNull(in_index, "index");
      this.name =
        Objects.requireNonNull(in_name, "name");

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
    public NTInstrumentIndex instrumentIndex()
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
    private final NTPresetIndex index;
    private NTBankIndex bank;

    private PresetBuilder(
      final NTBankIndex inBank,
      final AtomicInteger in_preset_generator_indices,
      final AtomicInteger in_preset_modulator_indices,
      final NTPresetIndex in_index,
      final NTPresetName in_name)
    {
      this.bank =
        Objects.requireNonNull(inBank, "inBank");
      this.preset_generator_indices =
        Objects.requireNonNull(in_preset_generator_indices, "preset_generator_indices");
      this.preset_modulator_indices =
        Objects.requireNonNull(in_preset_modulator_indices, "preset_modulator_indices");
      this.index =
        Objects.requireNonNull(in_index, "index");
      this.name =
        Objects.requireNonNull(in_name, "name");
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
    public NTBankIndex bank()
    {
      return this.bank;
    }

    @Override
    public NTPresetBuilderType setBank(
      final int in_bank)
    {
      this.bank = NTBankIndex.of(in_bank);
      return this;
    }
  }

  private static final class Generator
  {
    private final NTGeneratorIndex index;
    private final NTGenerator generator;
    private final NTGenericAmount amount;

    private Generator(
      final NTGeneratorIndex in_index,
      final NTGenerator in_generator,
      final NTGenericAmount in_amount)
    {
      this.index =
        Objects.requireNonNull(in_index, "index");
      this.generator =
        Objects.requireNonNull(in_generator, "generator");
      this.amount =
        Objects.requireNonNull(in_amount, "amount");
    }
  }

  private static final class Modulator
  {
    private final NTModulatorIndex index;
    private final int source_operator;
    private final NTGenerator target_operator;
    private final short modulation_amount;
    private final int modulation_amount_source_operator;
    private final NTTransform modulation_transform_operator;

    private Modulator(
      final NTModulatorIndex in_index,
      final int in_source_operator,
      final NTGenerator in_target_operator,
      final short in_modulation_amount,
      final int in_modulation_amount_source_operator,
      final NTTransform in_modulation_transform_operator)
    {
      this.index =
        Objects.requireNonNull(in_index, "index");
      this.source_operator =
        in_source_operator;
      this.target_operator =
        Objects.requireNonNull(in_target_operator, "target_operator");
      this.modulation_amount =
        in_modulation_amount;
      this.modulation_amount_source_operator =
        in_modulation_amount_source_operator;
      this.modulation_transform_operator =
        Objects.requireNonNull(in_modulation_transform_operator, "modulation_transform_operator");
    }
  }

  private static final class InstrumentZoneBuilder implements NTInstrumentZoneBuilderType
  {
    private final AtomicInteger instrument_generator_indices;
    private final AtomicInteger instrument_modulator_indices;
    private final TreeMap<NTGeneratorIndex, Generator> generators;
    private final TreeMap<NTModulatorIndex, Modulator> modulators;

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
        NTGeneratorIndex.of(this.instrument_generator_indices.getAndIncrement()),
        generator,
        amount);
      this.generators.put(gen.index, gen);
      return this;
    }

    @Override
    public NTInstrumentZoneBuilderType addModulator(
      final int source_operator,
      final NTGenerator target_operator,
      final short modulation_amount,
      final int modulation_amount_source_operator,
      final NTTransform modulation_transform_operator)
    {
      Objects.requireNonNull(target_operator, "target_operator");
      Objects.requireNonNull(modulation_transform_operator, "modulation_transform_operator");

      final var mod =
        new Modulator(
          NTModulatorIndex.of(this.instrument_modulator_indices.getAndIncrement()),
          source_operator,
          target_operator,
          modulation_amount,
          modulation_amount_source_operator,
          modulation_transform_operator);

      this.modulators.put(mod.index, mod);
      return this;
    }
  }

  private static final class PresetZoneBuilder implements NTPresetZoneBuilderType
  {
    private final AtomicInteger preset_generator_indices;
    private final AtomicInteger preset_modulator_indices;
    private final TreeMap<NTGeneratorIndex, Generator> generators;
    private final TreeMap<NTModulatorIndex, Modulator> modulators;

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
        NTGeneratorIndex.of(this.preset_generator_indices.getAndIncrement()),
        generator,
        amount);
      this.generators.put(gen.index, gen);
      return this;
    }

    @Override
    public NTPresetZoneBuilderType addModulator(
      final int source_operator,
      final NTGenerator target_operator,
      final short modulation_amount,
      final int modulation_amount_source_operator,
      final NTTransform modulation_transform_operator)
    {
      Objects.requireNonNull(target_operator, "target_operator");
      Objects.requireNonNull(modulation_transform_operator, "modulation_transform_operator");

      final var mod =
        new Modulator(
          NTModulatorIndex.of(this.preset_modulator_indices.getAndIncrement()),
          source_operator,
          target_operator,
          modulation_amount,
          modulation_amount_source_operator,
          modulation_transform_operator);

      this.modulators.put(mod.index, mod);
      return this;
    }
  }

  private static final class SampleBuilder implements NTSampleBuilderType
  {
    private final NTSampleIndex index;
    private NTSampleBuilderDescription description;
    private NTSampleIndex linked;

    private SampleBuilder(
      final NTSampleIndex in_index,
      final NTSampleBuilderDescription in_description)
    {
      this.index =
        Objects.requireNonNull(in_index, "index");
      this.description =
        Objects.requireNonNull(in_description, "description");
    }

    @Override
    public NTSampleName name()
    {
      return this.description.name();
    }

    @Override
    public NTSampleIndex sampleIndex()
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
    public NTSampleBuilderType setLinked(final NTSampleIndex inIndex)
    {
      this.description = this.description.withLinked(inIndex);
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
    public NTPitch originalPitch()
    {
      return this.description.originalPitch();
    }

    @Override
    public NTSampleBuilderType setOriginalPitch(
      final NTPitch pitch)
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
