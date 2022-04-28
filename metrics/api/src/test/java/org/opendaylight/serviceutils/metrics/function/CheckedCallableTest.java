/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.FileNotFoundException;
import org.junit.Test;

/**
 * Unit Test for {@link CheckedCallable}.
 *
 * @author Michael Vorburger.ch
 */
public class CheckedCallableTest {
    @Test
    public void testCheckedCallableWithCheckedException() {
        assertThrows(FileNotFoundException.class, () -> foo(() -> {
            throw new FileNotFoundException("boum");
        }));
    }

    @Test
    public void testCheckedCallableWithUncheckedException() {
        assertThrows(IllegalArgumentException.class, () -> foo(() -> {
            throw new IllegalArgumentException("boum");
        }));
    }

    @Test
    public void testCheckedCallableWithoutAnyException() {
        assertEquals(43, (int) foo(() -> 43));
    }

    private static <T, E extends Exception> T foo(CheckedCallable<T, E> checkedCallable) throws E {
        return checkedCallable.call();
    }
}
