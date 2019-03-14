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

package com.io7m.jnoisetype.cmdline;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

final class ByteBufferBackedInputStream extends InputStream
{
  private final ByteBuffer buffer;

  ByteBufferBackedInputStream(
    final ByteBuffer in_buffer)
  {
    this.buffer = Objects.requireNonNull(in_buffer, "buf");
  }

  @Override
  public int read()
  {
    if (!this.buffer.hasRemaining()) {
      return -1;
    }
    return (int) this.buffer.get() & 0xFF;
  }

  @Override
  public int read(
    final byte[] bytes,
    final int off,
    final int len)
  {
    Objects.requireNonNull(bytes, "bytes");

    if (!this.buffer.hasRemaining()) {
      return -1;
    }

    final var amount = Math.min(len, this.buffer.remaining());
    this.buffer.get(bytes, off, amount);
    return amount;
  }
}
