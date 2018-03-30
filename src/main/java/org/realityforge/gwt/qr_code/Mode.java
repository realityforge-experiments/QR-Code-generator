package org.realityforge.gwt.qr_code;

import org.realityforge.braincheck.BrainCheckConfig;
import org.realityforge.braincheck.Guards;

/**
 * The mode field of a segment. Immutable. Provides methods to retrieve closely related values.
 */
public enum Mode
{
  NUMERIC( 0x1, 10, 12, 14 ),
  ALPHANUMERIC( 0x2, 9, 11, 13 ),
  BYTE( 0x4, 8, 16, 16 ),
  KANJI( 0x8, 8, 10, 12 ),
  ECI( 0x7, 0, 0, 0 );

  private final int _modeBits;
  private final int[] _numBitsCharCount;

  Mode( final int modeBits, final int... numBitsCharCount )
  {
    _modeBits = modeBits;
    _numBitsCharCount = numBitsCharCount;
  }

  /**
   * Returns the bit width of the segment character count field for this mode object at the specified version number.
   *
   * @param version the version number, which is between 1 to 40, inclusive
   * @return the number of bits for the character count, which is between 8 to 16, inclusive
   * @throws IllegalArgumentException if the version number is out of range
   */
  int numCharCountBits( final int version )
  {
    if ( 1 <= version && version <= 9 )
    {
      return _numBitsCharCount[ 0 ];
    }
    else if ( 10 <= version && version <= 26 )
    {
      return _numBitsCharCount[ 1 ];
    }
    else
    {
      if ( BrainCheckConfig.checkInvariants() )
      {
        Guards.invariant( () -> 27 <= version && version <= 40,
                          () -> "Version number " + version + "out of range" );
      }
      return _numBitsCharCount[ 2 ];
    }
  }

  /**
   * An unsigned 4-bit integer value (range 0 to 15) representing the mode indicator bits for this mode object.
   */
  int getModeBits()
  {
    return _modeBits;
  }
}
