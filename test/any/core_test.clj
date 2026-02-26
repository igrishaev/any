(ns any.core-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [any.core :as any]))

(deftest test-clojure-entities
  (is (= {:uuid any/uuid
          :string any/string
          :int any/int
          :float any/float
          :keyword any/keyword
          :symbol any/symbol
          :not-nil any/not-nil
          :uuid-string any/uuid-string}
         {:uuid (random-uuid)
          :string (str (random-uuid))
          :int 0
          :float 1.53
          :keyword :foo
          :symbol 'hello
          :not-nil 42
          :uuid-string (str (random-uuid))})))

(deftest test-java-instances
  (is (= {:instant any/Instant
          :date any/Date
          :file any/File}
         {:instant (java.time.Instant/now)
          :date (new java.util.Date)
          :file (clojure.java.io/file "hello")})))

(deftest test-representation
  (is (= "<any string>"
         (pr-str any/string)))
  (is (= "<any integer>"
         (pr-str any/int)))
  (is (= "<any instance of java.time.Instant>"
         (pr-str any/Instant))))

(deftest test-vectors
  (is (= [any/int any/float any/uuid]
         [(rand-int 10) (rand) (random-uuid)])))

(deftest test-enum
  (is (= (any/enum 1 :foo 9 "test")
         9))
  (is (not= (any/enum 1 :foo "test")
         9))
  (is (= "<any of: #{1, :foo, test}>"
         (pr-str (any/enum 1 :foo "test")))))

(deftest test-regex

  (testing "re-matches"
    (is (= (any/re-matches #"\s+foobar\s+")
           "  foobar  "))
    (is (not= (any/re-matches #"\s+foobar\s+")
              "foobar"))
    (is (= "<any string matching \\s+foobar\\s+>"
           (pr-str (any/re-matches #"\s+foobar\s+")))))

  (testing "re-find"
    (is (= (any/re-find #"\d+")
           "  foo42bar  "))
    (is (not= (any/re-find #"\d+")
              "  foobar  "))
    (is (= "<any string including \\s+foobar\\s+>"
           (pr-str (any/re-find #"\s+foobar\s+")))))

  (testing "includes"
    (is (= (any/includes "foo")
           "aa foo bb"))
    (is (not= (any/includes "foo")
              "aa lol bb"))))

(deftest test-range
  (is (= (any/range 1 9) 3))
  (is (= (any/range 1 9) 1))
  (is (not= (any/range 1 9) 9))
  (is (= (pr-str (any/range 1 9))
         "<any number in range [1, 9)>")))
