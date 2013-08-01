(ns unpackr.core-test
  (:require [clojure.test :refer :all]
            [unpackr.core :refer :all])
  (:import [org.apache.commons.lang.builder EqualsBuilder]
           [java.nio ByteBuffer]))

(defn ref= [e a]
  (EqualsBuilder/reflectionEquals e a))

(deftest convert-sequence-to-bytes
  (testing "Creates byte array from sequence"
    (is (ref= (byte-array (map byte [1 1 1]))
              (as-bytes [1 1 1])))))

(deftest create-buffer-from-byte-array
  (testing "ByteBuffer wraps byte array"
    (let [barr (byte-array 10)]
      (is (ref= (ByteBuffer/wrap barr) (buffer-from barr))))))

(deftest unpack-bytes
  (testing "Unpacked bytes are treated"
    (testing "as signed byte"
      (is (= [-1] (unpack [:byte] (as-bytes [-1])))))

    (testing "as unsigned byte"
      (is (= [255] (unpack [:ubyte] (as-bytes [-1])))))

    (testing "as signed short"
      (is (= [-256] (unpack [:short] (as-bytes [-1 0])))))

    (testing "as unsigned short"
      (is (= [65280] (unpack [:ushort] (as-bytes [-1 0])))))

    (testing "as signed int"
      (is (= [-16777216] (unpack [:int] (as-bytes [-1 0 0 0])))))

    (testing "as unsigned int"
      (is (= [4278190080] (unpack [:uint] (as-bytes [-1 0 0 0])))))

    (testing "as another byte array"
      (is (ref= (as-bytes [1 2 3]) (first (unpack [:rest] (as-bytes [1 2 3])))))))

  (testing "Bytes can be iteratively unpacked into multiple formats"
    (let [[b s i more] (unpack [:byte :short :int :rest] (as-bytes (range 10)))]
      (is (= 0 b))
      (is (= 258 s))
      (is (= 50595078 i))
      (is (ref= (as-bytes [7 8 9]) more)))))

(deftest ensure-unpack-format-is-valid
  (testing "Invalid unpack format raises IllegalArgumentException"
    (is (thrown? IllegalArgumentException (ensure-unpack-format! :invalid)))))