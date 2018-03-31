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

import elemental2.core.JsRegExp;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

public final class QrCodeTool
{
  private static final int MIN_VERSION = 1;
  private static final int MAX_VERSION = 40;
  static final int AUTO_MASK = -1;
  private static final int MIN_MASK = 0;
  private static final int MAX_MASK = 7;
  // @formatter:off
  static final byte[][] ECC_CODEWORDS_PER_BLOCK = {
    // Version: (note that index 0 is for padding, and is set to an illegal value)
    //0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40    Error correction level
    {-1,  7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},  // Low
    {-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28},  // Medium
    {-1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},  // Quartile
    {-1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},  // High
  };
  static final byte[][] NUM_ERROR_CORRECTION_BLOCKS = {
    // Version: (note that index 0 is for padding, and is set to an illegal value)
    //0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40    Error correction level
    {-1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4,  4,  4,  4,  4,  6,  6,  6,  6,  7,  8,  8,  9,  9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25},  // Low
    {-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5,  5,  8,  9,  9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49},  // Medium
    {-1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8,  8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68},  // Quartile
    {-1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81},  // High
  };
  // @formatter:on

  /**
   * Can test whether a string is encodable in numeric mode (such as by using {@link #makeNumeric(String)}).
   */
  private static final JsRegExp NUMERIC_REGEX = new JsRegExp( "^[0-9]+$" );
  /**
   * Can test whether a string is encodable in alphanumeric mode (such as by using {@link #makeAlphanumeric(String)}).
   */
  private static final JsRegExp ALPHANUMERIC_REGEX = new JsRegExp( "^[A-Z0-9 $%*+./:-]*$" );
  /**
   * The set of all legal characters in alphanumeric mode, where each character value maps to the index in the string.
   */
  private static final String ALPHANUMERIC_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";

  /**
   * Returns a QR Code symbol representing the specified Unicode text string at the specified error correction level.
   * As a conservative upper bound, this function is guaranteed to succeed for strings that have 738 or fewer
   * Unicode code points (not UTF-16 code units) if the low error correction level is used. The smallest possible
   * QR Code version is automatically chosen for the output. The ECC level of the result may be higher than the
   * ecl argument if it can be done without increasing the version.
   *
   * @param text the text to be encoded, which can be any Unicode string
   * @param ecl  the error correction level to use (will be boosted)
   * @return a QR Code representing the text
   * @throws NullPointerException     if the text or error correction level is {@code null}
   * @throws IllegalArgumentException if the text fails to fit in the largest version QR Code, which means it is too long
   */
  public static QrCode encodeText( @Nonnull final String text,
                                   @Nonnull final Ecc ecl )
  {
    return encodeSegments( makeSegments( Objects.requireNonNull( text ) ), Objects.requireNonNull( ecl ) );
  }

  /**
   * Returns a QR Code symbol representing the specified binary data string at the specified error correction level.
   * This function always encodes using the binary segment mode, not any text mode. The maximum number of
   * bytes allowed is 2953. The smallest possible QR Code version is automatically chosen for the output.
   * The ECC level of the result may be higher than the ecl argument if it can be done without increasing the version.
   *
   * @param data the binary data to encode
   * @param ecl  the error correction level to use (will be boosted)
   * @return a QR Code representing the binary data
   * @throws NullPointerException     if the data or error correction level is {@code null}
   * @throws IllegalArgumentException if the data fails to fit in the largest version QR Code, which means it is too long
   */
  public static QrCode encodeBinary( @Nonnull final byte[] data, @Nonnull final Ecc ecl )
  {
    return encodeSegments( Collections.singletonList( makeBytes( Objects.requireNonNull( data ) ) ),
                           Objects.requireNonNull( ecl ) );
  }

  /**
   * Returns a QR Code symbol representing the specified data segments at the specified error correction
   * level or higher. The smallest possible QR Code version is automatically chosen for the output.
   * <p>This function allows the user to create a custom sequence of segments that switches
   * between modes (such as alphanumeric and binary) to encode text more efficiently.
   * This function is considered to be lower level than simply encoding text or binary data.</p>
   *
   * @param segments the segments to encode
   * @param ecl      the error correction level to use (will be boosted)
   * @return a QR Code representing the segments
   * @throws NullPointerException     if the list of segments, a segment, or the error correction level is {@code null}
   * @throws IllegalArgumentException if the data is too long to fit in the largest version QR Code at the ECL
   */
  public static QrCode encodeSegments( @Nonnull final List<QrSegment> segments, @Nonnull final Ecc ecl )
  {
    return encodeSegments( segments, ecl, MIN_VERSION, MAX_VERSION, -1, true );
  }

  /**
   * Returns a QR Code symbol representing the specified data segments with the specified encoding parameters.
   * The smallest possible QR Code version within the specified range is automatically chosen for the output.
   * <p>This function allows the user to create a custom sequence of segments that switches
   * between modes (such as alphanumeric and binary) to encode text more efficiently.
   * This function is considered to be lower level than simply encoding text or binary data.</p>
   *
   * @param segments   the segments to encode
   * @param ecl        the error correction level to use (may be boosted)
   * @param minVersion the minimum allowed version of the QR symbol (at least 1)
   * @param maxVersion the maximum allowed version of the QR symbol (at most 40)
   * @param mask       the mask pattern to use, which is either -1 for automatic choice or from 0 to 7 for fixed choice
   * @param boostEcl   increases the error correction level if it can be done without increasing the version number
   * @return a QR Code representing the segments
   * @throws NullPointerException     if the list of segments, a segment, or the error correction level is {@code null}
   * @throws IllegalArgumentException if 1 &le; minVersion &le; maxVersion &le; 40 is violated, or if mask
   *                                  &lt; &minus;1 or mask &gt; 7, or if the data is too long to fit in a QR Code at maxVersion at the ECL
   */
  public static QrCode encodeSegments( @Nonnull final List<QrSegment> segments,
                                       @Nonnull Ecc ecl,
                                       int minVersion,
                                       int maxVersion,
                                       int mask,
                                       boolean boostEcl )
  {
    Objects.requireNonNull( segments );
    Objects.requireNonNull( ecl );
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> isVersionValid( minVersion ),
                    () -> "MinVersion value specified '" + minVersion + "' is out of range." );
      apiInvariant( () -> isVersionValid( maxVersion ),
                    () -> "MaxVersion value specified '" + maxVersion + "' is out of range." );
      apiInvariant( () -> minVersion <= maxVersion,
                    () -> "MinVersion " + minVersion + " is greater than MaxVersion " + maxVersion );
      apiInvariant( () -> isMaskValid( mask ) || AUTO_MASK == mask, () -> "Mask " + mask + " is out of range." );
    }

    // Find the minimal version number to use
    int version, dataUsedBits;
    for ( version = minVersion; ; version++ )
    {
      int dataCapacityBits = getNumDataCodewords( version, ecl ) * 8;  // Number of data bits available
      dataUsedBits = getTotalBits( segments, version );
      if ( dataUsedBits != -1 && dataUsedBits <= dataCapacityBits )
      {
        break;  // This version number is found to be suitable
      }
      if ( BrainCheckConfig.checkInvariants() )
      {
        final int v = version;
        invariant( () -> v < maxVersion, () -> "All versions in the range could not fit the given data" );
      }
    }

    // Increase the error correction level while the data still fits in the current version number
    for ( final Ecc newEcl : Ecc.values() )
    {
      if ( boostEcl && dataUsedBits <= getNumDataCodewords( version, newEcl ) * 8 )
      {
        ecl = newEcl;
      }
    }

    // Create the data bit string by concatenating all segments
    final int dataCapacityBits = getNumDataCodewords( version, ecl ) * 8;
    final BitBuffer bb = new BitBuffer();
    for ( final QrSegment seg : segments )
    {
      bb.appendBits( seg.getMode().getModeBits(), 4 );
      bb.appendBits( seg.getNumChars(), seg.getMode().numCharCountBits( version ) );
      bb.appendData( seg );
    }

    // Add terminator and pad up to a byte if applicable
    bb.appendBits( 0, Math.min( 4, dataCapacityBits - bb.bitLength() ) );
    bb.appendBits( 0, ( 8 - bb.bitLength() % 8 ) % 8 );

    // Pad with alternate bytes until data capacity is reached
    for ( int padByte = 0xEC; bb.bitLength() < dataCapacityBits; padByte ^= 0xEC ^ 0x11 )
    {
      bb.appendBits( padByte, 8 );
    }
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> bb.bitLength() % 8 == 0, () -> "Invalid remainder." );
    }

    // Create the QR Code symbol
    return new QrCode( version, ecl, bb.getBytes(), mask );
  }

  // Returns the number of data bits that can be stored in a QR Code of the given version number, after
  // all function modules are excluded. This includes remainder bits, so it might not be a multiple of 8.
  // The result is in the range [208, 29648]. This could be implemented as a 40-entry lookup table.
  static int getNumRawDataModules( final int version )
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> isVersionValid( version ),
                    () -> "Version value specified '" + version + "' is out of range." );
    }

    int size = version * 4 + 17;
    int result = size * size;   // Number of modules in the whole QR symbol square
    result -= 64 * 3;           // Subtract the three finders with separators
    result -= 15 * 2 + 1;       // Subtract the format information and black module
    result -= ( size - 16 ) * 2;  // Subtract the timing patterns
    // The five lines above are equivalent to: int result = (16 * ver + 128) * ver + 64;
    if ( version >= 2 )
    {
      int numAlign = version / 7 + 2;
      result -=
        ( numAlign - 1 ) * ( numAlign - 1 ) * 25;  // Subtract alignment patterns not overlapping with timing patterns
      result -= ( numAlign - 2 ) * 2 * 20;  // Subtract alignment patterns that overlap with timing patterns
      // The two lines above are equivalent to: result -= (25 * numAlign - 10) * numAlign - 55;
      if ( version >= 7 )
      {
        result -= 18 * 2;  // Subtract version information
      }
    }
    return result;
  }

  // Returns the number of 8-bit data (i.e. not error correction) codewords contained in any
  // QR Code of the given version number and error correction level, with remainder bits discarded.
  // This stateless pure function could be implemented as a (40*4)-cell lookup table.
  static int getNumDataCodewords( final int version, @Nonnull final Ecc ecl )
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> isVersionValid( version ),
                    () -> "Version value specified '" + version + "' is out of range." );
    }
    return getNumRawDataModules( version ) /
           8 -
           ECC_CODEWORDS_PER_BLOCK[ ecl.ordinal() ][ version ] *
           NUM_ERROR_CORRECTION_BLOCKS[ ecl.ordinal() ][ version ];
  }

  // Returns a set of positions of the alignment patterns in ascending order. These positions are
  // used on both the x and y axes. Each value in the resulting array is in the range [0, 177).
  // This stateless pure function could be implemented as table of 40 variable-length lists of unsigned bytes.
  static int[] getAlignmentPatternPositions( final int version )
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> isVersionValid( version ),
                    () -> "Version value specified '" + version + "' is out of range." );
    }
    if ( 1 == version )
    {
      return new int[]{};
    }
    else
    {
      int numAlign = version / 7 + 2;
      int step;
      if ( version != 32 )
      {
        // ceil((size - 13) / (2*numAlign - 2)) * 2
        step = ( version * 4 + numAlign * 2 + 1 ) / ( 2 * numAlign - 2 ) * 2;
      }
      else  // C-C-C-Combo breaker!
      {
        step = 26;
      }

      int[] result = new int[ numAlign ];
      result[ 0 ] = 6;
      for ( int i = result.length - 1, pos = version * 4 + 10; i >= 1; i--, pos -= step )
      {
        result[ i ] = pos;
      }
      return result;
    }
  }

  static boolean isMaskValid( final int mask )
  {
    return MIN_MASK <= mask && MAX_MASK >= mask;
  }

  static boolean isVersionValid( final int version )
  {
    return MIN_VERSION <= version && MAX_VERSION >= version;
  }

  static boolean isDataLengthValid( final int version, @Nonnull final Ecc errorCorrectionLevel, final int dataLength )
  {
    return dataLength == getNumDataCodewords( version, errorCorrectionLevel );
  }

  /**
   * Returns a segment representing the specified binary data encoded in byte mode.
   *
   * @param data the binary data
   * @return a segment containing the data
   */
  public static QrSegment makeBytes( @Nonnull final byte[] data )
  {
    Objects.requireNonNull( data );
    BitBuffer bb = new BitBuffer();
    for ( byte b : data )
    {
      bb.appendBits( b & 0xFF, 8 );
    }
    return new QrSegment( Mode.BYTE, data.length, bb );
  }

  /**
   * Returns a segment representing the specified string of decimal digits encoded in numeric mode.
   *
   * @param digits a string consisting of digits from 0 to 9
   * @return a segment containing the data
   * @throws NullPointerException     if the string is {@code null}
   * @throws IllegalArgumentException if the string contains non-digit characters
   */
  public static QrSegment makeNumeric( @Nonnull final String digits )
  {
    Objects.requireNonNull( digits );
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> NUMERIC_REGEX.test( digits ), () -> "String contains non-numeric characters" );
    }

    final BitBuffer bb = new BitBuffer();
    int i;
    for ( i = 0; i + 3 <= digits.length(); i += 3 )  // Process groups of 3
    {
      bb.appendBits( Integer.parseInt( digits.substring( i, i + 3 ) ), 10 );
    }
    int rem = digits.length() - i;
    if ( rem > 0 )  // 1 or 2 digits remaining
    {
      bb.appendBits( Integer.parseInt( digits.substring( i ) ), rem * 3 + 1 );
    }
    return new QrSegment( Mode.NUMERIC, digits.length(), bb );
  }

  /**
   * Returns a segment representing the specified text string encoded in alphanumeric mode.
   * The characters allowed are: 0 to 9, A to Z (uppercase only), space,
   * dollar, percent, asterisk, plus, hyphen, period, slash, colon.
   *
   * @param text a string of text, with only certain characters allowed
   * @return a segment containing the data
   * @throws IllegalArgumentException if the string contains non-encodable characters
   */
  public static QrSegment makeAlphanumeric( @Nonnull final String text )
  {
    Objects.requireNonNull( text );
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> ALPHANUMERIC_REGEX.test( text ),
                 () -> "String contains unencodable characters in alphanumeric mode" );
    }

    final BitBuffer bb = new BitBuffer();
    int i;
    for ( i = 0; i + 2 <= text.length(); i += 2 )
    {
      // Process groups of 2
      final int temp =
        ALPHANUMERIC_CHARSET.indexOf( text.charAt( i ) ) * 45 +
        ALPHANUMERIC_CHARSET.indexOf( text.charAt( i + 1 ) );
      bb.appendBits( temp, 11 );
    }
    if ( i < text.length() )  // 1 character remaining
    {
      bb.appendBits( ALPHANUMERIC_CHARSET.indexOf( text.charAt( i ) ), 6 );
    }
    return new QrSegment( Mode.ALPHANUMERIC, text.length(), bb );
  }

  /**
   * Returns a new mutable list of zero or more segments to represent the specified Unicode text string.
   * The result may use various segment modes and switch modes to optimize the length of the bit stream.
   *
   * @param text the text to be encoded, which can be any Unicode string
   * @return a list of segments containing the text
   */
  @Nonnull
  private static List<QrSegment> makeSegments( @Nonnull final String text )
  {
    Objects.requireNonNull( text );

    // Select the most efficient segment encoding automatically
    final List<QrSegment> result = new ArrayList<>();
    if ( !text.isEmpty() )
    {
      if ( NUMERIC_REGEX.test( text ) )
      {
        result.add( makeNumeric( text ) );
      }
      else if ( ALPHANUMERIC_REGEX.test( text ) )
      {
        result.add( makeAlphanumeric( text ) );
      }
      else
      {
        result.add( makeBytes( text.getBytes( StandardCharsets.UTF_8 ) ) );
      }
    }
    return result;
  }

  /**
   * Returns a segment representing an Extended Channel Interpretation
   * (ECI) designator with the specified assignment value.
   *
   * @param assignVal the ECI assignment number (see the AIM ECI specification)
   * @return a segment containing the data
   * @throws IllegalArgumentException if the value is outside the range [0, 10<sup>6</sup>)
   */
  public static QrSegment makeEci( final int assignVal )
  {
    BitBuffer bb = new BitBuffer();
    if ( 0 <= assignVal && assignVal < ( 1 << 7 ) )
    {
      bb.appendBits( assignVal, 8 );
    }
    else if ( ( 1 << 7 ) <= assignVal && assignVal < ( 1 << 14 ) )
    {
      bb.appendBits( 2, 2 );
      bb.appendBits( assignVal, 14 );
    }
    else if ( ( 1 << 14 ) <= assignVal && assignVal < 1000000 )
    {
      bb.appendBits( 6, 3 );
      bb.appendBits( assignVal, 21 );
    }
    else
    {
      if ( BrainCheckConfig.checkInvariants() )
      {
        fail( () -> "ECI assignment value out of range" );
      }
    }
    return new QrSegment( Mode.ECI, 0, bb );
  }

  private static int getTotalBits( @Nonnull final List<QrSegment> segments, final int version )
  {
    Objects.requireNonNull( segments );
    apiInvariant( () -> isVersionValid( version ),
                  () -> "Version value specified '" + version + "' is out of range." );

    long result = 0;
    for ( QrSegment seg : segments )
    {
      Objects.requireNonNull( seg );
      int ccbits = seg.getMode().numCharCountBits( version );
      // Fail if segment length value doesn't fit in the length field's bit-width
      if ( seg.getNumChars() >= ( 1 << ccbits ) )
      {
        return -1;
      }
      result += 4L + ccbits + seg.getData().bitLength();
      if ( result > Integer.MAX_VALUE )
      {
        return -1;
      }
    }
    return (int) result;
  }
}
