# Any

A small helper that saves lines of code in tests. Provides a number of objects
with a custom `equals` method implemetation. Say, the `any.core/uuid` object
equals to any possible UUID ever. The same applies to strings, numbers, and so
on.

## Table of Contents

<!-- toc -->

- [Demo](#demo)
- [Why?](#why)
- [Installation](#installation)
- [Builtin objects](#builtin-objects)
- [Representation](#representation)
- [Equality, Equivalence, and Order](#equality-equivalence-and-order)
- [Custom objects](#custom-objects)
- [Other](#other)

<!-- tocstop -->

## Demo

~~~clojure
(ns demo
  (:require
   [any.core :as any]))

;; positive cases

(= any/uuid (random-uuid)) ;; true
(= any/string "whatever")  ;; true
(= any/keyword :dunno)     ;; true

;; negative cases

(= any/uuid 1)          ;; false
(= any/string nil)      ;; false
(= any/symbol :keyword) ;; false
~~~

What is great, such equality works for nested items in maps, vector and other
collections:

~~~clojure
(= {:id any/uuid :name "Ivan"}
   {:id (random-uuid) :name "Ivan"})

(= [any/uuid any/string]
   (list (random-uuid) "test"))
~~~

The library provides a lot of built-in objects for equality: Clojure basic
types, Java types from `java.time.*` and other packages, regex checking, ranges,
and more (see below).

## Why?

Moslty for tests, as it helps a lot. Imagine you're testing a function returning
a map:

~~~clojure
(deftest test-some-func
  (is (= {:this 1
          :that [:foo :bar]
          :more {:ok true}}
         (some-function 1 "abc" [:foo]))))
~~~

All fields are constant so there is nothing to worry about. A regular `=`
operator works fine.

Should `some-function` return a map with a random UUID, you cannot blindly
compare maps: it won't work. At this moment, various solutions come into
play. You may `dissoc` all the random fiels and compare only the constant part:

~~~clojure
(deftest test-some-func
  (is (= {:this 1
          :that [:foo :bar]
          :more {:ok true}}
         (-> (some-function 1 "abc" [:foo])
             (dissoc :id :created_at)))))
~~~

But you still need to ensure the result has both `:id` and `:created_at`
though. That leads to clumsy code:

~~~clojure
(deftest test-some-func
  (let [result (some-function 1 "abc" [:foo])]
    (is (= {:this 1
            :that [:foo :bar]
            :more {:ok true}}
           (dissoc result :id :created_at)))
    (is (uuid? (:id result)))
    (is (instance? java.time.Instance (:created_at result)))))
~~~

With Any, this could be achieved in a shorter way:

~~~clojure
(deftest test-some-func
  (is (= {:id any/uuid
          :created_at any/Instance
          :this 1
          :that [:foo :bar]
          :more {:ok true}}
         (some-function 1 "abc" [:foo]))))
~~~

This reminds Clojure Spec or Malli approach, yet only partially. Schemas usually
check types but not values. With Any, you obtain both: check values and when
they're random or too complex, check types.

## Installation

Lein:

~~~clojure
[com.github.igrishaev/any "0.1.0-SNAPSHOT"]
~~~

Deps.edn

~~~clojure
com.github.igrishaev/any {:mvn/version "0.1.0-SNAPSHOT"}
~~~

## Builtin objects

Clojure primitives:

- `any/string`
- `any/uuid`
- `any/uuid-string` (a string matching UUID pattern)
- `any/int` (short, integer, long)
- `any/float` (float, double)
- `any/keyword`
- `any/symbol`
- `any/not-nil` (like `some?`)

Parameter-depended objects:

| Example                            | Comment                                               |
|------------------------------------|-------------------------------------------------------|
| `(any/instance SomeClass)`         | check if a value is an instance of `SomeClass`        |
| `(any/enum :foo :bar: :baz)`       | check if a value if one of the variadic arguments     |
| `(any/enum-seq [:foo :bar: :baz])` | like `enum` but accepts a collection of items         |
| `(any/range 1 9)`                  | if a given number is in range `[1, 9)`                |
| `(any/re-matches #"some regex")`   | if a value is a string matching the pattern           |
| `(any/re-find #some regex)`        | if a value is a string where the pattern can be found |
| `(any/includes "substring")`       | check if a string includes a substring                |

Java instances:

- `any/LocalDate`
- `any/LocalDateTime`
- `any/LocalTime`
- `any/OffsetTime`
- `any/OffsetDateTime`
- `any/Instant`
- `any/ZonedDateTime`
- `any/Period`
- `any/File`
- `any/InputStream`
- `any/OutputStream`
- `any/Reader`
- `any/Writer`
- `any/Date`
- `any/UUID`

## Representation

Every Any ojbect has a custom `.toString` and `print-method`
implementations. This makes the output a bit clearer in tests:

~~~clojure
(is (= {:id any/uuid :name "Ivan"}
       {:id 42 :name "Ivan"}))

FAIL in () (form-init4968882668721322291.clj:227)
expected: {:id <any UUID>, :name "Ivan"}
  actual: {:id 42, :name "Ivan"}

(is (= {:result {:data {:created_at any/Instant}}}
       {:result {:data {:created_at "bad string"}}}))

FAIL in () (form-init4968882668721322291.clj:235)
expected: {:result {:data {:created_at <any instance of java.time.Instant>}}}
  actual: {:result {:data {:created_at "bad string"}}}
~~~

## Equality, Equivalence, and Order

Keep in mind that the standard `=` Clojure function relies on equivalence, not
equality. Equivalence is a custom to compare objects in Clojure. Say, an
instance of `ArrayList` and a Clojure list are equivalent if they are of the
same size and each Nth element is equivalent to its counterpart.

In Clojure, you cannot extend nor override equivalence as it's hardocoded in
Java sources. The `.equals` method is a part of it and moslty used as the last
resort. Thus, Any cannot provide objects for maching agains collections.

When comparing Any objects with values, the order matters. Any objects should
stay at the first place:

~~~clojure
;; like this
(= any/text "hello")

;; but not like this
(= "hello" any/text)
~~~

These two forms expand into the following:

~~~clojure
;; like this
(.equals any/text "hello")

;; but not like this
(.equals "hello" any/text )
~~~

In the first case, the `any/text` object has got its own logic checking if the
opposite object is a string. In the second case, the standard `String.equals`
method is called.

## Custom objects

All Any objects are built with a number of macros. Here is how you can reuse
them. The `any/instance` accepts a class and returns an object which equals to
instances of that class only:

~~~clojure
(import 'com.acme.Class)

(def AnyClass
  (any/instance com.acme.Class))

(= AnyClass value)
~~~

The `any` macro accepts a binding symbol, a text representation and a custom
body where you describe the logic of equality:

~~~clojure
(any/any [x "text for (.toString) or (print)"]
  (boolean (some-condition x)))
~~~

## Other

~~~
©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©
Ivan Grishaev, 2026. © UNLICENSE ©
©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©
~~~
