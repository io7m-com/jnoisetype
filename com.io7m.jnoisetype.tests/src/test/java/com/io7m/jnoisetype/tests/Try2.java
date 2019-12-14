package com.io7m.jnoisetype.tests;

import com.io7m.jnoisetype.parser.api.NTParseException;
import com.io7m.jnoisetype.vanilla.interpreter.NTInterpreters;
import com.io7m.jnoisetype.vanilla.NTParsers;
import com.io7m.jspiel.vanilla.RiffParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

public final class Try2
{
  private static final Logger LOG = LoggerFactory.getLogger(Try2.class);

  private Try2()
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
      NTInterpreters.interpretFile(file);
    }
  }
}
