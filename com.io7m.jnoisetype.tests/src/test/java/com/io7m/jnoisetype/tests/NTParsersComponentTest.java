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

import com.io7m.jnoisetype.parser.api.NTFileParserProviderType;
import com.io7m.jnoisetype.vanilla.NTParsers;
import com.io7m.jnoisetype.vanilla.NTParsersComponent;
import com.io7m.jspiel.vanilla.RiffParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NTParsersComponentTest extends NTParsersContract
{
  @Override
  protected NTFileParserProviderType parsers()
  {
    final var parsers = new NTParsersComponent();
    parsers.onRIFFFileParsersAvailable(new RiffParsers());
    parsers.onActivate();
    return parsers;
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(NTParsersComponentTest.class);
  }
}
