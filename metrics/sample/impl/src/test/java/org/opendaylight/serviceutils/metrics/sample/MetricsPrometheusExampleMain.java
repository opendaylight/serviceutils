/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.sample;

import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.opendaylight.serviceutils.metrics.prometheus.impl.PrometheusMetricProvider;

/**
 * Launcher for and demo of simple standalone metrics example reporting to Prometheus.
 *
 * @author Michael Vorburger.ch
 */
public final class MetricsPrometheusExampleMain {
    private MetricsPrometheusExampleMain() {
        // Hidden on purpose
    }

    public static void main(String[] args) throws IOException {
        // see also OsgiWebInitializer
        try (var metricProvider = new PrometheusMetricProvider()) {
            try (var metricsExample = new MetricsExample(metricProvider)) {
                HTTPServer server = new HTTPServer(new InetSocketAddress("localhost", 1234), metricProvider.registry());
                System.in.read();
                server.stop();
            }
        }
    }
}
