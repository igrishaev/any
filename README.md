# Any

A helper to saves lines of code in tests. Provides a number of objects with a
custom `equals` method. Say, the `any.core/uuid` object equals to any UUID. The
same approach for strings, numbers, and so on.

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

Custom equality works for nested items in maps, vectors and other collections:

~~~clojure
(= {:id any/uuid :name "Ivan"}
   {:id (random-uuid) :name "Ivan"})

(= [any/uuid any/string]
   (list (random-uuid) "test"))
~~~

The library provides various objects for equality: Clojure primitives, Java
classes from `java.time.*` and other packages, regex checking, ranges, and more
(see below).

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
compare maps with `=`. At this moment, various solutions come into play. You may
`dissoc` all random fiels and compare only the constant subpart:

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

Any achieves the same in a shorter way:

~~~clojure
(deftest test-some-func
  (is (= {:id any/uuid
          :created_at any/Instance
          :this 1
          :that [:foo :bar]
          :more {:ok true}}
         (some-function 1 "abc" [:foo]))))
~~~

This reminds Clojure Spec or Malli, yet partially. Schemas usually check types
but not values. With Any, you obtain both: check values and when they're random
or too complex, rollback to types.

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

| Object                             | Comment                                               |
|------------------------------------|-------------------------------------------------------|
| `(any/instance SomeClass)`         | check if a value is an instance of `SomeClass`        |
| `(any/enum :foo :bar: :baz)`       | check if a value if one of the variadic arguments     |
| `(any/enum-seq [:foo :bar: :baz])` | like `enum` but accepts a collection of items         |
| `(any/range 1 9)`                  | if a given number is in range `[1, 9)`                |
| `(any/re-matches #"some regex")`   | if a value is a string matching the pattern           |
| `(any/re-find #some regex)`        | if a value is a string where the pattern can be found |
| `(any/includes "substring")`       | check if a string includes a substring                |

Examples:

~~~clojure
(= (any/range 1 9) 3)            ;; true
(= (any/range 1 9) 9)            ;; false
(= (any/enum 1 :foo 9 "test") 9) ;; true
(= (any/re-matches #"\s+foobar\s+") "  foobar  ") ;; true
(= (any/re-find #"\d+") "  foo42bar  ")           ;; true
(= (any/includes "foo") "aa foo bb")              ;; true
~~~

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

Every Any object has a custom `.toString` and `print-method`
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
equality. Equivalence is a custom way to compare objects in Clojure. Say,
instances of `ArrayList` and `PersistentList` are equivalent if they are of the
same size and each Nth element is equivalent to its counterpart.

In Clojure, you cannot extend nor override equivalence as it's hardcoded in Java
sources. The `.equals` method is a part of equivalence and is mostly used as the
last resort. For this reason, Any doesn't provide objects for matching
collections: it doesn't work.

When comparing Any objects with values, the order matters. Objects provided by
Any should stay first:

~~~clojure
;; like this
(= any/text "hello")

;; NOT like this
(= "hello" any/text)
~~~

These two forms expand into the following:

~~~clojure
;; like this
(.equals any/text "hello")

;; NOT like this
(.equals "hello" any/text )
~~~

In the first case, the `any/text` object has got its own logic checking if the
opposite object is a string. In the second case, the standard `String.equals`
method is called.

## Custom objects

Any objects are built with a number of utility macros, and you can reuse
them. The `any/instance` accepts a class and returns an object which equals to
instances of that class only:

~~~clojure
(import 'com.acme.Class)

(def AnyClass
  (any/instance com.acme.Class))

(= AnyClass value)
~~~

The `any` macro accepts a binding symbol, a text representation and a custom
body with the logic of equality:

~~~clojure
(any/any [x "text for (.toString) or (print)"]
  (boolean (some-condition x)))
~~~

The body should always return true or false, but not `nil`. It's better to wrap
the result with `boolean` to prevent such cases.

## Other

~~~
©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©
Ivan Grishaev, 2026. © UNLICENSE ©
©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©
~~~
