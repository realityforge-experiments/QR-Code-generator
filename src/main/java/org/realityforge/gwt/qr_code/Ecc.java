package org.realityforge.gwt.qr_code;

/**
 * Represents the error correction level used in a QR Code symbol.
 */
public enum Ecc
{
  // These enum constants must be declared in ascending order of error protection,
  // for the sake of the implicit ordinal() method and values() function.
  LOW( 1 ), MEDIUM( 0 ), QUARTILE( 3 ), HIGH( 2 );

  // In the range 0 to 3 (unsigned 2-bit integer).
  private final int formatBits;

  Ecc( int fb )
  {
    formatBits = fb;
  }

  int getFormatBits()
  {
    return formatBits;
  }
}
