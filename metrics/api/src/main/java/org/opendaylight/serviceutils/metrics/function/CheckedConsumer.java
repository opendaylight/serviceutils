/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.function;

import java.util.function.Consumer;

/**
 * {@link Consumer} which can throw a checked exception.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of the Exception to the operation
 *
 * @see Consumer
 *
 * @author Michael Vorburger.ch
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

    void accept(T input) throws E;

}
