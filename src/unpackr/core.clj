(ns unpackr.core
  (:import [java.nio ByteBuffer]))

(def ^:private unpack-schema
  {:byte   #(.get %)
   :ubyte  #(bit-and ((unpack-schema :byte) %) 0x00FF)

   :short  #(.getShort %)
   :ushort #(bit-and ((unpack-schema :short) %) 0x0000FFFF)

   :int    #(.getInt %)
   :uint   #(bit-and ((unpack-schema :int) %) 0x00000000FFFFFFFF)

   :rest   #(let [more (byte-array (.remaining %))]
              (.get % more)
              more)})

(defn- schema-entry? [fmt]
  (letfn [(in? [l x] (not= (.indexOf l x) -1))]
    (in? (keys unpack-schema) fmt)))

(defn- unpack-format [fmt]
  (cond
   (number? fmt) #(let [result (byte-array fmt)] (.get % result) result)
   (schema-entry? fmt) (unpack-schema fmt)
   :else (throw (IllegalArgumentException. (str "Invalid unpack format: " fmt)))))

(defn- unzip [coll]
  (let [pairs (partition 2 coll)]
    [(map first pairs) (map second pairs)]))

(defn as-bytes
  "Creates a byte-array from a seq of numbers.

   Note that all numbers must be between -128 and 127 inclusive, or else an ```IllegalArgumentException``` is thrown."
  [l]
  (byte-array (map byte l)))

(defn buffer-from
  "Wraps a byte array with a ```java.nio.ByteBuffer```"
  [barr]
  (ByteBuffer/wrap barr))

(defn unpack
  "Unpacks values corresponding to the specified formats from the byte-array.

  Valid formats are:

  ```:byte```      - Extract the next byte as a signed byte

  ```:ubyte```     - Extract the next byte as an unsigned byte

  ```:short```     - Extract the next 2 bytes as a signed short

  ```:ushort```    - Extract the next 2 bytes as an unsigned short

  ```:int```       - Extract the next 4 bytes as a signed int

  ```:uint```      - Extract the next 4 bytes as an unsigned int

  ```:rest```      - Extract the remaining bytes as a byte array

  ```n (number)``` - Extract the next ```n``` bytes as a byte array

  Example usage:

    (unpack [:short :uint 10] (as-bytes (range 100)))
    ;-> [1 33752069 #<byte[] [B@3761cf73>]```"
  [fmts barr]
  (let [buffer (buffer-from barr)]
    (map #((unpack-format %) buffer) fmts)))

(defmacro unpack-let
  "Unpacks values and immediately binds them to specified vars in a let clause.

  Example usage:

    (unpack-let [x :short y :int] (as-bytes [0 1 0 0 0 10])
      (+ x y))
    ;-> 11
  "
  [bindings barr & body]
  (let [[ks vs] (unzip bindings)]
    `(let [~(vec ks) (unpack ~(vec vs) ~barr)]
       ~@body)))
