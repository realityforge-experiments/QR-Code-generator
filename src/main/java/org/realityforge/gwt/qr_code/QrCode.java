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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * Represents an immutable square grid of black and white cells for a QR Code symbol, and
 * provides static functions to create a QR Code from user-supplied textual or binary data.
 * <p>This class covers the QR Code model 2 specification, supporting all versions (sizes)
 * from 1 to 40, all 4 error correction levels, and only 3 character encoding modes.</p>
 */
public final class QrCode
{
  private final int _version;
  private final int _size;
  private final Ecc _errorCorrectionLevel;
  private final int _mask;

  // Private grids of modules/pixels (conceptually immutable)
  private boolean[][] _modules;     // The modules of this QR Code symbol (false = white, true = black)
  private boolean[][] _isFunction;  // Indicates function modules that are not subjected to masking

  /**
   * Creates a new QR Code symbol with the specified version number, error correction level, binary data array, and mask number.
   * <p>This is a cumbersome low-level constructor that should not be invoked directly by the user.
   * To go one level up, see the {@link QrCodeTool#encodeSegments(List, Ecc)} function.</p>
   *
   * @param version       the version number to use, which must be in the range 1 to 40, inclusive
   * @param ecl           the error correction level to use
   * @param dataCodewords the raw binary user data to encode
   * @param mask          the mask pattern to use, which is either -1 for automatic choice or from 0 to 7 for fixed choice
   * @throws NullPointerException     if the byte array or error correction level is {@code null}
   * @throws IllegalArgumentException if the version or mask value is out of range
   */
  QrCode( final int version, @Nonnull final Ecc ecl, @Nonnull final byte[] dataCodewords, final int mask )
  {
    // Check arguments
    Objects.requireNonNull( ecl );
    Objects.requireNonNull( dataCodewords );
    assert QrCodeTool.isVersionValid( version );
    assert QrCodeTool.isMaskValid( mask ) || QrCodeTool.AUTO_MASK == mask;

    // Initialize fields
    _version = version;
    _size = version * 4 + 17;
    _errorCorrectionLevel = ecl;
    _modules = new boolean[ _size ][ _size ];  // Entirely white grid
    _isFunction = new boolean[ _size ][ _size ];

    // Draw function patterns, draw all codewords, do masking
    drawFunctionPatterns();
    drawCodewords( appendErrorCorrection( dataCodewords ) );
    _mask = handleConstructorMasking( mask );
  }

  /**
   * Return the QR Code symbol's version number, which is always between 1 and 40 (inclusive).
   *
   * @return the QR Code symbol's version number, which is always between 1 and 40 (inclusive).
   */
  public int getVersion()
  {
    return _version;
  }

  /**
   * The width and height of this QR Code symbol, measured in modules.
   * Always equal to version &times; 4 + 17, in the range 21 to 177.
   *
   * @return the width and height of this QR Code symbol, measured in modules.
   */
  public int getSize()
  {
    return _size;
  }

  /**
   * The error correction level used in this QR Code symbol.
   *
   * @return the error correction level used in this QR Code symbol.
   */
  @Nonnull
  public Ecc getErrorCorrectionLevel()
  {
    return _errorCorrectionLevel;
  }

  /**
   * The mask pattern used in this QR Code symbol, in the range 0 to 7 (i.e. unsigned 3-bit integer).
   * Note that even if a constructor was called with automatic masking requested
   * (_mask = -1), the resulting object will still have a mask value between 0 and 7.
   */
  public int getMask()
  {
    return _mask;
  }

  /**
   * Returns the color of the module (pixel) at the specified coordinates, which is either
   * false for white or true for black. The top left corner has the coordinates (x=0, y=0).
   * If the specified coordinates are out of bounds, then false (white) is returned.
   *
   * @param x the x coordinate, where 0 is the left edge and size&minus;1 is the right edge
   * @param y the y coordinate, where 0 is the top edge and size&minus;1 is the bottom edge
   * @return the module's color, which is either false (white) or true (black)
   */
  public boolean getModule( int x, int y )
  {
    return 0 <= x && x < _size && 0 <= y && y < _size && _modules[ y ][ x ];
  }

  /*
   * Returns a new image object representing this QR Code, with the specified module scale and number
   * of border modules. For example, the arguments scale=10, border=4 means to pad the QR Code symbol
   * with 4 white border modules on all four edges, then use 10*10 pixels to represent each module.
   * The resulting image only contains the hex colors 000000 and FFFFFF.
   *
   * @param scale  the module scale factor, which must be positive
   * @param border the number of border modules to add, which must be non-negative
   * @return an image representing this QR Code, with padding and scaling
   * @throws IllegalArgumentException if the scale or border is out of range
   */
  /*
  public BufferedImage toImage( int scale, int border )
  {
    if ( scale <= 0 || border < 0 )
    {
      throw new IllegalArgumentException( "Value out of range" );
    }
    if ( border > Integer.MAX_VALUE / 2 || _size + border * 2L > Integer.MAX_VALUE / scale )
    {
      throw new IllegalArgumentException( "Scale or border too large" );
    }

    BufferedImage result =
      new BufferedImage( ( _size + border * 2 ) * scale, ( _size + border * 2 ) * scale, BufferedImage.TYPE_INT_RGB );
    for ( int y = 0; y < result.getHeight(); y++ )
    {
      for ( int x = 0; x < result.getWidth(); x++ )
      {
        boolean val = getModule( x / scale - border, y / scale - border );
        result.setRGB( x, y, val ? 0x000000 : 0xFFFFFF );
      }
    }
    return result;
  }
  */

  /**
   * Based on the specified number of border modules to add as padding, this returns a
   * string whose contents represents an SVG XML file that depicts this QR Code symbol.
   * Note that Unix newlines (\n) are always used, regardless of the platform.
   *
   * @param border the number of border modules to add, which must be non-negative
   * @return a string representing this QR Code as an SVG document
   */
  public String toSvgString( final int border )
  {
    if ( border < 0 )
    {
      throw new IllegalArgumentException( "Border must be non-negative" );
    }
    if ( _size + border * 2L > Integer.MAX_VALUE )
    {
      throw new IllegalArgumentException( "Border too large" );
    }

    StringBuilder sb = new StringBuilder();
    sb.append( "<?xml _version=\"1.0\" encoding=\"UTF-8\"?>\n" );
    sb.append(
      "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" );
    final int dimension = _size + border * 2;
    sb.append( "<svg xmlns=\"http://www.w3.org/2000/svg\" _version=\"1.1\" viewBox=\"0 0 " )
      .append( dimension )
      .append( " " )
      .append( dimension )
      .append( "\" stroke=\"none\">\n" )
      .append( "\t<rect width=\"100%\" height=\"100%\" fill=\"#FFFFFF\"/>\n" )
      .append( "\t<path d=\"" );
    boolean head = true;
    for ( int y = -border; y < _size + border; y++ )
    {
      for ( int x = -border; x < _size + border; x++ )
      {
        if ( getModule( x, y ) )
        {
          if ( head )
          {
            head = false;
          }
          else
          {
            sb.append( " " );
          }
          sb.append( "M" ).append( x + border ).append( "," ).append( y + border ).append( "h1v1h-1z" );
        }
      }
    }
    sb.append( "\" fill=\"#000000\"/>\n" );
    sb.append( "</svg>\n" );
    return sb.toString();
  }

  private void drawFunctionPatterns()
  {
    // Draw horizontal and vertical timing patterns
    for ( int i = 0; i < _size; i++ )
    {
      setFunctionModule( 6, i, i % 2 == 0 );
      setFunctionModule( i, 6, i % 2 == 0 );
    }

    // Draw 3 finder patterns (all corners except bottom right; overwrites some timing modules)
    drawFinderPattern( 3, 3 );
    drawFinderPattern( _size - 4, 3 );
    drawFinderPattern( 3, _size - 4 );

    // Draw numerous alignment patterns
    int[] alignPatPos = QrCodeTool.getAlignmentPatternPositions( _version );
    int numAlign = alignPatPos.length;
    for ( int i = 0; i < numAlign; i++ )
    {
      for ( int j = 0; j < numAlign; j++ )
      {
        if ( ( i != 0 || j != 0 ) && ( i != 0 || j != numAlign - 1 ) && ( i != numAlign - 1 || j != 0 ) )
        {
          drawAlignmentPattern( alignPatPos[ i ], alignPatPos[ j ] );
        }
      }
    }

    // Draw configuration data
    drawFormatBits( 0 );  // Dummy mask value; overwritten later in the constructor
    drawVersion();
  }

  // Draws two copies of the format bits (with its own error correction code)
  // based on the given mask and this object's error correction level field.
  private void drawFormatBits( int mask )
  {
    // Calculate error correction code and pack bits
    int data = _errorCorrectionLevel.getFormatBits() << 3 | mask;  // errCorrLvl is uint2, mask is uint3
    int rem = data;
    for ( int i = 0; i < 10; i++ )
    {
      rem = ( rem << 1 ) ^ ( ( rem >>> 9 ) * 0x537 );
    }
    data = data << 10 | rem;
    data ^= 0x5412;  // uint15
    if ( data >>> 15 != 0 )
    {
      throw new AssertionError();
    }

    // Draw first copy
    for ( int i = 0; i <= 5; i++ )
    {
      setFunctionModule( 8, i, ( ( data >>> i ) & 1 ) != 0 );
    }
    setFunctionModule( 8, 7, ( ( data >>> 6 ) & 1 ) != 0 );
    setFunctionModule( 8, 8, ( ( data >>> 7 ) & 1 ) != 0 );
    setFunctionModule( 7, 8, ( ( data >>> 8 ) & 1 ) != 0 );
    for ( int i = 9; i < 15; i++ )
    {
      setFunctionModule( 14 - i, 8, ( ( data >>> i ) & 1 ) != 0 );
    }

    // Draw second copy
    for ( int i = 0; i <= 7; i++ )
    {
      setFunctionModule( _size - 1 - i, 8, ( ( data >>> i ) & 1 ) != 0 );
    }
    for ( int i = 8; i < 15; i++ )
    {
      setFunctionModule( 8, _size - 15 + i, ( ( data >>> i ) & 1 ) != 0 );
    }
    setFunctionModule( 8, _size - 8, true );
  }

  // Draws two copies of the version bits (with its own error correction code),
  // based on this object's version field (which only has an effect for 7 <= version <= 40).
  private void drawVersion()
  {
    if ( _version < 7 )
    {
      return;
    }

    // Calculate error correction code and pack bits
    int rem = _version;  // version is uint6, in the range [7, 40]
    for ( int i = 0; i < 12; i++ )
    {
      rem = ( rem << 1 ) ^ ( ( rem >>> 11 ) * 0x1F25 );
    }
    int data = _version << 12 | rem;  // uint18
    if ( data >>> 18 != 0 )
    {
      throw new AssertionError();
    }

    // Draw two copies
    for ( int i = 0; i < 18; i++ )
    {
      boolean bit = ( ( data >>> i ) & 1 ) != 0;
      int a = _size - 11 + i % 3, b = i / 3;
      setFunctionModule( a, b, bit );
      setFunctionModule( b, a, bit );
    }
  }

  // Draws a 9*9 finder pattern including the border separator, with the center module at (x, y).
  private void drawFinderPattern( int x, int y )
  {
    for ( int i = -4; i <= 4; i++ )
    {
      for ( int j = -4; j <= 4; j++ )
      {
        int dist = Math.max( Math.abs( i ), Math.abs( j ) );  // Chebyshev/infinity norm
        int xx = x + j, yy = y + i;
        if ( 0 <= xx && xx < _size && 0 <= yy && yy < _size )
        {
          setFunctionModule( xx, yy, dist != 2 && dist != 4 );
        }
      }
    }
  }

  // Draws a 5*5 alignment pattern, with the center module at (x, y).
  private void drawAlignmentPattern( int x, int y )
  {
    for ( int i = -2; i <= 2; i++ )
    {
      for ( int j = -2; j <= 2; j++ )
      {
        setFunctionModule( x + j, y + i, Math.max( Math.abs( i ), Math.abs( j ) ) != 1 );
      }
    }
  }

  // Sets the color of a module and marks it as a function module.
  // Only used by the constructor. Coordinates must be in range.
  private void setFunctionModule( int x, int y, boolean isBlack )
  {
    _modules[ y ][ x ] = isBlack;
    _isFunction[ y ][ x ] = true;
  }


	/*---- Private helper methods for constructor: Codewords and masking ----*/

  // Returns a new byte string representing the given data with the appropriate error correction
  // codewords appended to it, based on this object's version and error correction level.
  private byte[] appendErrorCorrection( byte[] data )
  {
    if ( data.length != QrCodeTool.getNumDataCodewords( _version, _errorCorrectionLevel ) )
    {
      throw new IllegalArgumentException();
    }

    // Calculate parameter numbers
    int numBlocks = QrCodeTool.NUM_ERROR_CORRECTION_BLOCKS[ _errorCorrectionLevel.ordinal() ][ _version ];
    int blockEccLen = QrCodeTool.ECC_CODEWORDS_PER_BLOCK[ _errorCorrectionLevel.ordinal() ][ _version ];
    int rawCodewords = QrCodeTool.getNumRawDataModules( _version ) / 8;
    int numShortBlocks = numBlocks - rawCodewords % numBlocks;
    int shortBlockLen = rawCodewords / numBlocks;

    // Split data into blocks and append ECC to each block
    byte[][] blocks = new byte[ numBlocks ][];
    ReedSolomonGenerator rs = new ReedSolomonGenerator( blockEccLen );
    for ( int i = 0, k = 0; i < numBlocks; i++ )
    {
      byte[] dat = Arrays.copyOfRange( data, k, k + shortBlockLen - blockEccLen + ( i < numShortBlocks ? 0 : 1 ) );
      byte[] block = Arrays.copyOf( dat, shortBlockLen + 1 );
      k += dat.length;
      byte[] ecc = rs.getRemainder( dat );
      System.arraycopy( ecc, 0, block, block.length - blockEccLen, ecc.length );
      blocks[ i ] = block;
    }

    // Interleave (not concatenate) the bytes from every block into a single sequence
    byte[] result = new byte[ rawCodewords ];
    for ( int i = 0, k = 0; i < blocks[ 0 ].length; i++ )
    {
      for ( int j = 0; j < blocks.length; j++ )
      {
        // Skip the padding byte in short blocks
        if ( i != shortBlockLen - blockEccLen || j >= numShortBlocks )
        {
          result[ k ] = blocks[ j ][ i ];
          k++;
        }
      }
    }
    return result;
  }

  // Draws the given sequence of 8-bit codewords (data and error correction) onto the entire
  // data area of this QR Code symbol. Function modules need to be marked off before this is called.
  private void drawCodewords( byte[] data )
  {
    Objects.requireNonNull( data );
    if ( data.length != QrCodeTool.getNumRawDataModules( _version ) / 8 )
    {
      throw new IllegalArgumentException();
    }

    int i = 0;  // Bit index into the data
    // Do the funny zigzag scan
    for ( int right = _size - 1; right >= 1; right -= 2 )
    {  // Index of right column in each column pair
      if ( right == 6 )
      {
        right = 5;
      }
      for ( int vert = 0; vert < _size; vert++ )
      {  // Vertical counter
        for ( int j = 0; j < 2; j++ )
        {
          int x = right - j;  // Actual x coordinate
          boolean upward = ( ( right + 1 ) & 2 ) == 0;
          int y = upward ? _size - 1 - vert : vert;  // Actual y coordinate
          if ( !_isFunction[ y ][ x ] && i < data.length * 8 )
          {
            _modules[ y ][ x ] = ( ( data[ i >>> 3 ] >>> ( 7 - ( i & 7 ) ) ) & 1 ) != 0;
            i++;
          }
          // If there are any remainder bits (0 to 7), they are already
          // set to 0/false/white when the grid of modules was initialized
        }
      }
    }
    if ( i != data.length * 8 )
    {
      throw new AssertionError();
    }
  }

  // XORs the data modules in this QR Code with the given mask pattern. Due to XOR's mathematical
  // properties, calling applyMask(m) twice with the same value is equivalent to no change at all.
  // This means it is possible to apply a mask, undo it, and try another mask. Note that a final
  // well-formed QR Code symbol needs exactly one mask applied (not zero, not two, etc.).
  private void applyMask( int mask )
  {
    if ( mask < 0 || mask > 7 )
    {
      throw new IllegalArgumentException( "Mask value out of range" );
    }
    for ( int y = 0; y < _size; y++ )
    {
      for ( int x = 0; x < _size; x++ )
      {
        boolean invert;
        switch ( mask )
        {
          case 0:
            invert = ( x + y ) % 2 == 0;
            break;
          case 1:
            invert = y % 2 == 0;
            break;
          case 2:
            invert = x % 3 == 0;
            break;
          case 3:
            invert = ( x + y ) % 3 == 0;
            break;
          case 4:
            invert = ( x / 3 + y / 2 ) % 2 == 0;
            break;
          case 5:
            invert = x * y % 2 + x * y % 3 == 0;
            break;
          case 6:
            invert = ( x * y % 2 + x * y % 3 ) % 2 == 0;
            break;
          case 7:
            invert = ( ( x + y ) % 2 + x * y % 3 ) % 2 == 0;
            break;
          default:
            throw new AssertionError();
        }
        _modules[ y ][ x ] ^= invert & !_isFunction[ y ][ x ];
      }
    }
  }

  // A messy helper function for the constructors. This QR Code must be in an unmasked state when this
  // method is called. The given argument is the requested mask, which is -1 for auto or 0 to 7 for fixed.
  // This method applies and returns the actual mask chosen, from 0 to 7.
  private int handleConstructorMasking( final int mask )
  {
    int actualMask = mask;
    if ( QrCodeTool.AUTO_MASK == mask )
    {
      // Automatically choose best _mask
      int minPenalty = Integer.MAX_VALUE;
      for ( int i = 0; i < 8; i++ )
      {
        drawFormatBits( i );
        applyMask( i );
        int penalty = getPenaltyScore();
        if ( penalty < minPenalty )
        {
          actualMask = i;
          minPenalty = penalty;
        }
        applyMask( i );  // Undoes the mask due to XOR
      }
    }
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      final int m = actualMask;
      apiInvariant( () -> m < 0 || m > 7, () -> "Invalid mask specified: " + m );
    }

    drawFormatBits( actualMask );  // Overwrite old format bits
    applyMask( actualMask );  // Apply the final choice of mask
    return actualMask;  // The caller shall assign this value to the final-declared field
  }

  // Calculates and returns the penalty score based on state of this QR Code's current modules.
  // This is used by the automatic mask choice algorithm to find the mask pattern that yields the lowest score.
  private int getPenaltyScore()
  {
    int result = 0;

    // Adjacent modules in row having same color
    for ( int y = 0; y < _size; y++ )
    {
      boolean colorX = false;
      for ( int x = 0, runX = 0; x < _size; x++ )
      {
        if ( x == 0 || _modules[ y ][ x ] != colorX )
        {
          colorX = _modules[ y ][ x ];
          runX = 1;
        }
        else
        {
          runX++;
          if ( runX == 5 )
          {
            result += PENALTY_N1;
          }
          else if ( runX > 5 )
          {
            result++;
          }
        }
      }
    }
    // Adjacent modules in column having same color
    for ( int x = 0; x < _size; x++ )
    {
      boolean colorY = false;
      for ( int y = 0, runY = 0; y < _size; y++ )
      {
        if ( y == 0 || _modules[ y ][ x ] != colorY )
        {
          colorY = _modules[ y ][ x ];
          runY = 1;
        }
        else
        {
          runY++;
          if ( runY == 5 )
          {
            result += PENALTY_N1;
          }
          else if ( runY > 5 )
          {
            result++;
          }
        }
      }
    }

    // 2*2 blocks of modules having same color
    for ( int y = 0; y < _size - 1; y++ )
    {
      for ( int x = 0; x < _size - 1; x++ )
      {
        boolean color = _modules[ y ][ x ];
        if ( color == _modules[ y ][ x + 1 ] &&
             color == _modules[ y + 1 ][ x ] &&
             color == _modules[ y + 1 ][ x + 1 ] )
        {
          result += PENALTY_N2;
        }
      }
    }

    // Finder-like pattern in rows
    for ( int y = 0; y < _size; y++ )
    {
      for ( int x = 0, bits = 0; x < _size; x++ )
      {
        bits = ( ( bits << 1 ) & 0x7FF ) | ( _modules[ y ][ x ] ? 1 : 0 );
        if ( x >= 10 && ( bits == 0x05D || bits == 0x5D0 ) )  // Needs 11 bits accumulated
        {
          result += PENALTY_N3;
        }
      }
    }
    // Finder-like pattern in columns
    for ( int x = 0; x < _size; x++ )
    {
      for ( int y = 0, bits = 0; y < _size; y++ )
      {
        bits = ( ( bits << 1 ) & 0x7FF ) | ( _modules[ y ][ x ] ? 1 : 0 );
        if ( y >= 10 && ( bits == 0x05D || bits == 0x5D0 ) )  // Needs 11 bits accumulated
        {
          result += PENALTY_N3;
        }
      }
    }

    // Balance of black and white modules
    int black = 0;
    for ( boolean[] row : _modules )
    {
      for ( boolean color : row )
      {
        if ( color )
        {
          black++;
        }
      }
    }
    int total = _size * _size;
    // Find smallest k such that (45-5k)% <= dark/total <= (55+5k)%
    for ( int k = 0; black * 20 < ( 9 - k ) * total || black * 20 > ( 11 + k ) * total; k++ )
    {
      result += PENALTY_N4;
    }
    return result;
  }

  // For use in getPenaltyScore(), when evaluating which mask is best.
  private static final int PENALTY_N1 = 3;
  private static final int PENALTY_N2 = 3;
  private static final int PENALTY_N3 = 40;
  private static final int PENALTY_N4 = 10;
}
