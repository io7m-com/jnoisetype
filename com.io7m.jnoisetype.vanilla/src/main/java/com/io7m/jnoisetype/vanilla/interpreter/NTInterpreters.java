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

package com.io7m.jnoisetype.vanilla.interpreter;

import com.io7m.jnoisetype.api.NTBankIndex;
import com.io7m.jnoisetype.api.NTFontType;
import com.io7m.jnoisetype.api.NTGenerators;
import com.io7m.jnoisetype.api.NTGenericAmount;
import com.io7m.jnoisetype.api.NTInstrumentIndex;
import com.io7m.jnoisetype.api.NTNamedType;
import com.io7m.jnoisetype.api.NTPresetIndex;
import com.io7m.jnoisetype.api.NTPresetType;
import com.io7m.jnoisetype.api.NTRanges;
import com.io7m.jnoisetype.api.NTSource;
import com.io7m.jnoisetype.api.NTTransforms;
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
import com.io7m.jnoisetype.vanilla.NTInvariants;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeCheckException;
import com.io7m.jranges.RangeHalfOpenI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;

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

  private static boolean isNamedTerminalRecord(
    final int index,
    final int count,
    final NTNamedType named,
    final String name)
  {
    return index + 1 == count && Objects.equals(named.nameText(), name);
  }

  private static final class Interpreter implements NTInterpreterType
  {
    private final NTParsedFile file;

    private Interpreter(
      final NTParsedFile in_file)
    {
      this.file = Objects.requireNonNull(in_file, "file");
    }

    private static NTISample interpretSample(
      final NTIFont font,
      final int sample_index,
      final NTParsedSample sample)
    {
      if (LOG.isTraceEnabled()) {
        final var description = sample.description();
        LOG.trace(
          "sample [{}][\"{}\"] {}",
          Integer.valueOf(sample_index),
          description.name().value(),
          description.kind());
      }

      return new NTISample(font, sample, sample.dataByteRange());
    }

    private static RangeHalfOpenI makeRange(
      final String container_name,
      final String container,
      final String range_name,
      final IntSupplier curr,
      final IntSupplier next,
      final NTSource source)
      throws NTParseException
    {
      final RangeHalfOpenI mod_range;
      final var range_lo = curr.getAsInt();
      final var range_hi = next.getAsInt();

      try {
        RangeCheck.checkIncludedInInteger(
          range_lo,
          range_name,
          NTRanges.UNSIGNED_16_RANGE,
          "Valid " + range_name);
        RangeCheck.checkIncludedInInteger(
          range_hi,
          range_name,
          NTRanges.UNSIGNED_16_RANGE,
          "Valid " + range_name);

        mod_range = RangeHalfOpenI.of(range_lo, range_hi);
      } catch (final RangeCheckException e) {
        final var separator = System.lineSeparator();
        throw new NTParseException(
          new StringBuilder(64)
            .append("Corrupt ")
            .append(container_name.toLowerCase())
            .append(": ")
            .append(range_name)
            .append(" range is malformed")
            .append(separator)

            .append("  ")
            .append(container_name)
            .append(": ")
            .append(container)
            .append(separator)

            .append("  ")
            .append(range_name)
            .append(" lower index: ")
            .append(range_lo)
            .append(separator)

            .append("  ")
            .append(range_name)
            .append(" lower index: ")
            .append(range_hi)
            .append(separator)
            .toString(),
          e,
          source.source(),
          source.offset());
      }
      return mod_range;
    }

    private static NTIInstrumentZoneGenerator interpretInstrumentZoneGenerator(
      final NTIInstrumentZone zone,
      final NTParsedInstrumentZoneGenerator gen_curr)
    {
      final var named_generator =
        NTGenerators.find(gen_curr.generatorOperator());
      final var amount =
        NTGenericAmount.of(gen_curr.amount().value());

      return new NTIInstrumentZoneGenerator(zone, named_generator, amount);
    }

    private static void checkPresetZoneIndex(
      final int preset_index,
      final List<NTParsedPresetZone> pbag,
      final NTSource pbag_source,
      final int index)
      throws NTParseException
    {
      checkIndex(
        "preset zone",
        Integer.toUnsignedString(preset_index),
        "Zone",
        pbag.size(),
        pbag_source,
        index);
    }

    private static void checkInstrumentZoneIndex(
      final int instrument_index,
      final List<NTParsedInstrumentZone> ibag,
      final NTSource ibag_source,
      final int index)
      throws NTParseException
    {
      checkIndex(
        "instrument zone",
        Integer.toUnsignedString(instrument_index),
        "Zone",
        ibag.size(),
        ibag_source,
        index);
    }

    private static void checkIndex(
      final String container_name,
      final String container_index,
      final String name,
      final int upper_bound,
      final NTSource source,
      final int index)
      throws NTParseException
    {
      if (index >= upper_bound) {
        final var separator = System.lineSeparator();
        throw new NTParseException(
          new StringBuilder(64)
            .append("Corrupted ")
            .append(container_name)
            .append(": ")
            .append(name)
            .append(" out of range")
            .append(separator)
            .append("  ")
            .append(container_name)
            .append(": ")
            .append(container_index)
            .append(separator)
            .append("  ")
            .append(name)
            .append(": ")
            .append(index)
            .append(separator)
            .append("  Allowed range: [0, ")
            .append(upper_bound)
            .append(")")
            .append(separator)
            .toString(),
          source.source(),
          source.offset());
      }
    }

    private static void checkPresetZoneModulatorIndex(
      final NTIPreset preset,
      final List<NTParsedPresetZoneModulator> pmod,
      final NTSource pmod_source,
      final int mod_index)
      throws NTParseException
    {
      checkIndex(
        "preset zone",
        preset.nameText(),
        "Modulator index",
        pmod.size(),
        pmod_source,
        mod_index);
    }

    private static void checkPresetZoneGeneratorIndex(
      final NTIPreset preset,
      final List<NTParsedPresetZoneGenerator> pgen,
      final NTSource pgen_source,
      final int gen_index)
      throws NTParseException
    {
      checkIndex(
        "preset zone",
        preset.nameText(),
        "Generator index",
        pgen.size(),
        pgen_source,
        gen_index);
    }

    private static void checkInstrumentZoneModulatorIndex(
      final NTIIInstrument instrument,
      final List<NTParsedInstrumentZoneModulator> imod,
      final NTSource imod_source,
      final int mod_index)
      throws NTParseException
    {
      checkIndex(
        "instrument zone",
        instrument.nameText(),
        "Modulator index",
        imod.size(),
        imod_source,
        mod_index);
    }

    private static void checkInstrumentZoneGeneratorIndex(
      final NTIIInstrument instrument,
      final List<NTParsedInstrumentZoneGenerator> igen,
      final NTSource igen_source,
      final int gen_index)
      throws NTParseException
    {
      checkIndex(
        "instrument zone",
        instrument.nameText(),
        "Generator index",
        igen.size(),
        igen_source,
        gen_index);
    }

    private static RangeHalfOpenI makePresetZoneModulatorRange(
      final NTIPreset preset,
      final NTParsedPresetZone zone_curr,
      final NTParsedPresetZone zone_next,
      final NTSource pmod_source)
      throws NTParseException
    {
      return makeRange(
        "Preset zone",
        preset.nameText(),
        "Modulator",
        zone_curr::modulatorIndex,
        zone_next::modulatorIndex,
        pmod_source);
    }

    private static RangeHalfOpenI makePresetZoneGeneratorRange(
      final NTIPreset preset,
      final NTParsedPresetZone zone_curr,
      final NTParsedPresetZone zone_next,
      final NTSource pgen_source)
      throws NTParseException
    {
      return makeRange(
        "Preset zone",
        preset.nameText(),
        "Generator",
        zone_curr::generatorIndex,
        zone_next::generatorIndex,
        pgen_source);
    }

    private static RangeHalfOpenI makeInstrumentZoneModulatorRange(
      final NTIIInstrument preset,
      final NTParsedInstrumentZone zone_curr,
      final NTParsedInstrumentZone zone_next,
      final NTSource pmod_source)
      throws NTParseException
    {
      return makeRange(
        "Instrument zone",
        preset.nameText(),
        "Modulator",
        zone_curr::modulatorIndex,
        zone_next::modulatorIndex,
        pmod_source);
    }

    private static RangeHalfOpenI makeInstrumentZoneGeneratorRange(
      final NTIIInstrument preset,
      final NTParsedInstrumentZone zone_curr,
      final NTParsedInstrumentZone zone_next,
      final NTSource pgen_source)
      throws NTParseException
    {
      return makeRange(
        "Instrument zone",
        preset.nameText(),
        "Generator",
        zone_curr::generatorIndex,
        zone_next::generatorIndex,
        pgen_source);
    }

    @Override
    public NTFontType interpret()
      throws NTParseException
    {
      final var font = new NTIFont(this.file.info());
      this.interpretSamples(font);
      this.interpretInstruments(font);
      this.interpretPresets(font);
      return font;
    }

    private void interpretPresets(final NTIFont font)
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
        final var input_curr = phdr.get(preset_index);
        if (isNamedTerminalRecord(
          preset_index,
          phdr.size(),
          input_curr,
          "EOP")) {
          break;
        }

        final var input_next = phdr.get(preset_index + 1);
        font.addPreset(this.interpretPreset(
          font,
          preset_index,
          input_curr,
          input_next));
      }
    }

    private NTPresetType interpretPreset(
      final NTIFont font,
      final int preset_index,
      final NTParsedPreset input_preset_curr,
      final NTParsedPreset input_preset_next)
      throws NTParseException
    {
      final var bankIndex =
        NTBankIndex.of((int) (char) input_preset_curr.bank());
      final NTPresetIndex presetIndex =
        NTPresetIndex.of(preset_index);

      final var preset =
        new NTIPreset(
          font,
          bankIndex,
          presetIndex,
          input_preset_curr.name());

      final var pbag = this.file.pbag();
      final var pbag_source = this.file.presetZoneRecordsSource();

      final var zone_range =
        makeRange(
          "Preset",
          preset.nameText(),
          "Zone",
          input_preset_curr::presetBagIndex,
          input_preset_next::presetBagIndex,
          pbag_source);

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "preset [{}][\"{}\"] zone range [{}, {}) ({} zones)",
          Integer.valueOf(preset_index),
          input_preset_curr.name().value(),
          Integer.valueOf(zone_range.lower()),
          Integer.valueOf(zone_range.upper()),
          Integer.valueOf(zone_range.interval()));
      }

      NTInvariants.checkUnnamedTerminalRecordExists(pbag, "7.7", pbag_source);

      if (zone_range.interval() < 2) {
        return preset;
      }

      for (var zone_index = zone_range.lower(); zone_index < zone_range.upper(); ++zone_index) {
        checkPresetZoneIndex(preset_index, pbag, pbag_source, zone_index);
        final var zone_curr = pbag.get(zone_index);
        checkPresetZoneIndex(preset_index, pbag, pbag_source, zone_index + 1);
        final var zone_next = pbag.get(zone_index + 1);

        preset.addZone(
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

    private NTIPresetZone interpretPresetZone(
      final int preset_index,
      final NTIPreset preset,
      final int zone_index,
      final int zone_lower,
      final NTParsedPresetZone zone_curr,
      final NTParsedPresetZone zone_next)
      throws NTParseException
    {
      final var pgen = this.file.pgen();
      final var pmod = this.file.pmod();

      final var pgen_source = this.file.presetZoneGeneratorRecordsSource();
      NTInvariants.checkUnnamedTerminalRecordExists(pgen, "7.5", pgen_source);
      final var pmod_source = this.file.presetZoneModulatorRecordsSource();
      NTInvariants.checkUnnamedTerminalRecordExists(pmod, "7.4", pmod_source);

      final var zone =
        new NTIPresetZone(preset, zone_index - zone_lower);
      final var gen_range =
        makePresetZoneGeneratorRange(preset, zone_curr, zone_next, pgen_source);
      final var mod_range =
        makePresetZoneModulatorRange(preset, zone_curr, zone_next, pmod_source);

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

      for (var gen_index = gen_range.lower(); gen_index < gen_range.upper(); ++gen_index) {
        checkPresetZoneGeneratorIndex(preset, pgen, pgen_source, gen_index);
        zone.addGenerator(interpretPresetZoneGenerator(
          zone,
          pgen.get(gen_index)));
      }

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

      for (var mod_index = mod_range.lower(); mod_index < mod_range.upper(); ++mod_index) {
        checkPresetZoneModulatorIndex(preset, pmod, pmod_source, mod_index);
        zone.addModulator(interpretPresetZoneModulator(
          zone,
          pmod.get(mod_index)));
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

    private static NTIPresetZoneModulator interpretPresetZoneModulator(
      final NTIPresetZone zone,
      final NTParsedPresetZoneModulator mod_curr)
    {
      final var named_generator =
        NTGenerators.find(mod_curr.targetOperator());
      final var named_transform =
        NTTransforms.find(mod_curr.modulationTransformOperator());

      return new NTIPresetZoneModulator(
        zone,
        mod_curr.sourceOperator(),
        named_generator,
        mod_curr.modulationAmount(),
        mod_curr.modulationAmountSourceOperator(),
        named_transform);
    }

    private static NTIPresetZoneGenerator interpretPresetZoneGenerator(
      final NTIPresetZone zone,
      final NTParsedPresetZoneGenerator gen_curr)
    {
      final var named_generator =
        NTGenerators.find(gen_curr.generatorOperator());
      final var amount =
        NTGenericAmount.of(gen_curr.amount().value());

      return new NTIPresetZoneGenerator(zone, named_generator, amount);
    }

    private void interpretInstruments(
      final NTIFont font)
      throws NTParseException
    {
      final var inst = this.file.inst();

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "interpreting {} instrument records (including terminal record)",
          Integer.valueOf(inst.size()));
      }

      NTInvariants.checkNamedTerminalRecordExists(
        inst,
        "EOI",
        "7.6",
        this.file.instrumentRecordsSource());

      for (var index = 0; index < inst.size(); ++index) {
        final var input_curr = inst.get(index);
        if (isNamedTerminalRecord(index, inst.size(), input_curr, "EOI")) {
          break;
        }

        final var input_next = inst.get(index + 1);
        font.addInstrument(this.interpretInstrument(
          font,
          index,
          input_curr,
          input_next));
      }
    }

    private NTIIInstrument interpretInstrument(
      final NTIFont font,
      final int instrument_index,
      final NTParsedInstrument input_instrument_curr,
      final NTParsedInstrument input_instrument_next)
      throws NTParseException
    {
      final var instrument =
        new NTIIInstrument(
          font,
          NTInstrumentIndex.of(instrument_index),
          input_instrument_curr.name());

      final var ibag = this.file.ibag();
      final var ibag_source = this.file.instrumentZoneRecordsSource();

      final var zone_range =
        makeRange(
          "Instrument",
          instrument.nameText(),
          "Zone",
          input_instrument_curr::instrumentZoneIndex,
          input_instrument_next::instrumentZoneIndex,
          ibag_source);

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "instrument [{}][\"{}\"] zone range [{}, {}) ({} zones)",
          Integer.valueOf(instrument_index),
          input_instrument_curr.name().value(),
          Integer.valueOf(zone_range.lower()),
          Integer.valueOf(zone_range.upper()),
          Integer.valueOf(zone_range.interval()));
      }

      NTInvariants.checkUnnamedTerminalRecordExists(ibag, "7.7", ibag_source);

      if (zone_range.interval() < 2) {
        return instrument;
      }

      for (var zone_index = zone_range.lower(); zone_index < zone_range.upper(); ++zone_index) {
        checkInstrumentZoneIndex(
          instrument_index,
          ibag,
          ibag_source,
          zone_index);
        final var zone_curr = ibag.get(zone_index);
        checkInstrumentZoneIndex(
          instrument_index,
          ibag,
          ibag_source,
          zone_index + 1);
        final var zone_next = ibag.get(zone_index + 1);

        instrument.addZone(
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

    private NTIInstrumentZone interpretInstrumentZone(
      final int instrument_index,
      final NTIIInstrument instrument,
      final int zone_index,
      final int zone_lower,
      final NTParsedInstrumentZone zone_curr,
      final NTParsedInstrumentZone zone_next)
      throws NTParseException
    {
      final var igen = this.file.igen();
      final var imod = this.file.imod();

      final var igen_source = this.file.instrumentZoneGeneratorRecordsSource();
      NTInvariants.checkUnnamedTerminalRecordExists(igen, "7.9", igen_source);
      final var imod_source = this.file.instrumentZoneModulatorRecordsSource();
      NTInvariants.checkUnnamedTerminalRecordExists(imod, "7.8", imod_source);

      final var zone =
        new NTIInstrumentZone(instrument, zone_index - zone_lower);
      final var gen_range =
        makeInstrumentZoneGeneratorRange(
          instrument,
          zone_curr,
          zone_next,
          igen_source);
      final var mod_range =
        makeInstrumentZoneModulatorRange(
          instrument,
          zone_curr,
          zone_next,
          imod_source);

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "instrument [{}][\"{}\"] zone [{}] generator range [{}, {}) ({} generators)",
          Integer.valueOf(instrument_index),
          instrument.name().value(),
          Integer.valueOf(zone_index),
          Integer.valueOf(gen_range.lower()),
          Integer.valueOf(gen_range.upper()),
          Integer.valueOf(gen_range.interval()));
      }

      for (var gen_index = gen_range.lower(); gen_index < gen_range.upper(); ++gen_index) {
        checkInstrumentZoneGeneratorIndex(
          instrument,
          igen,
          igen_source,
          gen_index);
        zone.addGenerator(interpretInstrumentZoneGenerator(
          zone,
          igen.get(gen_index)));
      }

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "instrument [{}][\"{}\"] zone [{}] modulator range [{}, {}) ({} modulators)",
          Integer.valueOf(instrument_index),
          instrument.name().value(),
          Integer.valueOf(zone_index),
          Integer.valueOf(mod_range.lower()),
          Integer.valueOf(mod_range.upper()),
          Integer.valueOf(mod_range.interval()));
      }

      for (var mod_index = mod_range.lower(); mod_index < mod_range.upper(); ++mod_index) {
        checkInstrumentZoneModulatorIndex(
          instrument,
          imod,
          imod_source,
          mod_index);
        zone.addModulator(this.interpretInstrumentZoneModulator(
          zone,
          imod.get(mod_index)));
      }

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "instrument [{}][\"{}\"] zone [{}] global {}",
          Integer.valueOf(instrument_index),
          instrument.name().value(),
          Integer.valueOf(zone_index),
          Boolean.valueOf(zone.isGlobal()));
      }

      return zone;
    }

    private NTIInstrumentZoneModulator interpretInstrumentZoneModulator(
      final NTIInstrumentZone zone,
      final NTParsedInstrumentZoneModulator mod_curr)
    {
      final var named_generator =
        NTGenerators.find(mod_curr.targetOperator());
      final var named_transform =
        NTTransforms.find(mod_curr.modulationTransformOperator());

      return new NTIInstrumentZoneModulator(
        zone,
        mod_curr.sourceOperator(),
        named_generator,
        mod_curr.modulationAmount(),
        mod_curr.modulationAmountSourceOperator(),
        named_transform);
    }

    private void interpretSamples(
      final NTIFont font)
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
        if (isNamedTerminalRecord(
          sample_index,
          samples.size(),
          input_sample,
          "EOS")) {
          break;
        }

        font.addSample(interpretSample(font, sample_index, input_sample));
      }
    }
  }

}
