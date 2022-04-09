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

import com.io7m.jnoisetype.cmdline.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NTCommandLineTest
{
  private static Path createSF2()
    throws IOException
  {
    final var temp = NTTestDirectories.createTempFile("nt-cmdline-", ".sf2");
    try (var stream = NTCommandLineTest.class.getResourceAsStream(
      "/com/io7m/jnoisetype/tests/complex0.sf2")) {
      try (var output = Files.newOutputStream(temp)) {
        stream.transferTo(output);
        output.flush();
      }
    }
    return temp;
  }

  @Test
  public void testMainShowNoArgumentsFails()
  {
    final var main = new Main(new String[]{
      "show"
    });

    main.run();
    Assertions.assertEquals(1L, (long) main.exitCode(), "Fails");
  }

  @Test
  public void testMainShowNonexistentFails()
  {
    final var main = new Main(new String[]{
      "show",
      "--file",
      "/nonexistent"
    });

    main.run();
    Assertions.assertEquals(1L, (long) main.exitCode(), "Fails");
  }

  @Test
  public void testMainShowOK()
    throws IOException
  {
    final var temp = createSF2();

    final var main = new Main(new String[]{
      "show",
      "--file",
      temp.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");
  }

  @Test
  public void testMainShowOKVerbose()
    throws IOException
  {
    final var temp = createSF2();

    final var main = new Main(new String[]{
      "show",
      "--verbose",
      "trace",
      "--file",
      temp.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");
  }

  @Test
  public void testMainNoArgumentsOK()
  {
    final var main = new Main(new String[]{

    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");
  }

  @Test
  public void testMainExtractAll()
    throws IOException
  {
    final var temp = createSF2();
    final var output = NTTestDirectories.createTempDirectory();

    final var main = new Main(new String[]{
      "extract-samples",
      "--file",
      temp.toString(),
      "--output-directory",
      output.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");

    final var path0 = output.resolve("000_60.wav");
    Assertions.assertTrue(Files.isRegularFile(path0));
    Assertions.assertEquals(16584L, Files.size(path0));
    Assertions.assertTrue(Files.probeContentType(path0).contains("audio"));
    Assertions.assertTrue(Files.probeContentType(path0).contains("wav"));

    final var path1 = output.resolve("002_60.wav");
    Assertions.assertTrue(Files.isRegularFile(path1));
    Assertions.assertEquals(16584L, Files.size(path1));
    Assertions.assertTrue(Files.probeContentType(path1).contains("audio"));
    Assertions.assertTrue(Files.probeContentType(path1).contains("wav"));
  }

  @Test
  public void testMainExtractIncludeSome()
    throws IOException
  {
    final var temp = createSF2();
    final var output = NTTestDirectories.createTempDirectory();

    final var main = new Main(new String[]{
      "extract-samples",
      "--pattern-include",
      "^000_60$",
      "--file",
      temp.toString(),
      "--output-directory",
      output.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");

    final var path0 = output.resolve("000_60.wav");
    Assertions.assertTrue(Files.isRegularFile(path0));
    Assertions.assertEquals(16584L, Files.size(path0));
    Assertions.assertTrue(Files.probeContentType(path0).contains("audio"));
    Assertions.assertTrue(Files.probeContentType(path0).contains("wav"));

    final var path1 = output.resolve("002_60.wav");
    Assertions.assertFalse(Files.exists(path1));
  }

  @Test
  public void testMainExtractIncludeNone()
    throws IOException
  {
    final var temp = createSF2();
    final var output = NTTestDirectories.createTempDirectory();

    final var main = new Main(new String[]{
      "extract-samples",
      "--pattern-include",
      "",
      "--file",
      temp.toString(),
      "--output-directory",
      output.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");

    final var path0 = output.resolve("000_60.wav");
    Assertions.assertFalse(Files.exists(path0));
    final var path1 = output.resolve("002_60.wav");
    Assertions.assertFalse(Files.exists(path1));
  }

  @Test
  public void testMainExtractExcludeSome()
    throws IOException
  {
    final var temp = createSF2();
    final var output = NTTestDirectories.createTempDirectory();

    final var main = new Main(new String[]{
      "extract-samples",
      "--pattern-exclude",
      "^000_60$",
      "--file",
      temp.toString(),
      "--output-directory",
      output.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");

    final var path0 = output.resolve("000_60.wav");
    Assertions.assertFalse(Files.exists(path0));

    final var path1 = output.resolve("002_60.wav");
    Assertions.assertTrue(Files.isRegularFile(path1));
    Assertions.assertEquals(16584L, Files.size(path1));
    Assertions.assertTrue(Files.probeContentType(path1).contains("audio"));
    Assertions.assertTrue(Files.probeContentType(path1).contains("wav"));
  }

  @Test
  public void testMainExtractExcludeAll()
    throws IOException
  {
    final var temp = createSF2();
    final var output = NTTestDirectories.createTempDirectory();

    final var main = new Main(new String[]{
      "extract-samples",
      "--pattern-exclude",
      ".*",
      "--file",
      temp.toString(),
      "--output-directory",
      output.toString()
    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");

    final var path0 = output.resolve("000_60.wav");
    Assertions.assertFalse(Files.exists(path0));

    final var path1 = output.resolve("002_60.wav");
    Assertions.assertFalse(Files.exists(path1));
  }
}
