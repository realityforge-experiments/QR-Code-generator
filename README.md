> Project Archived: The code works perfectly well but the author is no longer using the library.

---

<p align="center"><img src="/docs/logo.png" alt="GWT QR Code" width="120"></p>

# GWT QR Code

[![Build Status](https://secure.travis-ci.org/realityforge/gwt-qr-code.svg?branch=master)](http://travis-ci.org/realityforge/gwt-qr-code)
[<img src="https://img.shields.io/maven-central/v/org.realityforge.gwt.qr_code/gwt-qr-code.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.realityforge.gwt.qr_code%22)

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

```xml
<dependency>
   <groupId>org.realityforge.gwt.qr_code</groupId>
   <artifactId>gwt-qr-code</artifactId>
   <version>0.03</version>
</dependency>
```

Then add the snippet `<inherits name='org.realityforge.gwt.qr_code.QrCode'/>` into the `.gwt.xml` module file. Then
you can generate the svg via:

Render a url qr code as SVG:

```java
// Generate a simple text base url QR Code with High error correction
final QrCode qrCode = QrCodeTool.encodeText( "http://realityforge.org", Ecc.HIGH );

// Render the qr code as a svg element
final Element div = DomGlobal.document.createElement( "div" );
div.setAttribute( "style", "width:200px" );
div.innerHTML = qrCode.toSvgString( 2 );
DomGlobal.document.body.appendChild( div );
```

Render a unicode qr code on canvas element:

```java
// Generate a ‎🎉 QR Code with medium error correction
final QrCode qrCode = QrCodeTool.encodeText( "\u200E\uD83C\uDF89", Ecc.MEDIUM );

final HTMLCanvasElement canvas = (HTMLCanvasElement) DomGlobal.document.createElement( "canvas" );
final int border = 2;
// 8 pixels per "module"
final double scale = 8;
// Draw the canvas
qrCode.drawCanvas( scale, border, canvas );
DomGlobal.document.body.appendChild( canvas );

```

Manually render a numeric qr code:

```java
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
