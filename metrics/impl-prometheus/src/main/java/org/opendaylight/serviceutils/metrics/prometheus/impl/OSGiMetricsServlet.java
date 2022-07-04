/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.prometheus.impl;

import io.prometheus.client.exporter.MetricsServlet;
import javax.servlet.Servlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = Servlet.class)
@HttpWhiteboardServletName("MetricsServlet")
@HttpWhiteboardServletPattern("/metrics/prometheus")
public final class OSGiMetricsServlet extends MetricsServlet {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiMetricsServlet.class);
    private static final long serialVersionUID = 1L;

    @Activate
    public OSGiMetricsServlet(@Reference CollectorRegistryProvider provider) {
        super(provider.registry());
        LOG.info("Metrics for Prometheus scrape now exposed on /metrics/prometheus");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Metrics for Prometheus scrape no longer exposed");
    }
}
