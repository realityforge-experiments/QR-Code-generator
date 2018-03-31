/*
 * QR Code generator library (Java)
 *
 * Copyright (c) Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/qr-code-generator-library
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

package org.realityforge.gwt.qr_code;

import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a character string to be encoded in a QR Code symbol. Each segment has
 * a mode, and a sequence of characters that is already encoded as a sequence of bits.
 * Instances of this class are immutable.
 * <p>This segment class imposes no length restrictions, but QR Codes have restrictions.
 * Even in the most favorable conditions, a QR Code can only hold 7089 characters of data.
 * Any segment longer than this is meaningless for the purpose of generating QR Codes.</p>
 */
public final class QrSegment
{
  /**
   * The mode indicator for this segment.
   */
  @Nonnull
  public final Mode mode;

  /**
   * The length of this segment's unencoded data, measured in characters.
   */
  @Nonnegative
  public final int numChars;

  /**
   * The data bits of this segment.
   */
  @Nonnull
  final BitBuffer data;

  /**
   * Creates a new QR Code data segment with the specified parameters and data.
   *
   * @param md    the mode, which is not {@code null}
   * @param numCh the data length in characters, which is non-negative
   * @param data  the data bits of this segment, which is not {@code null}
   * @throws NullPointerException     if the mode or bit buffer is {@code null}
   * @throws IllegalArgumentException if the character count is negative
   */
  public QrSegment( Mode md, int numCh, BitBuffer data )
  {
    Objects.requireNonNull( md );
    Objects.requireNonNull( data );
    if ( numCh < 0 )
    {
      throw new IllegalArgumentException( "Invalid value" );
    }
    mode = md;
    numChars = numCh;
    this.data = data.duplicate();  // Make defensive copy
  }
}
