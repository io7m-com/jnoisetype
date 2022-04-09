/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.jnoisetype.cmdline;

import com.beust.jcommander.internal.Console;

final class StringConsole implements Console
{
  private final StringBuilder text;

  StringConsole()
  {
    this.text = new StringBuilder();
  }

  @Override
  public void print(final String msg)
  {
    this.text.append(msg);
  }

  @Override
  public void println(final String msg)
  {
    this.text.append(msg);
    this.text.append(System.lineSeparator());
  }

  @Override
  public char[] readPassword(final boolean echoInput)
  {
    return new char[0];
  }

  public StringBuilder text()
  {
    return this.text;
  }
}
