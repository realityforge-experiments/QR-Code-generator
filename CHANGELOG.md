# Change Log

### Unreleased

* Convert several more invariant checks to use the `org.realityforge.braincheck` library so that they are
  optimized out in production builds.
* Remove unused dependency `org.realityforge.anodoc`
* Remove dependency on the `elemental2` library by explicitly checking the characters that are in values using
  iteration rather than using an instance of javascripts `RegExp`. This has the added advantage that it increased
  the execution speed and could lead to code size decreases when the compiler can statically determine the segment
  types. It also means that the code runs in java without the need to compile to Javascript.
* Add some basic tests that just verify the library produces output. These tests are not rigorous or comprehensive
  but add a minimal level of comfort when making changes to the library.

### [v0.01](https://github.com/realityforge/gwt-qr-code/tree/v0.01) (2018-04-01)
[Full Changelog](https://github.com/realityforge/gwt-qr-code/compare/72921ece2999bd9a5dd6987743280a12a58d2242...v0.01)

 ‎🎉	Initial super-alpha release ‎🎉.
