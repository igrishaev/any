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
- [On Equality vs Equivalence](#on-equality-vs-equivalence)
- [Collections](#collections)
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

The library provides a lot of built-in objects for equality: Clojure basic
types, Java types from `java.time.*` and other packages, regex checking, ranges,
and more (see below).

## Why?

For tests, as it helps a lot.

Imagine you're testing a function returning a map:

~~~clojure
(deftest test-some-func
  (is (= {:this 1
          :that [:foo :bar]
          :more {:ok true}}
         (some-function 1 "abc" [:foo]))))
~~~

All fields are constant, so there is nothing to worry about. A regular `=`
operator works fine.

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

## On Equality vs Equivalence

## Collections

## Custom objects

## Other

~~~
©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©
Ivan Grishaev, 2026. © UNLICENSE ©
©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©©
~~~
