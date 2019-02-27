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

/**
 * SoundFont manipulation (Vanilla implementation)
 */

module com.io7m.jnoisetype.vanilla
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.service.component.annotations;

  requires transitive com.io7m.jnoisetype.api;
  requires transitive com.io7m.jnoisetype.parser.api;
  requires transitive com.io7m.jnoisetype.writer.api;

  requires com.io7m.jspiel.api;
  requires org.slf4j;

  uses com.io7m.jspiel.api.RiffFileParserProviderType;
  uses com.io7m.jspiel.api.RiffFileWriterProviderType;

  provides com.io7m.jnoisetype.parser.api.NTFileParserProviderType
    with com.io7m.jnoisetype.vanilla.NTParsersService;
  provides com.io7m.jnoisetype.parser.api.NTInterpreterProviderType
    with com.io7m.jnoisetype.vanilla.NTInterpreters;
  provides com.io7m.jnoisetype.writer.api.NTWriterProviderType
    with com.io7m.jnoisetype.vanilla.NTWritersService;
  provides com.io7m.jnoisetype.writer.api.NTBuilderProviderType
    with com.io7m.jnoisetype.vanilla.NTBuilders;

  exports com.io7m.jnoisetype.vanilla;
}
