(ns any.core-test
  (:refer-clojure :exclude [random-uuid])
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.walk :as walk]
   [any.core :as any]))

;; for Clojure < 11
(defn random-uuid []
  (java.util.UUID/randomUUID))

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
           (pr-str (any/re-find #"\s+foobar\s+"))))))

(deftest test-range
  (is (= (any/range 1 9) 3))
  (is (= (any/range 1 9) 1))
  (is (not= (any/range 1 9) 9))
  (is (= (pr-str (any/range 1 9))
         "<any number in range [1, 9)>")))

(deftest test-string-cases
  (testing "includes"
    (is (= (any/includes "foo")
           "aa foo bb"))
    (is (not= (any/includes "foo")
              "aa lol bb")))

  (testing "starts with"
    (is (= (any/starts-with "a")
           "aabbcc"))
    (is (not= (any/starts-with "a")
              "AABBCC")))

  (testing "ends with"
    (is (= (any/ends-with "c")
           "aabbcc"))
    (is (not= (any/ends-with "c")
           "AABBCC"))))

(deftest test-collections

  ;; https://github.com/igrishaev/any/pull/2/changes
  (let [numbers (any/any-pred #(and (seqable? %) (every? number? %)) "a coll of numbers")]
    (is (= numbers [1 2 3]))
    (is (not= numbers ["a" 1 2]))
    (is (not= numbers :not-numbers)))

  (testing "count"
    (is (= (any/count 3) [1 2 3]))
    (is (= (any/count 0) nil))
    (is (not= (any/count 3) [1 2 3 4])))

  (testing "collection predicates"
    (is (= any/vector [1 2 3]))
    (is (not= any/vector '(1 2 3)))

    (is (= any/list '(1 2 3)))
    (is (not= any/list [1 2 3]))

    (is (= any/set #{1 2 3}))
    (is (not= any/set [1 2 3]))

    (is (= any/map {:foo 1}))
    (is (not= any/map 42))))

(deftest test-arrays
  (is (= any/bytes (byte-array 32)))
  (is (= "<any instance of [B>" (str any/bytes)))
  (is (= any/ints (int-array 32)))
  (is (= any/shorts (short-array 32)))
  (is (= any/longs (long-array 32)))
  (is (= any/floats (float-array 32)))
  (is (= any/doubles (double-array 32)))
  (is (= any/booleans (boolean-array 32)))
  (is (= any/chars (char-array 32)))
  (is (= any/objects (object-array 32))))

(deftest test-postwalk-preserves-any-objects
  (testing "postwalk identity preserves scalar any objects"
    (is (identical? any/uuid (walk/postwalk identity any/uuid)))
    (is (identical? any/string (walk/postwalk identity any/string)))
    (is (identical? any/int (walk/postwalk identity any/int))))

  (testing "postwalk preserves any objects nested in collections"
    (is (= [any/uuid any/string]
           (walk/postwalk identity [any/uuid any/string])))
    (is (= {:id any/uuid :name any/string}
           (walk/postwalk identity {:id any/uuid :name any/string}))))

  (testing "cider-nrepl deep-sorted-maps simulation"
    (let [deep-sorted-maps
          (fn [m]
            (walk/postwalk
             (fn [x]
               (if (and (map? x) (not (record? x)))
                 (into (sorted-map) x)
                 x))
             m))]
      (is (identical? any/uuid (deep-sorted-maps any/uuid)))
      (is (= {:id any/uuid :name "test"}
             (deep-sorted-maps {:id any/uuid :name "test"}))))))

(comment ;; demo for readme

  (= {:id any/uuid :name "Ivan"}
     {:id (random-uuid) :name "Ivan"})

  (= [any/uuid any/string] (list (random-uuid) "test"))

  (deftest test-some-func
    (let [result (some-function 1 "abc" [:foo])]
      (is (= {:this 1
              :that [:foo :bar]
              :more {:ok true}}
             (dissoc result :id :created_at)))
      (is (uuid? (:id result)))
      (is (instance? java.time.Instance (:created_at result)))))

  (deftest test-some-func
    (is (= {:id any/uuid
            :created_at any/Instance
            :this 1
            :that [:foo :bar]
            :more {:ok true}}
           (some-function 1 "abc" [:foo]))))

  (is (= {:id any/uuid :name "Ivan"}
         {:id 42 :name "Ivan"}))

  (is (= {:result {:data {:created_at any/Instant}}}
         {:result {:data {:created_at "bad string"}}})))
