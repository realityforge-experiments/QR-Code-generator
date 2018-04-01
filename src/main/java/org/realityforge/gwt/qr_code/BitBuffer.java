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
import java.util.Objects;
import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * An appendable sequence of bits (0's and 1's).
 */
public final class BitBuffer
{
  private int[] _data = new int[ 64 ];
  private int _bitLength;

  /**
   * Returns the length of this sequence, which is a non-negative value.
   *
   * @return the length of this sequence
   */
  public int getBitLength()
  {
    return _bitLength;
  }

  /**
   * Returns the bit at the specified index, yielding 0 or 1.
   *
   * @param index the index to get the bit at
   * @return the bit at the specified index
   * @throws IndexOutOfBoundsException if index &lt; 0 or index &ge; _bitLength
   */
  public int getBit( int index )
  {
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> !( index < 0 || index >= _bitLength ), () -> "Index Out Of Bounds" );
    }
    return ( _data[ index >>> 5 ] >>> ~index ) & 1;
  }

  public int[] getData()
  {
    return _data;
  }

  /**
   * Packs this buffer's bits into bytes in big endian,
   * padding with '0' bit values, and returns the new array.
   *
   * @return this sequence as a new array of bytes (not {@code null})
   */
  @Nonnull
  public byte[] getBytes()
  {
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> _bitLength % 8 == 0, () -> "Data is not a whole number of bytes" );
    }
    final byte[] result = new byte[ _bitLength / 8 ];
    for ( int i = 0; i < result.length; i++ )
    {
      result[ i ] = (byte) ( _data[ i >>> 2 ] >>> ( ~i << 3 ) );
    }
    return result;
  }

  /**
   * Appends the specified number of low bits of the specified value
   * to this sequence. Requires 0 &le; val &lt; 2<sup>len</sup>.
   *
   * @param value the value to append
   * @param len the number of low bits in the value to take
   */
  public void appendBits( final int value, final int len )
  {
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> !( len < 0 || len > 31 || value >>> len != 0 ), () -> "Value out of range" );
    }
    int v = value;
    int l = len;
    if ( _bitLength + l + 1 > _data.length << 5 )
    {
      _data = Arrays.copyOf( _data, _data.length * 2 );
    }
    assert _bitLength + l <= _data.length << 5;

    int remain = 32 - ( _bitLength & 0x1F );
    assert 1 <= remain && remain <= 32;
    if ( remain < l )
    {
      _data[ _bitLength >>> 5 ] |= v >>> ( l - remain );
      _bitLength += remain;
      assert ( _bitLength & 0x1F ) == 0;
      l -= remain;
      v &= ( 1 << l ) - 1;
      remain = 32;
    }
    _data[ _bitLength >>> 5 ] |= v << ( remain - l );
    _bitLength += l;
  }

  /**
   * Appends the bit _data of the specified segment to this bit buffer.
   *
   * @param vals the data to append
   */
  public void appendData( @Nonnull final int[] vals, final int len )
  {
    Objects.requireNonNull( vals );
    if ( len == 0 )
    {
      return;
    }
    if ( len < 0 || len > vals.length * 32 )
    {
      throw new IllegalArgumentException( "Value out of range" );
    }
    int wholeWords = len / 32;
    int tailBits = len % 32;
    if ( tailBits > 0 && vals[ wholeWords ] << tailBits != 0 )
    {
      throw new IllegalArgumentException( "Last word must have low bits clear" );
    }

    while ( _bitLength + len > _data.length * 32 )
    {
      _data = Arrays.copyOf( _data, _data.length * 2 );
    }

    int shift = _bitLength % 32;
    if ( shift == 0 )
    {
      System.arraycopy( vals, 0, _data, _bitLength / 32, ( len + 31 ) / 32 );
      _bitLength += len;
    }
    else
    {
      for ( int i = 0; i < wholeWords; i++ )
      {
        int word = vals[ i ];
        _data[ _bitLength >>> 5 ] |= word >>> shift;
        _bitLength += 32;
        _data[ _bitLength >>> 5 ] = word << ( 32 - shift );
      }
      if ( tailBits > 0 )
      {
        appendBits( vals[ wholeWords ] >>> ( 32 - tailBits ), tailBits );
      }
    }
  }
}
