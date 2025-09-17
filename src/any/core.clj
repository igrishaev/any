(ns any.core
  (:require
   [clojure.string :as str])
  (:refer-clojure :exclude [uuid int]))

(alias 'cc 'clojure.core)

(set! *warn-on-reflection* true)

(defmacro any [[other repr] & body]
  (let [this (gensym "this")]
    `(let [result#
           (reify Object
             (equals [~this ~other]
               ~@body)
             ~@(when repr
                 [`(toString [~this] ~repr)]))]
       (defmethod print-method (type result#)
         [x# ^java.io.Writer w#]
         (.write w# ^String (str x#)))
       result#)))

(defmacro enum [& items]
  `(any [x# (format "<enum: %s>" (str/join ", " [~@items]))]
     (contains? #{~@items} x#)))

(defmacro instance [Class]
  `(any [x# (format "<instance: %s>" (.getName ~Class))]
     (instance? ~Class x#)))

(defmacro pred [fn-name]
  `(any [x# (format "<pred: %s>" ~(str fn-name))]
     (~fn-name x#)))

;; pred
;; range

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

#_
(intern 'any.core
        'Number
        (instance java.lang.Number))

#_
(intern 'any.core
        'String
        (instance java.lang.String))




;; (defmacro defany [name [other repr] & body]
;;   `(def ~name (any [~other ~repr] ~@body)))

;; #_
;; (defany int [x "any int"]
;;   (cc/int? x))

;; (defany uuid [x "any uuid"]
;;   (cc/uuid? x))

;; (defany uuid [x "any keyword"]
;;   (cc/keyword? x))

;; (defany local-date [x "any LocalDate"]
;;   (instance? LocalDate x))

#_
(= {:foo (enum :fo :bar :baz)} {:foo :sdfsdf})
