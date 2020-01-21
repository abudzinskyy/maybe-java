package com.github.maybe.java;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A container object which may exist value or has en error at runtime
 * If a value is present, {@code isPresent()} will return {@code true} and
 * {@code get()} will return the value.
 */
public final class Maybe<T> {

    /**
     * Common instance for {@code empty()}.
     */
    private static final Maybe<?> EMPTY = new Maybe<>();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    /**
     * If non-null, the error; if null, indicates no error is present
     */
    private final Exception error;

    /**
     * Constructs an empty instance.
     */
    private Maybe() {
        this.value = null;
        this.error = null;
    }

    /**
     * Constructs an instance with the value.
     *
     * @param value the value to be present
     * @param error the error to be present
     */
    private Maybe(T value, Exception error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Returns a {@code Maybe} with the specified present non-null value.
     *
     * @param <T>   the class of the value
     * @param value the value to be present, which must be non-null
     * @return a {@code Maybe} with the value present
     * @throws NullPointerException if value is null
     */
    public static <T> Maybe<T> of(T value) {
        return new Maybe<>(Objects.requireNonNull(value), null);
    }

    /**
     * Returns a {@code Maybe} describing the specified value, if non-null,
     * otherwise returns an empty {@code Maybe}.
     *
     * @param <T>   the class of the value
     * @param value the possibly-null value to describe
     * @return a {@code Maybe} with a present value if the specified value
     * is non-null, otherwise an empty {@code Maybe}
     */
    public static <T> Maybe<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    /**
     * Returns an empty {@code Maybe} instance.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code Maybe}
     */
    public static <T> Maybe<T> empty() {
        @SuppressWarnings("unchecked")
        Maybe<T> empty = (Maybe<T>) EMPTY;
        return empty;
    }

    /**
     * Returns an {@code Maybe} with the specified present non-null error.
     *
     * @param <T>   the class of the value
     * @param error the error to be present, which must be non-null
     * @return a {@code Maybe} with the error present
     * @throws NullPointerException if error is null
     */
    public static <T> Maybe<T> failure(Exception error) {
        return new Maybe<>(null, Objects.requireNonNull(error));
    }

    /**
     * If a value is present in this {@code Maybe}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code Maybe}
     * @throws NoSuchElementException if there is no value present
     * @see Maybe#isPresent()
     */
    public T get() {
        if (!isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * If an error is present in this {@code Maybe}, returns the error,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null error held by this {@code Maybe}
     * @throws NoSuchElementException if there is no error present
     * @see Maybe#isError()
     */
    public Throwable getError() {
        if (!isError()) {
            throw new NoSuchElementException("No error present");
        }
        return error;
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Return {@code true} if there is an error present, otherwise {@code false}.
     *
     * @return {@code true} if there is an error present, otherwise {@code false}
     */
    public boolean isError() {
        return error != null;
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is null
     */
    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null)
            consumer.accept(value);
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * return a {@code Maybe} describing the value, otherwise return an
     * empty {@code Maybe}.
     *
     * @param predicate a predicate to apply to the value, if present
     * @return a {@code Maybe} describing the value of this {@code Maybe}
     * if a value is present and the value matches the given predicate,
     * otherwise an empty {@code Maybe}
     * @throws NullPointerException if the predicate is null
     */
    public Maybe<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        }
        return predicate.test(value) ? this : Maybe.empty();
    }

    /**
     * If a value is present, apply the provided mapping function to it,
     * and if the result is non-null, return a {@code Maybe} describing the
     * result. Otherwise return a failure {@code Maybe}.
     *
     * @param <R>    The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return a {@code Maybe} describing the result of applying a mapping
     * function to the value of this {@code Maybe}, if a value is present,
     * otherwise an failure {@code Maybe}
     * @throws NullPointerException if the mapping function is null
     */
    public <R> Maybe<R> map(ThrowingFunction<? super T, R, ?> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            @SuppressWarnings("unchecked")
            Maybe<R> r = (Maybe<R>) this;
            return r;
        }
        try {
            R apply = mapper.apply(value);
            return Maybe.ofNullable(apply);
        } catch (Exception e) {
            return Maybe.failure(e);
        }
    }

    /**
     * If an error is present, apply the provided mapping function to it,
     * and if the result is non-null, return a {@code Maybe} describing the
     * result. Otherwise return a failure {@code Maybe}.
     *
     * @param mapper a mapping function to apply to the error, if present
     * @return a {@code Maybe} describing the result of applying a mapping
     * function to the error of this {@code Maybe}, if an error is present,
     * otherwise an failure {@code Maybe}
     * @throws NullPointerException if the mapping function is null
     */
    public Maybe<T> mapError(Function<? super Exception, ? extends Exception> mapper) {
        Objects.requireNonNull(mapper);
        if (!isError()) {
            return this;
        }
        return Maybe.failure(mapper.apply(error));
    }

    /**
     * If a value is present, apply the provided {@code Maybe}-bearing
     * mapping function to it, return that result, otherwise return a failure
     * {@code Maybe}. This method is similar to {@link #map(ThrowingFunction)},
     * but the provided mapper is one whose result is already a {@code Maybe},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code Optional}.
     *
     * @param <R>    The type parameter to the {@code Maybe} returned by
     * @param mapper a mapping function to apply to the value, if present
     *               the mapping function
     * @return the result of applying an {@code Maybe}-bearing mapping
     * function to the value of this {@code Maybe}, if a value is present,
     * otherwise a failure {@code failure}
     * @throws NullPointerException if the mapping function is null or returns
     *                              a null result
     */
    public <R> Maybe<R> flatMap(ThrowingFunction<? super T, Maybe<R>, ?> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            @SuppressWarnings("unchecked")
            Maybe<R> r = (Maybe<R>) this;
            return (Maybe<R>) r;
        }
        try {
            return Objects.requireNonNull(mapper.apply(value));
        } catch (Exception e) {
            return Maybe.failure(e);
        }
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise return a {@code Maybe} describing the result.
     *
     * @param sideEffect block to be executed if a value is present
     * @throws NullPointerException if {@code sideEffect} is null
     */
    public Maybe<T> peek(Consumer<T> sideEffect) {
        Objects.requireNonNull(sideEffect);
        if (isPresent()) {
            sideEffect.accept(value);
        }
        return this;
    }

    /**
     * If an error is present, invoke the specified consumer with the error,
     * otherwise return a {@code Maybe} describing the result.
     *
     * @param errorConsumer block to be executed if an error is present
     * @throws NullPointerException if  {@code sideEffect} is null
     */
    public Maybe<T> peekError(Consumer<Exception> errorConsumer) {
        Objects.requireNonNull(errorConsumer);
        if (isError()) {
            errorConsumer.accept(error);
        }
        return this;
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     *              be null
     * @return the value, if present, otherwise {@code other}
     */
    public Maybe<T> or(Maybe<T> other) {
        return isPresent() ? this : other;
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     *              be null
     * @return the value, if present, otherwise {@code other}
     */
    public Maybe<T> orGet(Supplier<Maybe<T>> other) {
        return isPresent() ? this : other.get();
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     *              be null
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no value is present
     * @return the value if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if value is not present and {@code other} is null
     */
    public T orElseGet(Supplier<T> other) {
        return isPresent() ? value : other.get();
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

    public <R> Maybe<R> fold(BooleanSupplier condition,
                             ThrowingFunction<? super T, Maybe<R>, ? extends Exception> falseBranch,
                             ThrowingFunction<? super T, Maybe<R>, ? extends Exception> trueBranch) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(falseBranch);
        Objects.requireNonNull(trueBranch);
        if (!isPresent()) {
            @SuppressWarnings("unchecked")
            Maybe<R> r = (Maybe<R>) this;
            return r;
        }
        try {
            return condition.getAsBoolean() ? trueBranch.apply(get()) : falseBranch.apply(get());
        } catch (Exception e) {
            return Maybe.failure(e);
        }
    }

    /**
     * Indicates whether some other object is "equal to" this Maybe. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Maybe} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Maybe)) {
            return false;
        }

        Maybe<?> other = (Maybe<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this Maybe suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return value != null
                ? String.format("Maybe[%s]", value)
                : "Maybe.empty";
    }

    public interface ThrowingFunction<T, R, E extends Exception> {

        R apply(T t) throws E;

    }
}
