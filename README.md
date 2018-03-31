<p align="center"><img src="/docs/logo.png" alt="GWT QR Code" width="120"></p>

# GWT QR Code

[![Build Status](https://secure.travis-ci.org/realityforge/gwt-qr-code.png?branch=master)](http://travis-ci.org/realityforge/gwt-qr-code)
[<img src="https://img.shields.io/maven-central/v/org.realityforge.gwt.qr_code/gwt-qr-code-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.realityforge.gwt.qr_code%22)
[![codecov](https://codecov.io/gh/realityforge/gwt-qr-code/branch/master/graph/badge.svg)](https://codecov.io/gh/realityforge/gwt-qr-code)

The library is a GWT port of [QR-Code-generator](https://github.com/nayuki/QR-Code-generator) that
enables the generation of [QR Codes](https://en.wikipedia.org/wiki/QR_code). This original project aims
"... to be the best, clearest QR Code generator library ... The primary goals are flexible options and
absolute correctness". The best place to read about the original project is the project's
[website](https://www.nayuki.io/page/qr-code-generator-library).

Core features:

* Significantly shorter code but more documentation comments compared to competing libraries
* Supports encoding all 40 versions (sizes) and all 4 error correction levels, as per the QR Code Model 2 standard
* Output formats: Raw modules/pixels of the QR symbol, SVG XML string, HTML5 canvas
* Encodes numeric and special-alphanumeric text in less space than general text

Manual parameters:

* User can specify minimum and maximum version numbers allowed, then library will automatically choose smallest version in the range that fits the data
* User can specify mask pattern manually, otherwise library will automatically evaluate all 8 masks and select the optimal one
* User can specify absolute error correction level, or allow the library to boost it if it doesn't increase the version number
* User can create a list of data segments manually and add ECI segments

## Quick Start

The simplest way to use the library is to add the dependency into the build system. i.e.

<code>
<pre>
    &lt;dependency&gt;
       &lt;groupId&gt;org.realityforge.gwt.widget&lt;/groupId&gt;
       &lt;artifactId&gt;gwt-qr-code-core&lt;/artifactId&gt;
       &lt;version&gt;<img src="https://img.shields.io/maven-central/v/org.realityforge.gwt.qr_code/gwt-qr-code-core.svg?label=latest%20release"/>&lt;/version&gt;
    &lt;/dependency&gt;
</pre>
</code>

Then add the snippet `<inherits name='org.realityforge.gwt.qr_code.QrCode'/>` into the `.gwt.xml` module file. Then
you can generate the svg via:

```java
// Generate a simple text base url QR Code with High error correction
final String svg = QrCodeTool.encodeText( "http://realityforge.org", Ecc.HIGH ).toSvgString( 2 );

// Generate a ‎🎉 QR Code with medium error correction
final String svg = QrCodeTool.encodeText( "\u200E\uD83C\uDF89", Ecc.MEDIUM ).toSvgString( 2 );

// Generate a QrCode using manual process
final QrSegment segment = QrCodeTool.makeNumericSegment( "3141592653589793238462643383" );
final QrCode qrCode = QrCodeTool.encodeSegments( Collections.singletonList( segment ), Ecc.HIGH, 5, 5, 2, false );
for ( int y = 0; y < qrCode.getSize(); y++ )
{
  for ( int x = 0; x < qrCode.getSize(); x++ )
  {
    final boolean isPixelBlack = qrCode.getModule( x, y );
    // paint x, y, isPixelBlack
  }
}
```

# More Information

For the source code and project support please visit the [GitHub project](https://github.com/realityforge/gwt-qr-code).

# Contributing

The library was released as open source so others could benefit from the project. We are thankful for any
contributions from the community. A [Code of Conduct](CODE_OF_CONDUCT.md) has been put in place and
a [Contributing](CONTRIBUTING.md) document is under development.

# License

GWT QR Code Generator is licensed under [MIT License](LICENSE).

# Credit

This library is a reworking of the magnificent [QR-Code-generator](https://github.com/nayuki/QR-Code-generator)
by Nayuki Minase. The changes made the library GWT compatible and made it easier for the GWT compiler to optimize.
Support was also removed for the Kanji optimization. Any bugs are likely caused by the current author while the
credit for the code all goes to the original author.
