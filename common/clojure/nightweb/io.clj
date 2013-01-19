(ns nightweb.io
  (:use [clojure.java.io :only [file
                                input-stream
                                output-stream]]
        [nightweb.constants :only [slash get-posts-dir]]))

; basic file operations

(defn file-exists?
  [path]
  (.exists (file path)))

(defn write-file
  [path data-barray]
  (if-let [parent-dir (.getParentFile (file path))]
    (.mkdirs parent-dir))
  (with-open [bos (output-stream path)]
    (.write bos data-barray 0 (alength data-barray))))

(defn read-file
  [path]
  (let [length (.length (file path))
        data-barray (byte-array length)]
    (with-open [bis (input-stream path)]
      (.read bis data-barray))
    data-barray))

; encodings/decodings

(defn b-encode
  [data-map]
  (org.klomp.snark.bencode.BEncoder/bencode data-map))

(defn b-decode
  [data-barray]
  (try
    (.getMap (org.klomp.snark.bencode.BDecoder/bdecode
               (java.io.ByteArrayInputStream. data-barray)))
    (catch java.lang.Exception e nil)))

(defn base32-encode
  [data-barray]
  (net.i2p.data.Base32/encode data-barray))

(defn base32-decode
  [data-str]
  (net.i2p.data.Base32/decode data-str))

; read/write specific files

(defn write-key-file
  [file-path key-data]
  (write-file file-path (b-encode {"sign-key" key-data
                                   "sign-algo" "DSA-SHA1"})))

(defn read-key-file
  [file-path]
  (let [priv-key-map (b-decode (read-file file-path))
        sign-key-str (.get priv-key-map "sign-key")]
    (.getBytes sign-key-str)))

(defn write-post-file
  [dir-path user-hash text]
  (write-file (str dir-path
                   (get-posts-dir user-hash)
                   slash (.getTime (java.util.Date.)) ".post")
              (b-encode {"text" text})))