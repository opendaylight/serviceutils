/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics;

import org.opendaylight.serviceutils.metrics.function.CheckedCallable;
import org.opendaylight.serviceutils.metrics.function.CheckedRunnable;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A timer metric which aggregates timing durations.
 */
public interface Timer extends Registration {

    /**
     * Times and records the duration of event which returns a value.
     */
    <T, E extends Exception> T time(CheckedCallable<T, E> event) throws E;

    /**
     * Times and records the duration of event.
     */
    <E extends Exception> void time(CheckedRunnable<E> event) throws E;

}
