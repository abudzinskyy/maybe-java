maybe-java
=====
This is the Maybe monad for Java.

* [Introduction](#introduction)
* [Reference](#reference)
* [Usage Patterns](#usage-patterns)
* [Contribution](#contribution)

## Introduction

Maybe is implementation inspired by Haskell's [Data.Maybe](http://hackage.haskell.org/package/base-4.11.0.0/docs/Data-Maybe.html)
and [Control.Monad.Except](http://hackage.haskell.org/package/mtl-2.2.2/docs/Control-Monad-Except.html) monads.
Unlike exception-throwing, [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html)
or Either-style approaches, it's purpose is to provide a useful monad around data that may exist or has an error at runtime.

## Reference

 - `Maybe.of`
 - `Maybe.ofNullable`
 - `Maybe.empty`
 - `Maybe.failure`

## Usage Patterns

## Contribution
