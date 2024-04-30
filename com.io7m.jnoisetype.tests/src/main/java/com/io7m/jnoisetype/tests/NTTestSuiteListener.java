/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

public final class NTTestSuiteListener implements TestExecutionListener
{
  private static final Logger LOG =
    LoggerFactory.getLogger(NTTestSuiteListener.class);

  @Override
  public void testPlanExecutionFinished(
    final TestPlan testPlan)
  {
    try {
      final var baseDirectory =
        NTTestDirectories.createBaseDirectory();

      LOG.debug("deleting {}", baseDirectory);

      try (var stream = Files.walk(baseDirectory)) {
        stream.forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (final IOException e) {
            // Don't care
          }
        });
      }

      try (var stream = Files.walk(baseDirectory)) {
        stream.forEach(path -> {
          try {
            Files.deleteIfExists(path);
          } catch (final IOException e) {
            // Don't care
          }
        });
      }

    } catch (final IOException e) {
      LOG.error("unable to delete temporary directory: ", e);
    }
  }
}
