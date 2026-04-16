(ns any.core
  (:require
   [clojure.string :as str])
  (:import
   (clojure.lang IPersistentCollection))
  (:refer-clojure :exclude [uuid int keyword symbol
                            count float
                            bytes ints floats doubles longs
                            booleans chars shorts objects
                            vector list set map
                            re-matches re-find range]))

(alias 'cc 'clojure.core)

(set! *warn-on-reflection* true)

(defmacro any [[other repr] & body]
  (let [this (gensym "this")]
    `(let [result#
           (reify
             IPersistentCollection
             (equiv [~this ~other]
               ~@body)
             (empty [~this]
               nil)
             (seq [~this]
               nil)
             ~@(when repr
                 [`(toString [~this] ~repr)]))]
       (defmethod print-method (type result#)
         [x# ^java.io.Writer w#]
         (.write w# ^String (str x#)))
       result#)))

(defmacro any-pred [pred repr]
  `(any [o# ~repr]
     (~pred o#)))

(defmacro instance [Class]
  `(any [repr# (format "<any instance of %s>" (.getName ~Class))]
     (instance? ~Class repr#)))

;; enums

(defn enum-seq [items]
  (let [-set (cc/set items)]
    (let [repr (format "<any of: #{%s}>" (str/join ", " items))]
      (any [o repr]
        (contains? -set o)))))

(defn enum [& items]
  (enum-seq items))

;; Clojure predicates

(def string
  (any-pred cc/string? "<any string>"))

(def uuid
  (any-pred cc/uuid? "<any UUID>"))

(def uuid-string
  (any [o "<any string UUID>"]
    (if (cc/string? o)
      (try
        (java.util.UUID/fromString o)
        true
        (catch Exception e
          false))
      false)))

(defn range [from to]
  (any [o (format "<any number in range [%s, %s)>" from to)]
    (if (cc/number? o)
      (and (<= from o) (< o to)))))

(defn re-matches [re-pattern]
  (any [o (format "<any string matching %s>" re-pattern)]
    (if (cc/string? o)
      (some? (cc/re-matches re-pattern o))
      false)))

(defn re-find [re-pattern]
  (any [o (format "<any string including %s>" re-pattern)]
    (if (cc/string? o)
      (some? (cc/re-find re-pattern o))
      false)))

(defn includes [substring]
  (any [o (format "<any string including '%s'>" substring)]
    (if (cc/string? o)
      (str/includes? o substring)
      false)))

(defn starts-with [substring]
  (any [o (format "<string starting with '%s'>" substring)]
    (if (cc/string? o)
      (str/starts-with? o substring)
      false)))

(defn ends-with [substring]
  (any [o (format "<string ending with '%s'>" substring)]
    (if (cc/string? o)
      (str/ends-with? o substring)
      false)))

(def int
  (any-pred cc/int? "<any integer>"))

(def float
  (any-pred cc/float? "<any float>"))

(def keyword
  (any-pred cc/keyword? "<any keyword>"))

(def symbol
  (any-pred cc/symbol? "<any symbol>"))

(def not-nil
  (any-pred cc/some? "<any non-nil"))

;; java classes

(def LocalDate
  (instance java.time.LocalDate))

(def LocalDateTime
  (instance java.time.LocalDateTime))

(def LocalTime
  (instance java.time.LocalTime))

(def OffsetTime
  (instance java.time.OffsetTime))

(def OffsetDateTime
  (instance java.time.OffsetDateTime))

(def Instant
  (instance java.time.Instant))

(def ZonedDateTime
  (instance java.time.ZonedDateTime))

(def Period
  (instance java.time.Period))

(def File
  (instance java.io.File))

(def InputStream
  (instance java.io.InputStream))

(def OutputStream
  (instance java.io.OutputStream))

(def Reader
  (instance java.io.Reader))

(def Writer
  (instance java.io.Writer))

(def Date
  (instance java.util.Date))

(def UUID
  (instance java.util.UUID))

;; collections

(defn count [n]
  (let [repr (format "<count %s>" n)]
    (any [o repr]
      (-> o cc/count (= n)))))

(def vector
  (any-pred cc/vector? "<any vector>"))

(def list
  (any-pred cc/list? "<any list>"))

(def set
  (any-pred cc/set? "<any set>"))

(def map
  (any-pred cc/map? "<any map>"))

;; arrays

(def ^Class ARRAY_BOOL
  (Class/forName "[Z"))

(def ^Class ARRAY_BYTE
  (Class/forName "[B"))

(def ^Class ARRAY_CHAR
  (Class/forName "[C"))

(def ^Class ARRAY_DOUBLE
  (Class/forName "[D"))

(def ^Class ARRAY_FLOAT
  (Class/forName "[F"))

(def ^Class ARRAY_INT
  (Class/forName "[I"))

(def ^Class ARRAY_SHORT
  (Class/forName "[S"))

(def ^Class ARRAY_LONG
  (Class/forName "[J"))

(def ^Class ARRAY_OBJ
  (Class/forName "[Ljava.lang.Object;"))

(def bytes
  (instance ARRAY_BYTE))

(def ints
  (instance ARRAY_INT))

(def shorts
  (instance ARRAY_SHORT))

(def longs
  (instance ARRAY_LONG))

(def floats
  (instance ARRAY_FLOAT))

(def doubles
  (instance ARRAY_DOUBLE))

(def booleans
  (instance ARRAY_BOOL))

(def chars
  (instance ARRAY_CHAR))

(def objects
  (instance ARRAY_OBJ))
