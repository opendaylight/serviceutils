/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.prometheus.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.serviceutils.metrics.Labeled;
import org.opendaylight.serviceutils.metrics.Meter;
import org.opendaylight.serviceutils.metrics.MetricDescriptor;
import org.opendaylight.serviceutils.metrics.MetricProvider;

/**
 * Unit test for {@link PrometheusMetricProviderImpl}.
 *
 * @author Michael Vorburger.ch
 */
public class PrometheusMetricProviderImplTest {
    // TODO share all @Test with existing MetricProviderTest through some refactoring

    private final MetricProvider metricProvider = new PrometheusMetricProvider();

    @Test
    public void testNewMeter() {
        try (var meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build())) {
            meter.mark(123);
            assertEquals(123L, meter.get());
        }
    }

    @Test
    public void testNewMeterWith1FixedLabel() {
        try (var meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build(),
                "label1").label("value1")) {
            meter.mark(123);
            assertEquals(123L, meter.get());
        }
    }

    @Test
    public void testNewMeterWith1DynamicLabel() {
        Labeled<Meter> meterWithLabel = metricProvider.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(), "jobKey");

        Meter meterA = meterWithLabel.label("ABC");
        meterA.mark(3);
        assertEquals(3, meterA.get());

        Meter meterB = meterWithLabel.label("DEF");
        meterB.mark(1);
        assertEquals(1, meterB.get());
        assertEquals(3, meterA.get());

        Meter againMeterA = meterWithLabel.label("ABC");
        assertEquals(3, againMeterA.get());
    }

    @Test
    public void testGetOverflownMeter() {
        Meter meter = metricProvider.newMeter(
                MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics").id("test").build());
        meter.mark(Double.doubleToRawLongBits(Double.MAX_VALUE));
        assertThat(meter.get(), greaterThan(1000000L));
    }

    // TODO more..

}
