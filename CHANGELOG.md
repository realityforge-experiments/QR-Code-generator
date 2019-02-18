# Change Log

### Unreleased

* Upgrade the `org.realityforge.com.google.elemental2` artifacts to version `1.0.0-b17-6897368`.
* Remove deployment from TravisCI infrastructure as it is no longer feasible.

### [v0.03](https://github.com/realityforge/gwt-qr-code/tree/v0.03) (2018-06-18)
[Full Changelog](https://github.com/realityforge/gwt-qr-code/compare/v0.02...v0.03)

### [v0.02](https://github.com/realityforge/gwt-qr-code/tree/v0.02) (2018-04-01)
[Full Changelog](https://github.com/realityforge/gwt-qr-code/compare/v0.01...v0.02)

* Add support for rendering `QrCode` instances to html canvas via `QrCode.drawCanvas(...)`.
* Convert several more invariant checks to use the `org.realityforge.braincheck` library so that they are
  optimized out in production builds.
* Remove unused dependency `org.realityforge.anodoc`
* Remove dependency on the `elemental2` library by explicitly checking the characters that are in values using
  iteration rather than using an instance of javascripts `RegExp`. This has the added advantage that it increased
  the execution speed and could lead to code size decreases when the compiler can statically determine the segment
  types. It also means that the code runs in java without the need to compile to Javascript.
* Add some basic tests that just verify the library produces output. These tests are not rigorous or comprehensive
  but add a minimal level of comfort when making changes to the library.
* Remove unnecessary dependency on GWT2.x libraries.
* Fix title of javadocs generated for project.
* Make sure the generated pom has correct dependencies setup up and a more useful set of metadata regarding issue
  tracker, author, license etc.
* Port the [BitBuffer](https://github.com/nayuki/Fast-QR-Code-generator/blob/master/src/io/nayuki/fastqrcodegen/BitBuffer.java)
  from the `Fast-QR-Code-generator` library.

### [v0.01](https://github.com/realityforge/gwt-qr-code/tree/v0.01) (2018-04-01)
[Full Changelog](https://github.com/realityforge/gwt-qr-code/compare/72921ece2999bd9a5dd6987743280a12a58d2242...v0.01)

 ‎🎉	Initial super-alpha release ‎🎉.
