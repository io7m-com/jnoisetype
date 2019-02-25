package com.io7m.jnoisetype.tests;

import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGenerators;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.parser.api.NTParsedFile;
import com.io7m.jnoisetype.parser.api.NTParsedInstrument;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZone;
import com.io7m.jnoisetype.parser.api.NTParsedPreset;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZone;
import com.io7m.jnoisetype.vanilla.NTInterpreters;
import com.io7m.jnoisetype.vanilla.NTParsers;
import com.io7m.jranges.RangeInclusiveI;
import com.io7m.jspiel.vanilla.RiffParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

public final class Try
{
  private static final Logger LOG = LoggerFactory.getLogger(Try.class);

  private Try()
  {

  }

  public static void main(final String[] args)
    throws IOException, NTParseException
  {
    final var parsers = NTParsers.create(new RiffParsers());
    final var path = Paths.get(args[0]);
    try (var channel = FileChannel.open(path, READ)) {
      final var map = channel.map(READ_ONLY, 0L, channel.size());
      final var parser = parsers.createForByteBuffer(path.toUri(), map);
      final var file = parser.parse();
      final var presets = parsePresets(file);
      final var instruments = parseInstruments(file);

      NTInterpreters.interpretFile(file);
    }
  }

  private static List<Instrument> parseInstruments(
    final NTParsedFile file)
  {
    final var instruments = new ArrayList<Instrument>();
    final var generators = NTGenerators.generators();
    final var parsed_instruments = new LinkedBlockingDeque<>(file.instrumentRecords());

    while (true) {
      final var parsed_instrument = parsed_instruments.poll();
      if (parsed_instrument == null) {
        break;
      }

      final var instrument_zones = file.instrumentZoneRecords();
      final var instrument_zone_generators = file.instrumentZoneGeneratorRecords();
      final var parsed_instrument_next = parsed_instruments.peek();

      final var index_range =
        getInstrumentZoneRange(parsed_instrument, instrument_zones, parsed_instrument_next);

      final var instrument = new Instrument(parsed_instrument.name().value());
      for (var zone_index = index_range.lower(); zone_index <= index_range.upper(); ++zone_index) {
        final var parsed_instrument_zone = instrument_zones.get(zone_index);

        final int generator_upper;
        if (zone_index + 1 < index_range.upper()) {
          final var parsed_instrument_zone_next = instrument_zones.get(zone_index + 1);
          generator_upper = parsed_instrument_zone_next.generatorIndex() - 1;
        } else {
          generator_upper = instrument_zone_generators.size() - 1;
        }

        final var zone = new InstrumentZone();
        instrument.zones.add(zone);

        for (var generator_index = parsed_instrument_zone.generatorIndex(); generator_index <= generator_upper; ++generator_index) {
          final var instrument_zone_generator =
            instrument_zone_generators.get(generator_index);
          final var generator =
            generators.get(Integer.valueOf(instrument_zone_generator.generatorOperator()));

          LOG.debug(
            "instrument [{}]: zone {} generator {}: {}: amount {} ({}:{})",
            instrument.name,
            Integer.valueOf(zone_index),
            Integer.valueOf(generator_index),
            generator,
            Integer.valueOf(instrument_zone_generator.amount().value()),
            Integer.valueOf(instrument_zone_generator.amount().asUnsignedBytesLow()),
            Integer.valueOf(instrument_zone_generator.amount().asUnsignedBytesHigh()));

          zone.generators.add(new InstrumentGenerator(generator));
        }
      }
      instruments.add(instrument);
    }

    return instruments;
  }

  private static List<Preset> parsePresets(
    final NTParsedFile file)
  {
    final var presets = new ArrayList<Preset>();
    final var generators = NTGenerators.generators();
    final var parsed_presets = new LinkedBlockingDeque<>(file.presetRecords());

    while (true) {
      final var parsed_preset = parsed_presets.poll();
      if (parsed_preset == null) {
        break;
      }

      final var preset_zones = file.presetZoneRecords();
      final var preset_zone_generators = file.presetZoneGeneratorRecords();
      final var preset_zone_modulators = file.presetZoneModulatorRecords();
      final var parsed_preset_next = parsed_presets.peek();

      final var zone_index_range =
        getPresetZoneRange(parsed_preset, preset_zones, parsed_preset_next);

      final var preset = new Preset(parsed_preset.name().value());
      for (var zone_index = zone_index_range.lower(); zone_index <= zone_index_range.upper(); ++zone_index) {
        final var parsed_preset_zone = preset_zones.get(zone_index);

        final var generator_lower = parsed_preset_zone.generatorIndex();
        final int generator_upper;
        if (zone_index + 1 < zone_index_range.upper()) {
          final var parsed_preset_zone_next = preset_zones.get(zone_index + 1);
          generator_upper = parsed_preset_zone_next.generatorIndex() - 1;
        } else {
          generator_upper = preset_zone_generators.size() - 1;
        }

        final var modulator_lower = parsed_preset_zone.modulatorIndex();
        final int modulator_upper;
        if (zone_index + 1 < zone_index_range.upper()) {
          final var parsed_preset_zone_next = preset_zones.get(zone_index + 1);
          modulator_upper = parsed_preset_zone_next.modulatorIndex() - 1;
        } else {
          modulator_upper = preset_zone_modulators.size() - 1;
        }

        final var zone = new PresetZone();
        preset.zones.add(zone);

        for (var generator_index = generator_lower; generator_index <= generator_upper; ++generator_index) {
          final var preset_zone_generator =
            preset_zone_generators.get(generator_index);
          final var generator =
            generators.get(Integer.valueOf(preset_zone_generator.generatorOperator()));

          LOG.debug(
            "preset [{}]: zone {} generator {}: {}: amount {} ({}:{})",
            preset.name,
            Integer.valueOf(zone_index),
            Integer.valueOf(generator_index),
            generator,
            Integer.valueOf(preset_zone_generator.amount().value()),
            Integer.valueOf(preset_zone_generator.amount().asUnsignedBytesLow()),
            Integer.valueOf(preset_zone_generator.amount().asUnsignedBytesHigh()));

          zone.generators.add(new PresetGenerator(generator));
        }

        for (var modulator_index = modulator_lower; modulator_index <= modulator_upper; ++modulator_index) {
          final var preset_zone_modulator =
            preset_zone_modulators.get(modulator_index);

          LOG.debug(
            "preset [{}]: zone {} modulator {}",
            preset.name,
            Integer.valueOf(zone_index),
            Integer.valueOf(modulator_index));

          zone.modulators.add(new PresetModulator());
        }
      }
      presets.add(preset);
    }

    return presets;
  }

  private static RangeInclusiveI getPresetZoneRange(
    final NTParsedPreset parsed_preset,
    final List<NTParsedPresetZone> preset_zones,
    final NTParsedPreset parsed_preset_next)
  {
    final RangeInclusiveI index_range;
    if (parsed_preset_next != null) {
      index_range = RangeInclusiveI.of(
        parsed_preset.presetBagIndex(),
        parsed_preset_next.presetBagIndex() - 1);
    } else {
      index_range = RangeInclusiveI.of(parsed_preset.presetBagIndex(), preset_zones.size() - 1);
    }
    return index_range;
  }

  private static RangeInclusiveI getInstrumentZoneRange(
    final NTParsedInstrument parsed_instrument,
    final List<NTParsedInstrumentZone> instrument_zones,
    final NTParsedInstrument parsed_instrument_next)
  {
    final RangeInclusiveI index_range;
    if (parsed_instrument_next != null) {
      index_range = RangeInclusiveI.of(
        parsed_instrument.instrumentZoneIndex(),
        parsed_instrument_next.instrumentZoneIndex() - 1);
    } else {
      index_range = RangeInclusiveI.of(
        parsed_instrument.instrumentZoneIndex(),
        instrument_zones.size() - 1);
    }
    return index_range;
  }

  private static final class Preset
  {
    private final String name;
    private final List<PresetZone> zones;

    private Preset(
      final String in_name)
    {
      this.name = Objects.requireNonNull(in_name, "name");
      this.zones = new ArrayList<>();
    }
  }

  private static final class PresetGenerator
  {
    private final NTGenerator generator;

    private PresetGenerator(
      final NTGenerator in_generator)
    {
      this.generator = Objects.requireNonNull(in_generator, "generator");
    }
  }

  private static final class PresetModulator
  {
    private PresetModulator()
    {

    }
  }

  private static final class PresetZone
  {
    private final List<PresetGenerator> generators;
    private final List<PresetModulator> modulators;

    private PresetZone()
    {
      this.generators = new ArrayList<>();
      this.modulators = new ArrayList<>();
    }
  }

  private static final class Instrument
  {
    private final String name;
    private final List<InstrumentZone> zones;

    private Instrument(
      final String in_name)
    {
      this.name = Objects.requireNonNull(in_name, "name");
      this.zones = new ArrayList<>();
    }
  }

  private static final class InstrumentGenerator
  {
    private final NTGenerator generator;

    private InstrumentGenerator(
      final NTGenerator in_generator)
    {
      this.generator = Objects.requireNonNull(in_generator, "generator");
    }
  }

  private static final class InstrumentZone
  {
    private final List<InstrumentGenerator> generators;

    private InstrumentZone()
    {
      this.generators = new ArrayList<>();
    }
  }
}
