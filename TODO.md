# TODO

This document is essentially a list of shorthand notes describing work yet to completed.
Unfortunately it is not complete enough for other people to pick work off the list and
complete as there is too much un-said.

* Port parts of the [Fast QR Code generator library](https://www.nayuki.io/page/fast-qr-code-generator-library)
  across to this library. The purpose is partially to improve performance but mostly to reduce generated code
  size. Particulars involve:
  - Port [BitBuffer](https://github.com/nayuki/Fast-QR-Code-generator/blob/master/src/io/nayuki/fastqrcodegen/BitBuffer.java)

* Implement Canvas rendering

* Add some basic tests that just verify the library produces what it currently produces
