(ns unpackr.core
  (:import [java.nio ByteBuffer]
           [java.io File FileInputStream]))

(defn as-bytes [l]
  (byte-array (map byte l)))

(defn buffer-from [barr]
  (ByteBuffer/wrap barr))

(def unpack-schema
  {:byte   #(.get %)
   :ubyte  #(bit-and ((unpack-schema :byte) %) 0x00FF)

   :short  #(.getShort %)
   :ushort #(bit-and ((unpack-schema :short) %) 0x0000FFFF)

   :int    #(.getInt %)
   :uint   #(bit-and ((unpack-schema :int) %) 0x00000000FFFFFFFF)

   :rest   #(let [more (byte-array (.remaining %))]
              (.get % more)
              more)})

(defn schema-entry? [fmt]
  (letfn [(in? [l x] (not= (.indexOf l x) -1))]
    (in? (keys unpack-schema) fmt)))

(defn unpack-format [fmt]
  (cond
   (number? fmt) #(let [result (byte-array fmt)] (.get % result) result)
   (schema-entry? fmt) (unpack-schema fmt)
   :else (throw (IllegalArgumentException. (str "Invalid unpack format: " fmt)))))

(defn unpack [fmts barr]
  (let [buffer (buffer-from barr)]
    (map #((unpack-format %) buffer) fmts)))

(defn- unzip [coll]
  (let [pairs (partition 2 coll)]
    [(map first pairs) (map second pairs)]))

(defmacro unpack-let [bindings barr & body]
  (let [[ks vs] (unzip bindings)]
    `(let [~(vec ks) (unpack ~(vec vs) ~barr)]
       ~@body)))