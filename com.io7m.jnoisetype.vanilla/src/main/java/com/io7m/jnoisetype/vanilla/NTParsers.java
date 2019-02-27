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

import com.io7m.jnoisetype.api.NTInfo;
import com.io7m.jnoisetype.api.NTInstrumentName;
import com.io7m.jnoisetype.api.NTLongString;
import com.io7m.jnoisetype.api.NTPresetName;
import com.io7m.jnoisetype.api.NTRanges;
import com.io7m.jnoisetype.api.NTSampleDescription;
import com.io7m.jnoisetype.api.NTSampleKind;
import com.io7m.jnoisetype.api.NTSampleName;
import com.io7m.jnoisetype.api.NTShortString;
import com.io7m.jnoisetype.api.NTSource;
import com.io7m.jnoisetype.api.NTVersion;
import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.parser.api.NTFileParserType;
import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.parser.api.NTParsedFile;
import com.io7m.jnoisetype.parser.api.NTParsedGenericAmount;
import com.io7m.jnoisetype.parser.api.NTParsedInstrument;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZone;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZoneGenerator;
import com.io7m.jnoisetype.parser.api.NTParsedInstrumentZoneModulator;
import com.io7m.jnoisetype.parser.api.NTParsedPreset;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZone;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZoneGenerator;
import com.io7m.jnoisetype.parser.api.NTParsedPresetZoneModulator;
import com.io7m.jnoisetype.parser.api.NTParsedSample;
import com.io7m.jspiel.api.RiffChunkType;
import com.io7m.jspiel.api.RiffFileParserProviderType;
import com.io7m.jspiel.api.RiffFileParserType;
import com.io7m.jspiel.api.RiffParseException;
import com.io7m.jspiel.api.RiffRequiredChunkMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * The default parser provider.
 */

public final class NTParsers implements NTFileParserProviderType
{
  private static final Logger LOG = LoggerFactory.getLogger(NTParsers.class);

  private final RiffFileParserProviderType parsers;

  private NTParsers(
    final RiffFileParserProviderType in_parsers)
  {
    this.parsers = Objects.requireNonNull(in_parsers, "parsers");
  }

  /**
   * Create a new parser provider.
   *
   * @param parsers The RIFF file parser provider
   *
   * @return A new parser provider
   */

  public static NTFileParserProviderType create(
    final RiffFileParserProviderType parsers)
  {
    return new NTParsers(parsers);
  }

  @Override
  public NTFileParserType createForByteBuffer(
    final URI source,
    final ByteBuffer data)
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(data, "data");

    return new Parser(this.parsers.createForByteBuffer(source, data), source, data);
  }

  private static final class SourceAndData<T>
  {
    private final NTSource source;
    private final T data;

    private SourceAndData(
      final NTSource in_source,
      final T in_data)
    {
      this.source = Objects.requireNonNull(in_source, "source");
      this.data = Objects.requireNonNull(in_data, "data");
    }
  }

  private static final class Parser implements NTFileParserType
  {
    private final RiffFileParserType parser;
    private final URI source;
    private final ByteBuffer data;

    Parser(
      final RiffFileParserType in_parser,
      final URI in_source,
      final ByteBuffer in_data)
    {
      this.parser =
        Objects.requireNonNull(in_parser, "parser");
      this.source =
        Objects.requireNonNull(in_source, "source");
      this.data =
        Objects.requireNonNull(in_data, "data");
    }

    private static void describeChunk(
      final RiffChunkType chunk,
      final StringBuilder message)
    {
      final var separator = System.lineSeparator();
      message.append("  Chunk name: ")
        .append(chunk.name().value())
        .append(separator)
        .append("  Chunk offset: 0x")
        .append(Long.toUnsignedString(chunk.offset(), 16))
        .append(separator)
        .append("  Chunk data size: ")
        .append(Long.toUnsignedString(chunk.dataSizeIncludingForm().size(), 10))
        .append(separator);
    }

    private static ByteBuffer makeChunkDataView(
      final ByteBuffer data,
      final RiffChunkType chunk)
    {
      final var view = data.duplicate();
      view.position(Math.toIntExact(chunk.dataOffset()));
      view.limit(Math.toIntExact(
        Math.addExact(
          chunk.dataOffset(),
          chunk.dataSizeIncludingForm().size())));
      view.order(data.order());
      return view;
    }

    private static String newString(
      final byte[] name,
      final int offset_null)
    {
      // CHECKSTYLE:OFF
      return new String(name, 0, offset_null, US_ASCII);
      // CHECKSTYLE:ON
    }

    private static NTSampleName readSampleName(
      final ByteBuffer view)
    {
      final var name = new byte[NTRanges.SAMPLE_NAME_LENGTH_RANGE.upper()];
      view.get(name);

      final var offset_null = findNull(name);
      return NTSampleName.of(newString(name, offset_null));
    }

    private static NTPresetName readPresetName(
      final ByteBuffer view)
    {
      final var name = new byte[NTRanges.PRESET_NAME_LENGTH_RANGE.upper()];
      view.get(name);

      final var offset_null = findNull(name);
      return NTPresetName.of(newString(name, offset_null));
    }

    private static NTInstrumentName readInstrumentName(
      final ByteBuffer view)
    {
      final var name = new byte[NTRanges.INSTRUMENT_NAME_LENGTH_RANGE.upper()];
      view.get(name);

      final var offset_null = findNull(name);
      return NTInstrumentName.of(newString(name, offset_null));
    }

    private static int findNull(final byte[] name)
    {
      var offset_null = 0;
      for (var index = 0; index < name.length; ++index) {
        if (name[index] == 0) {
          offset_null = index;
          break;
        }
      }
      return offset_null;
    }

    private static boolean codePointIsNotNull(
      final int code)
    {
      return code != 0;
    }

    @Override
    public NTParsedFile parse()
      throws NTParseException
    {
      try {
        final var file = this.parser.parse();
        final var root = file.chunks().get(0);

        this.checkFormType(root);

        final var info_list =
          root.findRequiredSubChunkWithForm("LIST", "INFO");
        final var pdta_list =
          root.findRequiredSubChunkWithForm("LIST", "pdta");
        final var sdta_list =
          root.findRequiredSubChunkWithForm("LIST", "sdta");

        final var builder = NTParsedFile.builder();
        this.parsePData(pdta_list, builder);

        return builder
          .setInfo(this.parseInfo(info_list))
          .build();
      } catch (final RiffParseException e) {
        throw new NTParseException(e, e.source(), e.offset());
      } catch (final RiffRequiredChunkMissingException e) {
        throw new NTParseException(e, this.source, 0L);
      }
    }

    private RiffChunkType requireChunk(
      final RiffChunkType owner,
      final String spec_section,
      final String name)
      throws NTParseException
    {
      try {
        return owner.findRequiredSubChunk(name);
      } catch (final RiffRequiredChunkMissingException e) {
        final var separator = System.lineSeparator();
        throw new NTParseException(
          new StringBuilder(128)
            .append("Missing a required chunk.")
            .append(separator)
            .append("  Chunk name: ")
            .append(name)
            .append(separator)
            .append("  Chunk specification: SoundFontⓡ Technical Specification 2.04, §")
            .append(spec_section)
            .append(separator)
            .append("  Owner chunk: ")
            .append(owner.name().value())
            .append(separator)
            .append("  Owner chunk offset: 0x")
            .append(Long.toUnsignedString(owner.offset(), 16))
            .append(separator)
            .toString(),
          e,
          this.source,
          owner.offset());
      }
    }

    private RiffChunkType requireChunkIsDivisible(
      final RiffChunkType chunk,
      final String spec_section,
      final long divisor)
      throws NTParseException
    {
      if (chunk.dataSizeIncludingForm().sizeUnpadded() % divisor != 0L) {
        final var separator = System.lineSeparator();
        throw new NTParseException(
          new StringBuilder(128)
            .append("Chunk data size is not exactly divisible by a required amount.")
            .append(separator)
            .append("  Chunk name: ")
            .append(chunk.name().value())
            .append(separator)
            .append("  Chunk specification: SoundFontⓡ Technical Specification 2.04, §")
            .append(spec_section)
            .append(separator)
            .append("  Chunk offset: 0x")
            .append(Long.toUnsignedString(chunk.offset(), 16))
            .append(separator)
            .append("  Chunk data size: ")
            .append(Long.toUnsignedString(chunk.dataSizeIncludingForm().size(), 10))
            .append(separator)
            .append("  Chunk required divisor: ")
            .append(Long.toUnsignedString(divisor, 10))
            .append(separator)
            .toString(),
          this.source,
          chunk.offset());
      }
      return chunk;
    }

    private void parsePData(
      final RiffChunkType pdta_list,
      final NTParsedFile.Builder builder)
      throws NTParseException
    {
      final var preset_records = this.parsePDataPHDR(pdta_list);
      builder.setPresetRecords(preset_records.data);
      builder.setPresetRecordsSource(preset_records.source);

      final var preset_zone_records = this.parsePDataPBAG(pdta_list);
      builder.setPresetZoneRecords(preset_zone_records.data);
      builder.setPresetZoneRecordsSource(preset_zone_records.source);

      final var preset_zone_mod_records = this.parsePDataPMOD(pdta_list);
      builder.setPresetZoneModulatorRecords(preset_zone_mod_records.data);
      builder.setPresetZoneModulatorRecordsSource(preset_zone_mod_records.source);

      final var preset_zone_gen_records = this.parsePDataPGEN(pdta_list);
      builder.setPresetZoneGeneratorRecords(preset_zone_gen_records.data);
      builder.setPresetZoneGeneratorRecordsSource(preset_zone_gen_records.source);

      final var instrument_records = this.parsePDataINST(pdta_list);
      builder.setInstrumentRecords(instrument_records.data);
      builder.setInstrumentRecordsSource(instrument_records.source);

      final var instrument_zone_records = this.parsePDataIBAG(pdta_list);
      builder.setInstrumentZoneRecords(instrument_zone_records.data);
      builder.setInstrumentZoneRecordsSource(instrument_zone_records.source);

      final var instrument_zone_mod_records = this.parsePDataIMOD(pdta_list);
      builder.setInstrumentZoneModulatorRecords(instrument_zone_mod_records.data);
      builder.setInstrumentZoneModulatorRecordsSource(instrument_zone_mod_records.source);

      final var instrument_zone_gen_records = this.parsePDataIGEN(pdta_list);
      builder.setInstrumentZoneGeneratorRecords(instrument_zone_gen_records.data);
      builder.setInstrumentZoneGeneratorRecordsSource(instrument_zone_gen_records.source);

      final var sample_records = this.parsePDataSHDR(pdta_list);
      builder.setSampleRecords(sample_records.data);
      builder.setSampleRecordsSource(sample_records.source);
    }

    private SourceAndData<ArrayList<NTParsedPreset>> parsePDataPHDR(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var phdr =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.2", "phdr"),
          "7.2",
          38L);

      final var view = makeChunkDataView(this.data, phdr);
      final var results = new ArrayList<NTParsedPreset>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 38; position += 38) {
        view.position(position);

        final var name = readPresetName(view);
        final var preset_index = view.getShort() % 0xffff;
        final var bank = view.getShort() % 0xffff;
        final var preset_bag_index = view.getShort() % 0xffff;
        final var library = view.getInt();
        final var genre = view.getInt();
        final var morphology = view.getInt();

        final var result =
          NTParsedPreset.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setName(name)
            .setBank(bank)
            .setPreset(preset_index)
            .setPresetBagIndex(preset_bag_index)
            .setLibrary(Integer.toUnsignedLong(library))
            .setGenre(Integer.toUnsignedLong(genre))
            .setMorphology(Integer.toUnsignedLong(morphology))
            .build();

        LOG.trace("[phdr][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkNamedTerminalRecordExists(
        results,
        "EOP",
        "7.2",
        NTSource.of(this.source, phdr.offset()),
        message -> describeChunk(phdr, message));

      return new SourceAndData<>(NTSource.of(this.source, phdr.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedPresetZone>> parsePDataPBAG(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var pbag =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.3", "pbag"),
          "7.3",
          4L);

      final var view = makeChunkDataView(this.data, pbag);
      final var results = new ArrayList<NTParsedPresetZone>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 4; position += 4) {
        view.position(position);

        final var generator_index = view.getShort() & 0xffff;
        final var modulator_index = view.getShort() & 0xffff;

        final var preset =
          NTParsedPresetZone.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setGeneratorIndex(generator_index)
            .setModulatorIndex(modulator_index)
            .build();

        LOG.trace("[pbag][{}] {}", Integer.valueOf(index), preset);
        results.add(preset);
        ++index;
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        results,
        "7.3",
        NTSource.of(this.source, pbag.offset()),
        message -> describeChunk(pbag, message));

      return new SourceAndData<>(NTSource.of(this.source, pbag.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedPresetZoneModulator>> parsePDataPMOD(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var pmod =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.4", "pmod"),
          "7.4",
          10L);

      final var view = makeChunkDataView(this.data, pmod);
      final var results = new ArrayList<NTParsedPresetZoneModulator>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 10; position += 10) {
        view.position(position);

        final var modulator_source_operator = view.getShort() & 0xffff;
        final var modulator_target_operator = view.getShort() & 0xffff;
        final var modulator_amount = view.getShort();
        final var modulator_amount_source_operator = view.getShort() & 0xffff;
        final var modulator_transform_operator = view.getShort() & 0xffff;

        final var result =
          NTParsedPresetZoneModulator.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setModulationAmountSourceOperator(modulator_amount_source_operator)
            .setModulationAmount(modulator_amount)
            .setModulationTransformOperator(modulator_transform_operator)
            .setSourceOperator(modulator_source_operator)
            .setTargetOperator(modulator_target_operator)
            .build();

        LOG.trace("[pmod][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        results,
        "7.4",
        NTSource.of(this.source, pmod.offset()),
        message -> describeChunk(pmod, message));

      return new SourceAndData<>(NTSource.of(this.source, pmod.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedPresetZoneGenerator>> parsePDataPGEN(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var pgen =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.5", "pgen"),
          "7.5",
          4L);

      final var view = makeChunkDataView(this.data, pgen);
      final var results = new ArrayList<NTParsedPresetZoneGenerator>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 4; position += 4) {
        view.position(position);

        final var operator = view.getShort() & 0xffff;
        final var amount = view.getShort() & 0xffff;

        final var result =
          NTParsedPresetZoneGenerator.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setAmount(NTParsedGenericAmount.of(amount))
            .setGeneratorOperator(operator)
            .build();

        LOG.trace("[pgen][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        results,
        "7.5",
        NTSource.of(this.source, pgen.offset()),
        message -> describeChunk(pgen, message));

      return new SourceAndData<>(NTSource.of(this.source, pgen.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedInstrument>> parsePDataINST(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var inst =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.6", "inst"),
          "7.6",
          22L);

      final var view = makeChunkDataView(this.data, inst);
      final var results = new ArrayList<NTParsedInstrument>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 22; position += 22) {
        view.position(position);

        final var name = readInstrumentName(view);
        final var instrument_index = view.getShort() & 0xffff;

        final var result =
          NTParsedInstrument.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setName(name)
            .setInstrumentZoneIndex(instrument_index)
            .build();

        LOG.trace("[inst][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkNamedTerminalRecordExists(
        results,
        "EOI",
        "7.6",
        NTSource.of(this.source, inst.offset()),
        message -> describeChunk(inst, message));

      return new SourceAndData<>(NTSource.of(this.source, inst.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedInstrumentZone>> parsePDataIBAG(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var ibag =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.7", "ibag"),
          "7.7",
          4L);

      final var view = makeChunkDataView(this.data, ibag);
      final var results = new ArrayList<NTParsedInstrumentZone>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 4; position += 4) {
        view.position(position);

        final var generator_index = view.getShort() & 0xffff;
        final var modulator_index = view.getShort() & 0xffff;

        final var result =
          NTParsedInstrumentZone.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setGeneratorIndex(generator_index)
            .setModulatorIndex(modulator_index)
            .build();

        LOG.trace("[ibag][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        results,
        "7.7",
        NTSource.of(this.source, ibag.offset()),
        message -> describeChunk(ibag, message));

      return new SourceAndData<>(NTSource.of(this.source, ibag.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedInstrumentZoneModulator>> parsePDataIMOD(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var imod =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.8", "imod"),
          "7.8",
          10L);

      final var view = makeChunkDataView(this.data, imod);
      final var results = new ArrayList<NTParsedInstrumentZoneModulator>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 10; position += 10) {
        view.position(position);

        final var modulator_source_operator = view.getShort() & 0xffff;
        final var modulator_target_operator = view.getShort() & 0xffff;
        final var modulator_amount = view.getShort();
        final var modulator_amount_source_operator = view.getShort() & 0xffff;
        final var modulator_transform_operator = view.getShort() & 0xffff;

        final var result =
          NTParsedInstrumentZoneModulator.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setModulationAmountSourceOperator(modulator_amount_source_operator)
            .setModulationAmount(modulator_amount)
            .setModulationTransformOperator(modulator_transform_operator)
            .setSourceOperator(modulator_source_operator)
            .setTargetOperator(modulator_target_operator)
            .build();

        LOG.trace("[imod][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        results,
        "7.8",
        NTSource.of(this.source, imod.offset()),
        message -> describeChunk(imod, message));

      return new SourceAndData<>(NTSource.of(this.source, imod.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedInstrumentZoneGenerator>> parsePDataIGEN(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var igen =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.9", "igen"),
          "7.9",
          4L);

      final var view = makeChunkDataView(this.data, igen);
      final var results = new ArrayList<NTParsedInstrumentZoneGenerator>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 4; position += 4) {
        view.position(position);

        final var operator = view.getShort() & 0xffff;
        final var amount = view.getShort() & 0xffff;

        final var result =
          NTParsedInstrumentZoneGenerator.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setAmount(NTParsedGenericAmount.of(amount))
            .setGeneratorOperator(operator)
            .build();

        LOG.trace("[igen][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkUnnamedTerminalRecordExists(
        results,
        "7.9",
        NTSource.of(this.source, igen.offset()),
        message -> describeChunk(igen, message));

      return new SourceAndData<>(NTSource.of(this.source, igen.offset()), results);
    }

    private SourceAndData<ArrayList<NTParsedSample>> parsePDataSHDR(
      final RiffChunkType pdta_list)
      throws NTParseException
    {
      final var shdr =
        this.requireChunkIsDivisible(
          this.requireChunk(pdta_list, "7.10", "shdr"),
          "7.10",
          46L);

      final var view = makeChunkDataView(this.data, shdr);
      final var results = new ArrayList<NTParsedSample>();

      var index = 0;
      for (var position = view.position(); view.remaining() >= 46; position += 46) {
        view.position(position);

        final var name = readSampleName(view);
        final var start = view.getInt();
        final var end = view.getInt();
        final var loop_start = view.getInt();
        final var loop_end = view.getInt();
        final var sample_rate = view.getInt();
        final var original_pitch = view.get() & 0xff;
        final var pitch_correct = view.get();
        final var sample_link = view.getShort() & 0xffff;
        final var sample_kind = view.getShort() & 0xffff;

        final var description =
          NTSampleDescription.builder()
            .setName(name)
            .setStart(Integer.toUnsignedLong(start))
            .setEnd(Integer.toUnsignedLong(end))
            .setLoopStart(Integer.toUnsignedLong(loop_start))
            .setLoopEnd(Integer.toUnsignedLong(loop_end))
            .setSampleRate(sample_rate)
            .setOriginalPitch(original_pitch)
            .setPitchCorrection(pitch_correct)
            .setSampleLink(sample_link)
            .setKind(this.sampleKindOf(shdr, position, name, sample_kind))
            .build();

        final var result =
          NTParsedSample.builder()
            .setSource(NTSource.of(this.source, Integer.toUnsignedLong(position)))
            .setDescription(description)
            .build();

        LOG.trace("[shdr][{}] {}", Integer.valueOf(index), result);
        results.add(result);
        ++index;
      }

      NTInvariants.checkNamedTerminalRecordExists(
        results,
        "EOS",
        "7.10",
        NTSource.of(this.source, shdr.offset()),
        message -> describeChunk(shdr, message));

      return new SourceAndData<>(NTSource.of(this.source, shdr.offset()), results);
    }

    private NTSampleKind sampleKindOf(
      final RiffChunkType chunk,
      final int position,
      final NTSampleName name,
      final int sample_kind)
      throws NTParseException
    {
      for (final var value : NTSampleKind.values()) {
        if (value.value() == sample_kind) {
          return value;
        }
      }

      final var separator = System.lineSeparator();
      throw new NTParseException(
        new StringBuilder(128)
          .append("Unrecognized sample type value.")
          .append(separator)
          .append("  Chunk name: ")
          .append(chunk.name().value())
          .append(separator)
          .append("  Chunk specification: SoundFontⓡ Technical Specification 2.04, §7.10")
          .append(separator)
          .append("  Chunk offset: 0x")
          .append(Long.toUnsignedString(chunk.offset(), 16))
          .append(separator)
          .append("  Chunk data size: ")
          .append(Long.toUnsignedString(chunk.dataSizeIncludingForm().size(), 10))
          .append(separator)
          .append("  Sample name: ")
          .append(name.value())
          .append(separator)
          .append("  Sample kind received: 0x")
          .append(Long.toUnsignedString(Integer.toUnsignedLong(sample_kind), 10))
          .append(separator)
          .append("  Sample offset: 0x")
          .append(Long.toUnsignedString(Integer.toUnsignedLong(position), 16))
          .append(separator)
          .toString(),
        this.source,
        chunk.offset());
    }

    private NTInfo parseInfo(
      final RiffChunkType info_list)
      throws NTParseException
    {
      final var builder = NTInfo.builder();

      builder.setVersion(
        this.parseVersion(this.requireChunk(info_list, "5.1", "ifil")));
      builder.setSoundEngine(
        this.parseShortString(this.requireChunk(info_list, "5.2", "isng")));
      builder.setName(
        this.parseShortString(this.requireChunk(info_list, "5.3", "INAM")));
      builder.setRom(
        info_list.findOptionalSubChunk("irom").map(this::parseShortString));
      builder.setCreationDate(
        info_list.findOptionalSubChunk("ICRD").map(this::parseShortString));
      builder.setEngineers(
        info_list.findOptionalSubChunk("IENG").map(this::parseShortString));
      builder.setProduct(
        info_list.findOptionalSubChunk("IPRD").map(this::parseShortString));
      builder.setCopyright(
        info_list.findOptionalSubChunk("ICOP").map(this::parseShortString));
      builder.setSoftware(
        info_list.findOptionalSubChunk("ISFT").map(this::parseShortString));
      builder.setComment(
        info_list.findOptionalSubChunk("ICMT").map(this::parseLongString));

      return builder.build();
    }

    private NTVersion parseVersion(
      final RiffChunkType chunk)
      throws NTParseException
    {
      final var size = chunk.dataSizeIncludingForm().sizeUnpadded();
      if (size == 4L) {
        final var lower = chunk.dataOffset();
        final var major = this.data.getShort(Math.toIntExact(lower));
        final var minor = this.data.getShort(Math.toIntExact(Math.addExact(lower, 2L)));
        return NTVersion.of((int) major & 0xffff, (int) minor & 0xffff);
      }

      final var separator = System.lineSeparator();
      throw new NTParseException(
        new StringBuilder(128)
          .append("Unable to parse version data from the given chunk.")
          .append(separator)
          .append("  Chunk: ")
          .append(chunk.name().value())
          .append(separator)
          .append("  Size:  ")
          .append(size)
          .append(separator)
          .toString(),
        this.source,
        chunk.offset());
    }

    private NTShortString parseShortString(
      final RiffChunkType chunk)
    {
      return NTShortString.of(this.parseString(chunk));
    }

    private String parseString(
      final RiffChunkType chunk)
    {
      final var size = chunk.dataSizeIncludingForm().sizeUnpadded();
      final var lower = chunk.dataOffset();
      final var upper = Math.addExact(lower, size);

      final var view = this.data.duplicate().order(this.data.order());
      view.position(Math.toIntExact(lower));
      view.limit(Math.toIntExact(upper));

      final var copy = ByteBuffer.allocate(view.remaining());
      copy.put(view);

      return newString(copy.array(), copy.array().length)
        .codePoints()
        .takeWhile(Parser::codePointIsNotNull)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
    }

    private NTLongString parseLongString(
      final RiffChunkType chunk)
    {
      return NTLongString.of(this.parseString(chunk));
    }

    private void checkFormType(
      final RiffChunkType root)
      throws NTParseException
    {
      if (!Objects.equals(root.formType(), Optional.of("sfbk"))) {
        final var separator = System.lineSeparator();
        throw new NTParseException(
          new StringBuilder(128)
            .append("The RIFF form type must be sfbk")
            .append(separator)
            .append("  Expected: sfbk")
            .append(separator)
            .append("  Received: ")
            .append(root.formType().orElse("Nothing"))
            .append(separator)
            .toString(),
          this.source,
          root.offset());
      }
    }
  }
}
