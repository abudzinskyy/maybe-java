package com.github.maybe.java;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaybeTest {

    @Test
    public void testOf() {
        Object value = new Object();
        Maybe<Object> maybe = Maybe.of(value);
        assertTrue(maybe.isPresent());
        assertEquals(value, maybe.get());
    }

    @Test
    public void testOfNull() {
        assertThrows(NullPointerException.class, () -> Maybe.of(null));
    }

    @Test
    public void testOfNullable() {
        Object value = new Object();
        Maybe<Object> maybe = Maybe.ofNullable(value);
        assertTrue(maybe.isPresent());
        assertSame(value, maybe.get());
    }

    @Test
    public void testOfNullableNull() {
        Maybe<Object> maybe = Maybe.ofNullable(null);
        assertFalse(maybe.isPresent());
        assertFalse(maybe.isError());
    }

    @Test
    public void testEmpty() {
        Maybe<Object> empty = Maybe.empty();
        assertFalse(empty.isPresent());
        assertFalse(empty.isError());
        assertSame(empty, Maybe.empty());
    }

    @Test
    public void testFailure() {
        Exception ex = new Exception();
        Maybe<Object> failure = Maybe.failure(ex);
        assertFalse(failure.isPresent());
        assertTrue(failure.isError());
        assertSame(ex, failure.getError());
    }

    @Test
    public void testFailureNull() {
        assertThrows(NullPointerException.class, () -> Maybe.failure(null));
    }

    @Test
    public void testMap() {
        Maybe<Integer> mapResult = Maybe.of(1).map(i -> 2);
        assertTrue(mapResult.isPresent());
        assertEquals(2, mapResult.get().intValue());
    }

    @Test
    public void testMapNull() {
        Maybe<Integer> mapResult = Maybe.of(1).map(i -> null);
        assertSame(Maybe.empty(), mapResult);
    }

    @Test
    public void testMapThrow() {
        Exception ex = new Exception();
        Maybe<Integer> mapResult = Maybe.of(1).map(i -> {
            throw ex;
        });
        assertFalse(mapResult.isPresent());
        assertTrue(mapResult.isError());
        assertSame(ex, mapResult.getError());
    }

    @Test
    public void testMapEmpty() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        assertSame(Maybe.empty(), Maybe.empty().map(e -> {
            wasInvoked.set(true);
            return 1;
        }));
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testMapFailed() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        Maybe<Object> source = Maybe.failure(new Exception());
        assertSame(source, source.map(e -> {
            wasInvoked.set(true);
            return 1;
        }));
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testFlatMap() {
        Maybe<Integer> other = Maybe.of(2);
        Maybe<Integer> mapResult = Maybe.of(1).flatMap(i -> other);
        assertSame(other, mapResult);
    }

    @Test
    public void testFlatMapNull() {
        Maybe<Integer> flatMap = Maybe.of(1).flatMap(i -> null);
        assertTrue(flatMap.isError());
        assertTrue(flatMap.getError() instanceof NullPointerException);
    }

    @Test
    public void testFlatMapEmpty() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        assertSame(Maybe.empty(), Maybe.empty().flatMap(e -> {
            wasInvoked.set(true);
            return Maybe.of(1);
        }));
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testFlatMapFailed() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        Maybe<Object> source = Maybe.failure(new Exception());
        assertSame(source, source.flatMap(e -> {
            wasInvoked.set(true);
            return Maybe.of(1);
        }));
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testFilterTrue() {
        Maybe<Integer> filter = Maybe.of(1).filter(i -> i > 0);
        assertTrue(filter.isPresent());
        assertEquals(1, filter.get().intValue());
    }

    @Test
    public void testFilterFalse() {
        Maybe<Integer> filter = Maybe.of(1).filter(i -> i < 0);
        assertSame(Maybe.empty(), filter);
    }

    @Test
    public void testFilterEmpty() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        Maybe<Integer> filter = Maybe.<Integer>empty()
                .filter(i -> {
                    wasInvoked.set(true);
                    return i < 0;
                });
        assertFalse(filter.isPresent());
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testFilterFailed() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        Maybe<Integer> source = Maybe.failure(new Exception());
        Maybe<Integer> filter = source
                .filter(i -> {
                    wasInvoked.set(true);
                    return i < 0;
                });
        assertFalse(filter.isPresent());
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testOrValid() {
        Maybe<Integer> maybe = Maybe.of(1);
        assertSame(maybe, maybe.or(Maybe.of(2)));
    }

    @Test
    public void testOrEmpty() {
        Maybe<Integer> maybe = Maybe.of(1);
        assertSame(maybe, Maybe.<Integer>empty().or(maybe));
    }

    @Test
    public void testOrFailed() {
        Maybe<Integer> maybe = Maybe.of(1);
        assertSame(maybe, Maybe.<Integer>failure(new Exception()).or(maybe));
    }

    @Test
    public void testOrGetValid() {
        Maybe<Integer> maybe = Maybe.of(1);
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        assertSame(maybe, maybe.orGet(() -> {
            wasInvoked.set(true);
            return Maybe.of(2);
        }));
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testOrGetEmpty() {
        Maybe<Integer> maybe = Maybe.of(1);
        assertSame(maybe, Maybe.<Integer>empty().orGet(() -> maybe));
    }

    @Test
    public void testOrGetFailed() {
        Maybe<Integer> maybe = Maybe.of(1);
        assertSame(maybe, Maybe.<Integer>failure(new Exception()).orGet(() -> maybe));
    }

    @Test
    public void testOrElseValid() {
        assertEquals(1, Maybe.of(1).orElse(2).intValue());
    }

    @Test
    public void testOrElseEmpty() {
        assertEquals(2, Maybe.<Integer>empty().orElse(2).intValue());
    }

    @Test
    public void testOrElseFailed() {
        assertEquals(2, Maybe.<Integer>failure(new Exception()).orElse(2).intValue());
    }

    @Test
    public void testOrElseGetValid() {
        AtomicBoolean wasInvoked = new AtomicBoolean(false);
        assertEquals(1, Maybe.of(1).orElseGet(() -> {
            wasInvoked.set(true);
            return 2;
        }).intValue());
        assertFalse(wasInvoked.get());
    }

    @Test
    public void testOrElseGetEmpty() {
        assertEquals(2, Maybe.<Integer>empty().orElseGet(() -> 2).intValue());
    }

    @Test
    public void testOrElseGetFailed() {
        assertEquals(2, Maybe.<Integer>failure(new Exception()).orElseGet(() -> 2).intValue());
    }

    @Test
    public void testOnErrorValid() {
        assertEquals(1, Maybe.of(1).onError(ex -> 2).intValue());
    }

    @Test
    public void testOnErrorInvalid() {
        assertEquals(2, Maybe.<Integer>failure(new Exception()).onError(ex -> 2).intValue());
    }

    @Test
    public void testOnErrorEmpty() {
        NoSuchElementException npe = assertThrows(NoSuchElementException.class,
                () -> Maybe.empty().onError(ex -> 2));
        assertEquals("No value present", npe.getMessage());
    }

    @Test
    public void testGetValid() {
        assertEquals(1, Maybe.of(1).get().intValue());
    }

    @Test
    public void testGetEmpty() {
        NoSuchElementException npe = assertThrows(NoSuchElementException.class,
                () -> Maybe.empty().get());
        assertEquals("No value present", npe.getMessage());
    }

    @Test
    public void testGetFailed() {
        NoSuchElementException npe = assertThrows(NoSuchElementException.class,
                () -> Maybe.failure(new Exception()).get());
        assertEquals("No value present", npe.getMessage());
    }

    @Test
    public void getErrorValid() {
        NoSuchElementException npe = assertThrows(NoSuchElementException.class,
                () -> Maybe.of(1).getError());
        assertEquals("No error present", npe.getMessage());
    }

    @Test
    public void getErrorEmpty() {
        NoSuchElementException npe = assertThrows(NoSuchElementException.class,
                () -> Maybe.empty().getError());
        assertEquals("No error present", npe.getMessage());
    }

    @Test
    public void getErrorFailed() {
        Exception ex = new Exception();
        assertSame(ex, Maybe.failure(ex).getError());
    }

    @Test
    public void isPresentValid() {
        assertTrue(Maybe.of(1).isPresent());
    }

    @Test
    public void isPresentEmpty() {
        assertFalse(Maybe.empty().isPresent());
    }

    @Test
    public void isPresentFailed() {
        assertFalse(Maybe.failure(new Exception()).isPresent());
    }

    @Test
    public void isErrorValid() {
        assertFalse(Maybe.of(1).isError());
    }

    @Test
    public void isErrorEmpty() {
        assertFalse(Maybe.empty().isError());
    }

    @Test
    public void isErrorFailed() {
        assertTrue(Maybe.failure(new Exception()).isError());
    }
}
