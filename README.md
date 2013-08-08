unpackr
=======

A DSL for making working with byte-arrays palatable in Clojure

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
