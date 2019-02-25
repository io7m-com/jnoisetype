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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTGenerator;
import com.io7m.jnoisetype.api.NTGenerators;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTInstrumentType;
import com.io7m.jnoisetype.api.NTInstrumentZoneGeneratorType;
import com.io7m.jnoisetype.api.NTInstrumentZoneModulatorType;
import com.io7m.jnoisetype.api.NTInstrumentZoneType;
import com.io7m.jnoisetype.api.NTNamedType;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTPresetType;
import com.io7m.jnoisetype.api.NTPresetZoneGeneratorType;
import com.io7m.jnoisetype.api.NTPresetZoneModulatorType;
import com.io7m.jnoisetype.api.NTPresetZoneType;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jnoisetype.api.NTSampleType;
import com.io7m.jnoisetype.parser.api.NTInterpreterProviderType;
import com.io7m.jnoisetype.parser.api.NTInterpreterType;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.parser.api.NTParsedFile;
import com.io7m.jnoisetype.parser.api.NTParsedInstrument;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZone;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZoneGenerator;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZoneModulator;
import com.io7m.jnoisetype.parser.api.NTParsedPreset;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZone;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZoneGenerator;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZoneModulator;
import com.io7m.jnoisetype.parser.api.NTParsedSample;
import com.io7m.jranges.RangeCheck;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An interpreter of parsed SoundFontⓡ values.
 */

@Component
public final class NTInterpreters implements NTInterpreterProviderType
{
  private static final Logger LOG = LoggerFactory.getLogger(NTInterpreters.class);

  /**
   * Construct a provider.
   */

  public NTInterpreters()
  {

  }

  /**
   * Interpret a parsed file.
   *
   * @param file The file
   *
   * @return An interpreted file
   *
   * @throws NTParseException If interpreting the file fails
   */

  public static NTFontType interpretFile(
    final NTParsedFile file)
    throws NTParseException
  {
    return new Interpreter(file).interpret();
  }

  @Override
  public NTInterpreterType createInterpreter(
    final NTParsedFile file)
  {
    return new Interpreter(Objects.requireNonNull(file, "file"));
  }

  private static final class Interpreter implements NTInterpreterType
  {
    private final NTParsedFile file;
    private final Map<Integer, NTGenerator> generators;

    private Interpreter(
      final NTParsedFile in_file)
    {
      this.file = Objects.requireNonNull(in_file, "file");
      this.generators = NTGenerators.generators();
    }

    private static boolean isNamedTerminalRecord(
      final int index,
      final int count,
      final NTNamedType record,
      final String name)
    {
      return index + 1 == count && Objects.equals(record.nameText(), name);
    }

    private static Sample interpretSample(
      final Font font,
      final int sample_index,
      final NTParsedSample sample)
    {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "sample [{}][\"{}\"] {}",
          Integer.valueOf(sample_index),
          sample.name().value(),
          sample.kind());
      }

      return new Sample(font, sample);
    }

    @Override
    public NTFontType interpret()
      throws NTParseException
    {
      final var font = new Font(this.file.info());
      this.interpretSamples(font);
      this.interpretInstruments(font);
      this.interpretPresets(font);
      return font;
    }

    private void interpretPresets(final Font font)
      throws NTParseException
    {
      final var phdr = this.file.phdr();

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "interpreting {} preset records (including terminal record)",
          Integer.valueOf(phdr.size()));
      }

      NTInvariants.checkNamedTerminalRecordExists(
        phdr,
        "EOP",
        "7.2",
        this.file.presetRecordsSource());

      for (var preset_index = 0; preset_index < phdr.size(); ++preset_index) {
        final var input_preset_curr = phdr.get(preset_index);
        if (isNamedTerminalRecord(preset_index, phdr.size(), input_preset_curr, "EOP")) {
          break;
        }

        final var input_preset_next = phdr.get(preset_index + 1);
        font.presets.add(
          this.interpretPreset(
            font,
            preset_index,
            input_preset_curr,
            input_preset_next));
      }
    }

    private NTPresetType interpretPreset(
      final Font font,
      final int preset_index,
      final NTParsedPreset input_preset_curr,
      final NTParsedPreset input_preset_next)
      throws NTParseException
    {
      final var zone_range =
        new RangeInclusiveExclusiveI(
          input_preset_curr.presetBagIndex(),
          input_preset_next.presetBagIndex());

      final var preset =
        new Preset(font, preset_index, input_preset_curr.name());

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone range [{}, {}) ({} zones)",
          Integer.valueOf(preset_index),
          input_preset_curr.name().value(),
          Integer.valueOf(zone_range.lower()),
          Integer.valueOf(zone_range.upper()),
          Integer.valueOf(zone_range.interval()));
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        this.file.pbag(),
        "7.7",
        this.file.presetZoneRecordsSource());

      if (zone_range.interval() < 2) {
        return preset;
      }

      final var pbag = this.file.pbag();

      for (var zone_index = zone_range.lower(); zone_index < zone_range.upper(); ++zone_index) {
        final var zone_curr = pbag.get(zone_index);
        final var zone_next = pbag.get(zone_index + 1);

        preset.zones.add(
          this.interpretPresetZone(
            preset_index,
            preset,
            zone_index,
            zone_range.lower(),
            zone_curr,
            zone_next));
      }

      return preset;
    }

    private PresetZone interpretPresetZone(
      final int preset_index,
      final Preset preset,
      final int zone_index,
      final int zone_lower,
      final NTParsedPresetZone zone_curr,
      final NTParsedPresetZone zone_next)
      throws NTParseException
    {
      final var pgen = this.file.pgen();
      final var pmod = this.file.pmod();

      NTInvariants.checkUnnamedTerminalRecordExists(
        pgen,
        "7.5",
        this.file.presetZoneGeneratorRecordsSource());

      NTInvariants.checkUnnamedTerminalRecordExists(
        pmod,
        "7.4",
        this.file.presetZoneModulatorRecordsSource());

      final var zone =
        new PresetZone(preset, zone_index - zone_lower);

      final var gen_range =
        new RangeInclusiveExclusiveI(
          zone_curr.generatorIndex(),
          zone_next.generatorIndex());

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone [{}] generator range [{}, {}) ({} generators)",
          Integer.valueOf(preset_index),
          preset.name().value(),
          Integer.valueOf(zone_index),
          Integer.valueOf(gen_range.lower()),
          Integer.valueOf(gen_range.upper()),
          Integer.valueOf(gen_range.interval()));
      }

      if (gen_range.interval() >= 2) {
        for (var gen_index = gen_range.lower(); gen_index < gen_range.upper(); ++gen_index) {
          final var gen_curr = pgen.get(gen_index);
          zone.generators.add(this.interpretPresetZoneGenerator(zone, gen_curr));
        }
      }

      final var mod_range =
        new RangeInclusiveExclusiveI(
          zone_curr.modulatorIndex(),
          zone_next.modulatorIndex());

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone [{}] modulator range [{}, {}) ({} modulators)",
          Integer.valueOf(preset_index),
          preset.name().value(),
          Integer.valueOf(zone_index),
          Integer.valueOf(mod_range.lower()),
          Integer.valueOf(mod_range.upper()),
          Integer.valueOf(mod_range.interval()));
      }

      if (mod_range.interval() >= 2) {
        for (var mod_index = mod_range.lower(); mod_index < mod_range.upper(); ++mod_index) {
          final var mod_curr = pmod.get(mod_index);
          final var mod_next = pmod.get(mod_index + 1);
          zone.modulators.add(this.interpretPresetZoneModulator(
            zone,
            mod_index,
            mod_curr,
            mod_next));
        }
      }

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone [{}] global {}",
          Integer.valueOf(preset_index),
          preset.name().value(),
          Integer.valueOf(zone_index),
          Boolean.valueOf(zone.isGlobal()));
      }

      return zone;
    }


    private PresetZoneModulator interpretPresetZoneModulator(
      final PresetZone zone,
      final int mod_index,
      final NTParsedPresetZoneModulator mod_curr,
      final NTParsedPresetZoneModulator mod_next)
    {
      final var named_generator =
        this.generators.get(Integer.valueOf(mod_curr.targetOperator()));

      return new PresetZoneModulator(zone);
    }

    private PresetZoneGenerator interpretPresetZoneGenerator(
      final PresetZone zone,
      final NTParsedPresetZoneGenerator gen_curr)
    {
      final var named_generator =
        this.generators.get(Integer.valueOf(gen_curr.generatorOperator()));
      final var amount =
        NTGenericAmount.of(gen_curr.amount().value());

      return new PresetZoneGenerator(zone, named_generator, amount);
    }


    private void interpretInstruments(
      final Font font)
      throws NTParseException
    {
      final var inst = this.file.inst();

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "interpreting {} preset records (including terminal record)",
          Integer.valueOf(inst.size()));
      }

      NTInvariants.checkNamedTerminalRecordExists(
        inst,
        "EOI",
        "7.6",
        this.file.instrumentRecordsSource());

      for (var instrument_index = 0; instrument_index < inst.size(); ++instrument_index) {
        final var input_instrument_curr = inst.get(instrument_index);
        if (isNamedTerminalRecord(instrument_index, inst.size(), input_instrument_curr, "EOI")) {
          break;
        }

        final var input_instrument_next = inst.get(instrument_index + 1);
        font.instruments.add(
          this.interpretInstrument(
            font,
            instrument_index,
            input_instrument_curr,
            input_instrument_next));
      }
    }

    private Instrument interpretInstrument(
      final Font font,
      final int instrument_index,
      final NTParsedInstrument input_instrument_curr,
      final NTParsedInstrument input_instrument_next)
      throws NTParseException
    {
      final var zone_range =
        new RangeInclusiveExclusiveI(
          input_instrument_curr.instrumentZoneIndex(),
          input_instrument_next.instrumentZoneIndex());

      final var instrument =
        new Instrument(font, instrument_index, input_instrument_curr.name());

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone range [{}, {}) ({} zones)",
          Integer.valueOf(instrument_index),
          input_instrument_curr.name().value(),
          Integer.valueOf(zone_range.lower()),
          Integer.valueOf(zone_range.upper()),
          Integer.valueOf(zone_range.interval()));
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        this.file.ibag(),
        "7.7",
        this.file.instrumentZoneRecordsSource());

      if (zone_range.interval() < 2) {
        return instrument;
      }

      final var ibag = this.file.ibag();

      for (var zone_index = zone_range.lower(); zone_index < zone_range.upper(); ++zone_index) {
        final var zone_curr = ibag.get(zone_index);
        final var zone_next = ibag.get(zone_index + 1);

        instrument.zones.add(
          this.interpretInstrumentZone(
            instrument_index,
            instrument,
            zone_index,
            zone_range.lower(),
            zone_curr,
            zone_next));
      }

      return instrument;
    }

    private InstrumentZone interpretInstrumentZone(
      final int instrument_index,
      final Instrument instrument,
      final int zone_index,
      final int zone_lower,
      final NTParsedInstrumentZone zone_curr,
      final NTParsedInstrumentZone zone_next)
      throws NTParseException
    {
      final var igen = this.file.igen();
      final var imod = this.file.imod();

      NTInvariants.checkUnnamedTerminalRecordExists(
        igen,
        "7.9",
        this.file.instrumentZoneGeneratorRecordsSource());

      NTInvariants.checkUnnamedTerminalRecordExists(
        imod,
        "7.8",
        this.file.instrumentZoneModulatorRecordsSource());

      final var zone =
        new InstrumentZone(instrument, zone_index - zone_lower);

      final var gen_range =
        new RangeInclusiveExclusiveI(
          zone_curr.generatorIndex(),
          zone_next.generatorIndex());

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone [{}] generator range [{}, {}) ({} generators)",
          Integer.valueOf(instrument_index),
          instrument.name().value(),
          Integer.valueOf(zone_index),
          Integer.valueOf(gen_range.lower()),
          Integer.valueOf(gen_range.upper()),
          Integer.valueOf(gen_range.interval()));
      }

      if (gen_range.interval() >= 2) {
        for (var gen_index = gen_range.lower(); gen_index < gen_range.upper(); ++gen_index) {
          final var gen_curr = igen.get(gen_index);
          zone.generators.add(this.interpretInstrumentZoneGenerator(zone, gen_curr));
        }
      }

      final var mod_range =
        new RangeInclusiveExclusiveI(
          zone_curr.modulatorIndex(),
          zone_next.modulatorIndex());

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone [{}] modulator range [{}, {}) ({} modulators)",
          Integer.valueOf(instrument_index),
          instrument.name().value(),
          Integer.valueOf(zone_index),
          Integer.valueOf(mod_range.lower()),
          Integer.valueOf(mod_range.upper()),
          Integer.valueOf(mod_range.interval()));
      }

      if (mod_range.interval() >= 2) {
        for (var mod_index = mod_range.lower(); mod_index < mod_range.upper(); ++mod_index) {
          final var mod_curr = imod.get(mod_index);
          final var mod_next = imod.get(mod_index + 1);
          zone.modulators.add(
            this.interpretInstrumentZoneModulator(zone, mod_index, mod_curr, mod_next));
        }
      }

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone [{}] global {}",
          Integer.valueOf(instrument_index),
          instrument.name().value(),
          Integer.valueOf(zone_index),
          Boolean.valueOf(zone.isGlobal()));
      }

      return zone;
    }

    private InstrumentZoneModulator interpretInstrumentZoneModulator(
      final InstrumentZone zone,
      final int mod_index,
      final NTParsedInstrumentZoneModulator mod_curr,
      final NTParsedInstrumentZoneModulator mod_next)
    {
      final var imod = this.file.imod();

      return new InstrumentZoneModulator(zone);
    }

    private InstrumentZoneGenerator interpretInstrumentZoneGenerator(
      final InstrumentZone zone,
      final NTParsedInstrumentZoneGenerator gen_curr)
    {
      final var named_generator =
        this.generators.get(Integer.valueOf(gen_curr.generatorOperator()));
      final var amount =
        NTGenericAmount.of(gen_curr.amount().value());

      return new InstrumentZoneGenerator(zone, named_generator, amount);
    }

    private void interpretSamples(
      final Font font)
      throws NTParseException
    {
      final var samples = this.file.sampleRecords();

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "interpreting {} sample records (including terminal record)",
          Integer.valueOf(samples.size()));
      }

      NTInvariants.checkNamedTerminalRecordExists(
        samples,
        "EOS",
        "7.10",
        this.file.sampleRecordsSource());

      for (var sample_index = 0; sample_index < samples.size(); ++sample_index) {
        final var input_sample = samples.get(sample_index);
        if (isNamedTerminalRecord(sample_index, samples.size(), input_sample, "EOS")) {
          break;
        }

        font.samples.add(interpretSample(font, sample_index, input_sample));
      }
    }
  }

  /**
   * A range with an inclusive lower bound and an exclusive upper bound.
   */

  private static final class RangeInclusiveExclusiveI
  {
    private final int lo;
    private final int hi;

    RangeInclusiveExclusiveI(
      final int in_lower,
      final int in_upper)
    {
      RangeCheck.checkLessEqualInteger(
        in_lower,
        "Lower range",
        in_upper,
        "Upper range");

      this.lo = in_lower;
      this.hi = in_upper;
    }

    int lower()
    {
      return this.lo;
    }

    int upper()
    {
      return this.hi;
    }

    int interval()
    {
      return this.hi - this.lo;
    }
  }

  private static final class InstrumentZone implements NTInstrumentZoneType
  {
    private final Instrument instrument;
    private final List<InstrumentZoneGenerator> generators;
    private final List<NTInstrumentZoneGeneratorType> generators_read;
    private final List<InstrumentZoneModulator> modulators;
    private final List<NTInstrumentZoneModulatorType> modulators_read;
    private final int index;

    private InstrumentZone(
      final Instrument in_instrument,
      final int in_index)
    {
      Preconditions.checkPreconditionI(
        in_index,
        in_index >= 0,
        x -> "Zone index must be > 0");

      this.instrument = Objects.requireNonNull(in_instrument, "preset");
      this.index = in_index;
      this.generators = new ArrayList<>();
      this.generators_read = Collections.unmodifiableList(this.generators);
      this.modulators = new ArrayList<>();
      this.modulators_read = Collections.unmodifiableList(this.modulators);
    }

    @Override
    public String toString()
    {
      return new StringBuilder(32)
        .append("[InstrumentZone ")
        .append(this.index)
        .append(']')
        .toString();
    }

    @Override
    public NTInstrumentType instrument()
    {
      return this.instrument;
    }

    @Override
    public boolean isGlobal()
    {
      return this.isFirstZone() && this.lastGeneratorIsNotInstrument();
    }

    @Override
    public List<NTInstrumentZoneGeneratorType> generators()
    {
      return this.generators_read;
    }

    @Override
    public List<NTInstrumentZoneModulatorType> modulators()
    {
      return this.modulators_read;
    }

    private boolean lastGeneratorIsNotInstrument()
    {
      if (this.generators.isEmpty()) {
        return true;
      }

      final var last_generator = this.generators.get(this.generators.size() - 1);
      return !Objects.equals(last_generator.generator.name(), "preset");
    }

    private boolean isFirstZone()
    {
      return this.index == 0;
    }
  }

  private static final class PresetZone implements NTPresetZoneType
  {
    private final Preset preset;
    private final List<PresetZoneGenerator> generators;
    private final List<NTPresetZoneGeneratorType> generators_read;
    private final List<PresetZoneModulator> modulators;
    private final List<NTPresetZoneModulatorType> modulators_read;
    private final int index;

    private PresetZone(
      final Preset in_preset,
      final int in_index)
    {
      Preconditions.checkPreconditionI(
        in_index,
        in_index >= 0,
        x -> "Zone index must be > 0");

      this.preset = Objects.requireNonNull(in_preset, "preset");
      this.index = in_index;
      this.generators = new ArrayList<>();
      this.generators_read = Collections.unmodifiableList(this.generators);
      this.modulators = new ArrayList<>();
      this.modulators_read = Collections.unmodifiableList(this.modulators);
    }

    @Override
    public String toString()
    {
      return new StringBuilder(32)
        .append("[PresetZone ")
        .append(this.index)
        .append(']')
        .toString();
    }

    @Override
    public List<NTPresetZoneGeneratorType> generators()
    {
      return this.generators_read;
    }

    @Override
    public List<NTPresetZoneModulatorType> modulators()
    {
      return this.modulators_read;
    }

    private boolean lastGeneratorIsNotInstrument()
    {
      if (this.generators.isEmpty()) {
        return true;
      }

      final var last_generator = this.generators.get(this.generators.size() - 1);
      return !Objects.equals(last_generator.generator.name(), "instrument");
    }

    private boolean isFirstZone()
    {
      return this.index == 0;
    }

    @Override
    public NTPresetType preset()
    {
      return this.preset;
    }

    @Override
    public boolean isGlobal()
    {
      return this.isFirstZone() && this.lastGeneratorIsNotInstrument();
    }
  }

  private static final class Preset implements NTPresetType
  {
    private final NTPresetName name;
    private final NTFontType font;
    private final int index;
    private final List<NTPresetZoneType> zones_read;
    private final List<PresetZone> zones;

    private Preset(
      final NTFontType in_font,
      final int preset_index,
      final NTPresetName in_name)
    {
      Preconditions.checkPreconditionI(
        preset_index,
        preset_index >= 0,
        i -> "Index must be non-negative");

      this.font = Objects.requireNonNull(in_font, "font");
      this.index = preset_index;
      this.name = Objects.requireNonNull(in_name, "name");
      this.zones = new ArrayList<>();
      this.zones_read = Collections.unmodifiableList(this.zones);
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[Preset ")
        .append(this.index)
        .append('\'')
        .append(this.name.value())
        .append('\'')
        .append("']")
        .toString();
    }

    @Override
    public NTPresetName name()
    {
      return this.name;
    }

    @Override
    public List<NTPresetZoneType> zones()
    {
      return this.zones_read;
    }

    @Override
    public NTFontType font()
    {
      return this.font;
    }

    @Override
    public String nameText()
    {
      return this.name.value();
    }
  }

  private static final class PresetZoneGenerator implements NTPresetZoneGeneratorType
  {
    private final PresetZone zone;
    private final NTGenerator generator;
    private final NTGenericAmount amount;

    private PresetZoneGenerator(
      final PresetZone in_zone,
      final NTGenerator in_generator,
      final NTGenericAmount in_amount)
    {
      this.zone = Objects.requireNonNull(in_zone, "zone");
      this.generator = Objects.requireNonNull(in_generator, "generator");
      this.amount = Objects.requireNonNull(in_amount, "amount");
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[PresetZoneGenerator ")
        .append(this.generator)
        .append(' ')
        .append(this.amount)
        .append(' ')
        .append(this.zone)
        .append(']')
        .toString();
    }

    @Override
    public NTPresetZoneType zone()
    {
      return this.zone;
    }

    @Override
    public NTGenerator generatorOperator()
    {
      return this.generator;
    }

    @Override
    public NTGenericAmount amount()
    {
      return this.amount;
    }
  }

  private static final class PresetZoneModulator implements NTPresetZoneModulatorType
  {
    private final PresetZone zone;

    private PresetZoneModulator(
      final PresetZone in_zone)
    {
      this.zone = Objects.requireNonNull(in_zone, "zone");
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[PresetZoneModulator ")
        .append(']')
        .toString();
    }

    @Override
    public NTPresetZoneType zone()
    {
      return this.zone;
    }
  }


  private static final class Instrument implements NTInstrumentType
  {
    private final Font font;
    private final NTInstrumentName name;
    private final List<NTInstrumentZoneType> zones_read;
    private final List<InstrumentZone> zones;
    private final int index;

    private Instrument(
      final Font in_font,
      final int in_index,
      final NTInstrumentName in_name)
    {
      Preconditions.checkPreconditionI(in_index, in_index >= 0, i -> "Index must be non-negative");

      this.font = Objects.requireNonNull(in_font, "font");
      this.index = in_index;
      this.name = Objects.requireNonNull(in_name, "name");
      this.zones = new ArrayList<>();
      this.zones_read = Collections.unmodifiableList(this.zones);
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[Instrument ")
        .append(this.index)
        .append('\'')
        .append(this.name.value())
        .append('\'')
        .append("']")
        .toString();
    }

    @Override
    public NTFontType font()
    {
      return this.font;
    }

    @Override
    public NTInstrumentName name()
    {
      return this.name;
    }

    @Override
    public List<NTInstrumentZoneType> zones()
    {
      return this.zones_read;
    }

    @Override
    public String nameText()
    {
      return this.name().value();
    }
  }

  private static final class Font implements NTFontType
  {
    private final NTInfo info;
    private final List<Instrument> instruments;
    private final List<NTInstrumentType> instruments_read;
    private final List<NTPresetType> presets;
    private final List<NTPresetType> presets_read;
    private final List<Sample> samples;
    private final List<NTSampleType> samples_read;

    private Font(final NTInfo in_info)
    {
      this.info = Objects.requireNonNull(in_info, "info");
      this.instruments = new ArrayList<>();
      this.presets = new ArrayList<>();
      this.samples = new ArrayList<>();

      this.instruments_read =
        Collections.unmodifiableList(this.instruments);
      this.presets_read =
        Collections.unmodifiableList(this.presets);
      this.samples_read =
        Collections.unmodifiableList(this.samples);
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[Font '")
        .append(this.info.name().value())
        .append("']")
        .toString();
    }

    @Override
    public NTInfo info()
    {
      return this.info;
    }

    @Override
    public List<NTInstrumentType> instruments()
    {
      return this.instruments_read;
    }

    @Override
    public List<NTPresetType> presets()
    {
      return this.presets_read;
    }

    @Override
    public List<NTSampleType> samples()
    {
      return this.samples_read;
    }
  }

  private static final class Sample implements NTSampleType
  {
    private final Font font;
    private final NTParsedSample sample;

    private Sample(
      final Font in_font,
      final NTParsedSample in_sample)
    {
      this.font = Objects.requireNonNull(in_font, "font");
      this.sample = Objects.requireNonNull(in_sample, "sample");
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[Sample '")
        .append(this.sample.name().value())
        .append("' ")
        .append(this.sample.kind())
        .append(']')
        .toString();
    }

    @Override
    public NTFontType font()
    {
      return this.font;
    }

    @Override
    public String nameText()
    {
      return this.name().value();
    }

    @Override
    public long start()
    {
      return this.sample.start();
    }

    @Override
    public long end()
    {
      return this.sample.end();
    }

    @Override
    public long loopStart()
    {
      return this.sample.loopStart();
    }

    @Override
    public long loopEnd()
    {
      return this.sample.loopEnd();
    }

    @Override
    public int sampleRate()
    {
      return this.sample.sampleRate();
    }

    @Override
    public int originalPitch()
    {
      return this.sample.originalPitch();
    }

    @Override
    public int pitchCorrection()
    {
      return this.sample.pitchCorrection();
    }

    @Override
    public int sampleLink()
    {
      return this.sample.sampleLink();
    }

    @Override
    public NTSampleName name()
    {
      return this.sample.name();
    }

    @Override
    public NTSampleKind kind()
    {
      return this.sample.kind();
    }
  }

  private static final class InstrumentZoneGenerator implements NTInstrumentZoneGeneratorType
  {
    private final InstrumentZone zone;
    private final NTGenerator generator;
    private final NTGenericAmount amount;

    private InstrumentZoneGenerator(
      final InstrumentZone in_zone,
      final NTGenerator in_generator,
      final NTGenericAmount in_amount)
    {
      this.zone = Objects.requireNonNull(in_zone, "zone");
      this.generator = Objects.requireNonNull(in_generator, "generator");
      this.amount = Objects.requireNonNull(in_amount, "amount");
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[InstrumentZoneGenerator ")
        .append(this.generator)
        .append(' ')
        .append(this.amount)
        .append(' ')
        .append(this.zone)
        .append(']')
        .toString();
    }

    @Override
    public NTInstrumentZoneType zone()
    {
      return this.zone;
    }

    @Override
    public NTGenerator generatorOperator()
    {
      return this.generator;
    }

    @Override
    public NTGenericAmount amount()
    {
      return this.amount;
    }
  }

  private static final class InstrumentZoneModulator implements NTInstrumentZoneModulatorType
  {
    private final InstrumentZone zone;

    private InstrumentZoneModulator(
      final InstrumentZone in_zone)
    {
      this.zone = Objects.requireNonNull(in_zone, "zone");
    }

    @Override
    public String toString()
    {
      return new StringBuilder(64)
        .append("[InstrumentZoneModulator ")
        .append(']')
        .toString();
    }

    @Override
    public NTInstrumentZoneType zone()
    {
      return this.zone;
    }
  }
}
