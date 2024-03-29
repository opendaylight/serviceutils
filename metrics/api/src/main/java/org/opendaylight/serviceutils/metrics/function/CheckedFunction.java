/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.function;

/**
 * {@link java.util.function.Function} which can throw a checked exception.
 *
 * @param <T> The type of the input to be processed.
 * @param <R> The type of the result to be returned.
 * @param <E> The type of the exception which may be thrown.
 */
@FunctionalInterface
public interface CheckedFunction<T, R, E extends Exception> {
    R apply(T input) throws E;
}
