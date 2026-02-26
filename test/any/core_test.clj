(ns any.core-test
  (:require
   [clojure.test :refer [deftest is]]
   [any.core :as any]))

(deftest test-clojure-entities
  (is (= {:uuid any/uuid
          :string any/string
          :int any/int
          :float any/float
          :keyword any/keyword
          :symbol any/symbol
          :not-nil any/not-nil
          :uuid-string any/uuid-string

          }
         {:uuid (random-uuid)
          :string (str (random-uuid))
          :int 0
          :float 1.53
          :keyword :foo
          :symbol 'hello
          :not-nil 42
          :uuid-string (str (random-uuid))

          })
      )
)

;; test repr
;; test instances
;; test other clojure predicates
