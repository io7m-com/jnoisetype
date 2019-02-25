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
    final var temp = Files.createTempFile("nt-cmdline-", ".sf2");
    try (var stream = NTCommandLineTest.class.getResourceAsStream("/com/io7m/jnoisetype/tests/complex0.sf2")) {
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
    final var main = new Main(new String[] {
      "show"
    });

    main.run();
    Assertions.assertEquals(1L, (long) main.exitCode(), "Fails");
  }

  @Test
  public void testMainShowNonexistentFails()
  {
    final var main = new Main(new String[] {
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

    final var main = new Main(new String[] {
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

    final var main = new Main(new String[] {
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
    final var main = new Main(new String[] {

    });

    main.run();
    Assertions.assertEquals(0L, (long) main.exitCode(), "Succeeds");
  }
}
