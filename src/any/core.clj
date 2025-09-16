(ns any.core
  (:import
   (java.time LocalDate)
   (java.io Writer))
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
         [x# ^Writer w#]
         (.write w# ^String (str x#)))
       result#)))

(defmacro enum [& items]
  `(any [x# (format "<enum: %s>" (str/join ", " [~@items]))]
     (contains? #{~@items} x#)))

(defmacro instance [Class]
  `(any [x# (format "<instance of %s>" (.getName ~Class))]
     (instance? ~Class x#)))

;; pred
;; range


(defmacro defany [name [other repr] & body]
  `(def ~name (any [~other ~repr] ~@body)))

#_
(defany int [x "any int"]
  (cc/int? x))

(defany uuid [x "any uuid"]
  (cc/uuid? x))

(defany uuid [x "any keyword"]
  (cc/keyword? x))

(defany local-date [x "any LocalDate"]
  (instance? LocalDate x))

#_
(= {:foo (enum :fo :bar :baz)} {:foo :sdfsdf})
