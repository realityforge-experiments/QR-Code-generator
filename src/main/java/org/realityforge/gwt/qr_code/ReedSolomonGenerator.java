package org.realityforge.gwt.qr_code;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * Computes the Reed-Solomon error correction codewords for a sequence of data codewords
 * at a given degree. Objects are immutable, and the state only depends on the degree.
 * This class exists because each data block in a QR Code shares the same the divisor polynomial.
 */
final class ReedSolomonGenerator
{
  // Coefficients of the divisor polynomial, stored from highest to lowest power, excluding the leading term which
  // is always 1. For example the polynomial x^3 + 255x^2 + 8x + 93 is stored as the uint8 array {255, 8, 93}.
  private final byte[] _coefficients;

  /**
   * Creates a Reed-Solomon ECC generator for the specified degree. This could be implemented
   * as a lookup table over all possible parameter values, instead of as an algorithm.
   *
   * @param degree the divisor polynomial degree, which must be between 1 and 255
   * @throws IllegalArgumentException if degree &lt; 1 or degree > 255
   */
  ReedSolomonGenerator( final int degree )
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> !( degree < 1 || degree > 255 ), () -> "Degree out of range" );
    }

    // Start with the monomial x^0
    _coefficients = new byte[ degree ];
    _coefficients[ degree - 1 ] = 1;

    // Compute the product polynomial (x - r^0) * (x - r^1) * (x - r^2) * ... * (x - r^{degree-1}),
    // drop the highest term, and store the rest of the _coefficients in order of descending powers.
    // Note that r = 0x02, which is a generator element of this field GF(2^8/0x11D).
    int root = 1;
    for ( int i = 0; i < degree; i++ )
    {
      // Multiply the current product by (x - r^i)
      for ( int j = 0; j < _coefficients.length; j++ )
      {
        _coefficients[ j ] = (byte) multiply( _coefficients[ j ] & 0xFF, root );
        if ( j + 1 < _coefficients.length )
        {
          _coefficients[ j ] ^= _coefficients[ j + 1 ];
        }
      }
      root = multiply( root, 0x02 );
    }
  }

  /**
   * Computes and returns the Reed-Solomon error correction codewords for the specified
   * sequence of data codewords. The returned object is always a new byte array.
   * This method does not alter this object's state (because it is immutable).
   *
   * @param data the sequence of data codewords
   * @return the Reed-Solomon error correction codewords
   * @throws NullPointerException if the data is {@code null}
   */
  @Nonnull
  byte[] getRemainder( @Nonnull final byte[] data )
  {
    // Compute the remainder by performing polynomial division
    final byte[] result = new byte[ _coefficients.length ];
    for ( byte b : Objects.requireNonNull( data ) )
    {
      int factor = ( b ^ result[ 0 ] ) & 0xFF;
      System.arraycopy( result, 1, result, 0, result.length - 1 );
      result[ result.length - 1 ] = 0;
      for ( int i = 0; i < result.length; i++ )
      {
        result[ i ] ^= multiply( _coefficients[ i ] & 0xFF, factor );
      }
    }
    return result;
  }

  // Returns the product of the two given field elements modulo GF(2^8/0x11D). The arguments and result
  // are unsigned 8-bit integers. This could be implemented as a lookup table of 256*256 entries of uint8.
  private static int multiply( final int x, final int y )
  {
    if ( BrainCheckConfig.checkApiInvariants() )
    {
      apiInvariant( () -> !( x >>> 8 != 0 || y >>> 8 != 0 ), () -> "Byte out of range" );
    }
    // Russian peasant multiplication
    int z = 0;
    for ( int i = 7; i >= 0; i-- )
    {
      z = ( z << 1 ) ^ ( ( z >>> 7 ) * 0x11D );
      z ^= ( ( y >>> i ) & 1 ) * x;
    }
    final int result = z;
    if ( BrainCheckConfig.checkInvariants() )
    {
      invariant( () -> result >>> 8 == 0, () -> "Invalid value for result" );
    }
    return result;
  }
}
