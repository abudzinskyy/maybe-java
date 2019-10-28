package com.github.maybe.java;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Maybe<T> {

    private static final Maybe<?> EMPTY = new Maybe<>(null, null);

    private final T value;
    private final Exception error;

    private Maybe(T value, Exception error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Maybe<T> of(T value) {
        return new Maybe<>(Objects.requireNonNull(value), null);
    }

    public static <T> Maybe<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    public static <T> Maybe<T> empty() {
        return (Maybe<T>) EMPTY;
    }

    public static <T> Maybe<T> failure(Exception err) {
        return new Maybe<>(null, Objects.requireNonNull(err));
    }

    public Maybe<T> peek(Consumer<T> sideEffect) {
        Objects.requireNonNull(sideEffect);
        if (isPresent()) {
            sideEffect.accept(value);
        }
        return this;
    }

    public Maybe<T> peekError(Consumer<Exception> errorConsumer) {
        Objects.requireNonNull(errorConsumer);
        if (isError()) {
            errorConsumer.accept(error);
        }
        return this;
    }

    public <R> Maybe<R> map(ThrowingFunction<? super T, R, ?> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return (Maybe<R>) this;
        }
        try {
            R apply = mapper.apply(value);
            return Maybe.ofNullable(apply);
        } catch (Exception e) {
            return Maybe.failure(e);
        }
    }

    public <R> Maybe<R> flatMap(ThrowingFunction<? super T, Maybe<R>, ?> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return (Maybe<R>) this;
        }
        try {
            return Objects.requireNonNull(mapper.apply(value));
        } catch (Exception e) {
            return Maybe.failure(e);
        }
    }

    public Maybe<T> mapError(Function<? super Exception, ? extends Exception> mapper) {
        Objects.requireNonNull(mapper);
        if (!isError()) {
            return this;
        }
        return Maybe.failure(mapper.apply(error));
    }

    public Optional<Maybe<T>> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return Optional.of(this);
        }
        return predicate.test(value) ? Optional.of(this) : Optional.empty();
    }

    public Maybe<T> or(Maybe<T> other) {
        return isPresent() ? this : other;
    }

    public Maybe<T> orGet(Supplier<Maybe<T>> other) {
        return isPresent() ? this : other.get();
    }

    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    public T orElseGet(Supplier<T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    public T onError(Function<? super Exception, T> handler) {
        if (isPresent()) {
            return value;
        }
        if (isError()) {
            return handler.apply(error);
        }
        throw new NoSuchElementException("No value present");
    }

    public T get() {
        if (!isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public Throwable getError() {
        if (!isError()) {
            throw new NoSuchElementException("No error present");
        }
        return error;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isError() {
        return error != null;
    }

    public <R> Maybe<R> fold(BooleanSupplier condition,
                             ThrowingFunction<? super T, Maybe<R>, ? extends Exception> falseBranch,
                             ThrowingFunction<? super T, Maybe<R>, ? extends Exception> trueBranch) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(falseBranch);
        Objects.requireNonNull(trueBranch);
        if (!isPresent()) {
            return (Maybe<R>) this;
        }
        try {
            return condition.getAsBoolean() ? trueBranch.apply(get()) : falseBranch.apply(get());
        } catch (Exception e) {
            return Maybe.failure(e);
        }
    }

    private String getClass(T value) {
        return value == null ? "unknown" : value.getClass().getName();
    }

    public interface ThrowingFunction<T, R, E extends Exception> {

        R apply(T t) throws E;

    }
}
