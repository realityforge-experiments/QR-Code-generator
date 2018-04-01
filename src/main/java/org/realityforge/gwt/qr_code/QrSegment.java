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
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

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
  @Nonnull
  private final Mode _mode;
  @Nonnegative
  private final int _numChars;
  @Nonnull
  private final int[] _data;
  @Nonnegative
  private final int _bitLength;

  /**
   * Creates a new QR Code data segment with the specified parameters and data.
   *
   * @param mode      the mode, which is not {@code null}
   * @param numChars  the data length in characters, which is non-negative
   * @param data      the data bits of this segment.
   * @param bitLength the length of data in segment.
   * @throws NullPointerException     if the mode or bit buffer is {@code null}
   * @throws IllegalArgumentException if the character count is negative
   */
  public QrSegment( final @Nonnull Mode mode,
                    @Nonnegative final int numChars,
                    @Nonnull final int[] data,
                    final int bitLength )
  {
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> numChars >= 0, () -> "numChars must be non-negative" );
      invariant( () -> bitLength >= 0, () -> "bitLength must be non-negative" );
      invariant( () -> bitLength <= data.length * 32, () -> "bitLength invalid value" );
    }
    _mode = Objects.requireNonNull( mode );
    _numChars = numChars;
    _data = Objects.requireNonNull( data );
    _bitLength = bitLength;
  }

  /**
   * Return the mode indicator for this segment.
   *
   * @return the mode indicator for this segment.
   */
  @Nonnull
  public Mode getMode()
  {
    return _mode;
  }

  /**
   * Return the length of this segment's unencoded data, measured in characters.
   *
   * @return the length of this segment's unencoded data, measured in characters.
   */
  public int getNumChars()
  {
    return _numChars;
  }

  /**
   * Return the data bits of this segment.
   *
   * @return the data bits of this segment.
   */
  @Nonnull
  public int[] getData()
  {
    return _data;
  }

  @Nonnegative
  public int getBitLength()
  {
    return _bitLength;
  }
}
