/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.sample;

import java.io.IOException;
import org.opendaylight.serviceutils.metrics.internal.MetricProviderImpl;

/**
 * Launcher for simple standalone metrics example.
 *
 * @author Michael Vorburger.ch
 */
public final class MetricsExampleMain {
    private MetricsExampleMain() {

    }

    public static void main(String[] args) throws IOException {
        MetricProviderImpl metricProvider = new MetricProviderImpl();
        try (MetricsExample metricsExample = new MetricsExample(metricProvider)) {
            System.in.read();
        }
        metricProvider.close();
    }
}
