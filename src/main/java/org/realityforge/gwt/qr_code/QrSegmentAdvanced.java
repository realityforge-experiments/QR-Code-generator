/*
 * QR Code generator library - Optional advanced logic (Java)
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import static org.realityforge.gwt.qr_code.QrSegment.Mode.*;

public final class QrSegmentAdvanced
{
  /**
   * Returns a new mutable list of zero or more segments to represent the specified Unicode text string.
   * The resulting list optimally minimizes the total encoded bit length, subjected to the constraints given
   * by the specified {error correction level, minimum version number, maximum version number}, plus the additional
   * constraint that the segment modes {NUMERIC, ALPHANUMERIC, BYTE} can be used but KANJI cannot be used.
   * <p>This function can be viewed as a significantly more sophisticated and slower replacement
   * for {@link QrSegment#makeSegments(String)}, but requiring more input parameters in a way
   * that overlaps with {@link QrCode#encodeSegments(List, Ecc, int, int, int, boolean)}.</p>
   *
   * @param text       the text to be encoded, which can be any Unicode string
   * @param ecl        the error correction level to use
   * @param minVersion the minimum allowed version of the QR symbol (at least 1)
   * @param maxVersion the maximum allowed version of the QR symbol (at most 40)
   * @return a list of segments containing the text, minimizing the bit length with respect to the constraints
   * @throws NullPointerException     if the data or error correction level is {@code null}
   * @throws IllegalArgumentException if 1 &le; minVersion &le; maxVersion &le; 40 is violated,
   *                                  or if the data is too long to fit in a QR Code at maxVersion at the ECL
   */
  public static List<QrSegment> makeSegmentsOptimally( String text, Ecc ecl, int minVersion, int maxVersion )
  {
    // Check arguments
    Objects.requireNonNull( text );
    Objects.requireNonNull( ecl );
    if ( !( 1 <= minVersion && minVersion <= maxVersion && maxVersion <= 40 ) )
    {
      throw new IllegalArgumentException( "Invalid value" );
    }

    // Iterate through version numbers, and make tentative segments
    List<QrSegment> segs = null;
    for ( int version = minVersion; version <= maxVersion; version++ )
    {
      if ( version == minVersion || version == 10 || version == 27 )
      {
        segs = makeSegmentsOptimally( text, version );
      }

      // Check if the segments fit
      int dataCapacityBits = QrCode.getNumDataCodewords( version, ecl ) * 8;
      int dataUsedBits = QrSegment.getTotalBits( segs, version );
      if ( dataUsedBits != -1 && dataUsedBits <= dataCapacityBits )
      {
        return segs;
      }
    }
    throw new IllegalArgumentException( "Data too long" );
  }

  // Returns a list of segments that is optimal for the given text at the given version number.
  private static List<QrSegment> makeSegmentsOptimally( String text, int version )
  {
    byte[] data = text.getBytes( StandardCharsets.UTF_8 );
    int[][] bitCosts = computeBitCosts( data, version );
    QrSegment.Mode[] charModes = computeCharacterModes( data, version, bitCosts );
    return splitIntoSegments( data, charModes );
  }

  private static int[][] computeBitCosts( byte[] data, int version )
  {
    // Segment header sizes, measured in 1/6 bits
    int bytesCost = ( 4 + BYTE.numCharCountBits( version ) ) * 6;
    int alphnumCost = ( 4 + ALPHANUMERIC.numCharCountBits( version ) ) * 6;
    int numberCost = ( 4 + NUMERIC.numCharCountBits( version ) ) * 6;

    // result[mode][len] is the number of 1/6 bits to encode the first len characters of the text, ending in the mode
    int[][] result = new int[ 3 ][ data.length + 1 ];
    Arrays.fill( result[ 1 ], Integer.MAX_VALUE / 2 );
    Arrays.fill( result[ 2 ], Integer.MAX_VALUE / 2 );
    result[ 0 ][ 0 ] = bytesCost;
    result[ 1 ][ 0 ] = alphnumCost;
    result[ 2 ][ 0 ] = numberCost;

    // Calculate the cost table using dynamic programming
    for ( int i = 0; i < data.length; i++ )
    {
      // Encode a character
      int j = i + 1;
      char c = (char) data[ i ];
      result[ 0 ][ j ] = result[ 0 ][ i ] + 48;  // 8 bits per byte
      if ( isAlphanumeric( c ) )
      {
        result[ 1 ][ j ] = result[ 1 ][ i ] + 33;  // 5.5 bits per alphanumeric char
      }
      if ( isNumeric( c ) )
      {
        result[ 2 ][ j ] = result[ 2 ][ i ] + 20;  // 3.33 bits per digit
      }

      // Switch modes, rounding up fractional bits
      result[ 0 ][ j ] =
        Math.min( ( Math.min( result[ 1 ][ j ], result[ 2 ][ j ] ) + 5 ) / 6 * 6 + bytesCost, result[ 0 ][ j ] );
      result[ 1 ][ j ] =
        Math.min( ( Math.min( result[ 2 ][ j ], result[ 0 ][ j ] ) + 5 ) / 6 * 6 + alphnumCost, result[ 1 ][ j ] );
      result[ 2 ][ j ] =
        Math.min( ( Math.min( result[ 0 ][ j ], result[ 1 ][ j ] ) + 5 ) / 6 * 6 + numberCost, result[ 2 ][ j ] );
    }
    return result;
  }

  private static QrSegment.Mode[] computeCharacterModes( byte[] data, int version, int[][] bitCosts )
  {
    // Segment header sizes, measured in 1/6 bits
    int bytesCost = ( 4 + BYTE.numCharCountBits( version ) ) * 6;
    int alphnumCost = ( 4 + ALPHANUMERIC.numCharCountBits( version ) ) * 6;
    int numberCost = ( 4 + NUMERIC.numCharCountBits( version ) ) * 6;

    // Infer the mode used for last character by taking the minimum
    QrSegment.Mode curMode;
    int end = bitCosts[ 0 ].length - 1;
    if ( bitCosts[ 0 ][ end ] <= Math.min( bitCosts[ 1 ][ end ], bitCosts[ 2 ][ end ] ) )
    {
      curMode = BYTE;
    }
    else if ( bitCosts[ 1 ][ end ] <= bitCosts[ 2 ][ end ] )
    {
      curMode = ALPHANUMERIC;
    }
    else
    {
      curMode = NUMERIC;
    }

    // Work backwards to calculate optimal encoding mode for each character
    QrSegment.Mode[] result = new QrSegment.Mode[ data.length ];
    if ( data.length == 0 )
    {
      return result;
    }
    result[ data.length - 1 ] = curMode;
    for ( int i = data.length - 2; i >= 0; i-- )
    {
      char c = (char) data[ i ];
      if ( curMode == NUMERIC )
      {
        if ( isNumeric( c ) )
        {
          curMode = NUMERIC;
        }
        else if ( isAlphanumeric( c ) &&
                  ( bitCosts[ 1 ][ i ] + 33 + 5 ) / 6 * 6 + numberCost == bitCosts[ 2 ][ i + 1 ] )
        {
          curMode = ALPHANUMERIC;
        }
        else
        {
          curMode = BYTE;
        }
      }
      else if ( curMode == ALPHANUMERIC )
      {
        if ( isNumeric( c ) && ( bitCosts[ 2 ][ i ] + 20 + 5 ) / 6 * 6 + alphnumCost == bitCosts[ 1 ][ i + 1 ] )
        {
          curMode = NUMERIC;
        }
        else if ( isAlphanumeric( c ) )
        {
          curMode = ALPHANUMERIC;
        }
        else
        {
          curMode = BYTE;
        }
      }
      else
      {
        if ( isNumeric( c ) && ( bitCosts[ 2 ][ i ] + 20 + 5 ) / 6 * 6 + bytesCost == bitCosts[ 0 ][ i + 1 ] )
        {
          curMode = NUMERIC;
        }
        else if ( isAlphanumeric( c ) && ( bitCosts[ 1 ][ i ] + 33 + 5 ) / 6 * 6 + bytesCost == bitCosts[ 0 ][ i + 1 ] )
        {
          curMode = ALPHANUMERIC;
        }
        else
        {
          curMode = BYTE;
        }
      }
      result[ i ] = curMode;
    }
    return result;
  }

  private static List<QrSegment> splitIntoSegments( byte[] data, QrSegment.Mode[] charModes )
  {
    List<QrSegment> result = new ArrayList<>();
    if ( data.length == 0 )
    {
      return result;
    }

    // Accumulate run of modes
    QrSegment.Mode curMode = charModes[ 0 ];
    int start = 0;
    for ( int i = 1; i < data.length; i++ )
    {
      if ( charModes[ i ] != curMode )
      {
        if ( curMode == BYTE )
        {
          result.add( QrSegment.makeBytes( Arrays.copyOfRange( data, start, i ) ) );
        }
        else
        {
          String temp = new String( data, start, i - start, StandardCharsets.US_ASCII );
          if ( curMode == NUMERIC )
          {
            result.add( QrSegment.makeNumeric( temp ) );
          }
          else if ( curMode == ALPHANUMERIC )
          {
            result.add( QrSegment.makeAlphanumeric( temp ) );
          }
          else
          {
            throw new AssertionError();
          }
        }
        curMode = charModes[ i ];
        start = i;
      }
    }

    // Final segment
    if ( curMode == BYTE )
    {
      result.add( QrSegment.makeBytes( Arrays.copyOfRange( data, start, data.length ) ) );
    }
    else
    {
      String temp = new String( data, start, data.length - start, StandardCharsets.US_ASCII );
      if ( curMode == NUMERIC )
      {
        result.add( QrSegment.makeNumeric( temp ) );
      }
      else if ( curMode == ALPHANUMERIC )
      {
        result.add( QrSegment.makeAlphanumeric( temp ) );
      }
      else
      {
        throw new AssertionError();
      }
    }
    return result;
  }

  private static boolean isAlphanumeric( char c )
  {
    return isNumeric( c ) || 'A' <= c && c <= 'Z' || " $%*+./:-".indexOf( c ) != -1;
  }

  private static boolean isNumeric( char c )
  {
    return '0' <= c && c <= '9';
  }
}
