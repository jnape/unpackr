unpackr [![Build Status](https://travis-ci.org/jnape/unpackr.png?branch=master)](https://travis-ci.org/jnape/unpackr)
=======

A DSL for making working with byte-arrays palatable in Clojure

installation
------------

Add the following dependency to your `project.clj` file:

    [unpackr "0.1.0-SNAPSHOT"]

examples
--------

Extract a signed short, followed by an unsigned int, followed by the remaining bytes from a byte array:

```clojure
; The normal way
(let [buffer (java.nio.ByteBuffer/wrap (byte-array (map byte (range 100))))]
  (let [a (.getShort buffer) b (bit-and (.getInt buffer) 0x00000000FFFFFFFF) c (byte-array (.remaining buffer))]
    (.get buffer c)
    [a b c]))

; Using unpackr
(unpack [:short :uint :rest] (as-bytes (range 100)))
```

Extract an unsigned short, followed by that many bytes ahead, followed by an unsigned byte:

```clojure
; The normal way
(let [buffer (java.nio.ByteBuffer/wrap (byte-array (map byte (range 100))))]
  (let [a (bit-and (.getShort buffer) 0x0000FFFF) b (byte-array a)]
    (.get buffer b)
    [a b (bit-and (.get buffer) 0x00FF)]))

; Using unpackr
(unpack-let [a :ushort more :rest] (as-bytes (range 100))
  (unpack-let [b a c :ubyte] more
    [a b c]))
```

documentation
-------------

The full API documentation of unpackr can be found at http://jnape.github.io/unpackr/

license
-------

unpackr is distributed under the [Eclipse Public License](http://choosealicense.com/licenses/eclipse/), the same as Clojure.
