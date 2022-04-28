/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.google.errorprone.annotations.Var;
import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.LogCaptureRule;
import org.opendaylight.infrautils.testutils.LogRule;
import org.opendaylight.serviceutils.metrics.Counter;
import org.opendaylight.serviceutils.metrics.Labeled;
import org.opendaylight.serviceutils.metrics.Meter;
import org.opendaylight.serviceutils.metrics.MetricDescriptor;
import org.opendaylight.serviceutils.metrics.MetricProvider;
import org.opendaylight.serviceutils.metrics.Timer;
import org.opendaylight.serviceutils.metrics.function.CheckedRunnable;
import org.opendaylight.serviceutils.metrics.internal.MetricProviderImpl;

/**
 * Unit Test for MetricProviderImpl.
 *
 * @author Michael Vorburger.ch
 */
public class MetricProviderTest {

    @Rule public LogRule logRule = new LogRule();

    @Rule public LogCaptureRule logCaptureRule = new LogCaptureRule();

    private final MetricProvider metrics = new MetricProviderImpl();

    @After
    public void afterEachTest() {
        ((MetricProviderImpl) metrics).close();
    }

    @Test
    public void testMeter() {
        Meter meter1 = metrics.newMeter(MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics")
                .id("test_meter1").build());
        meter1.mark();
        meter1.mark(2);
        assertEquals(3, meter1.get());
    }

    @Test
    public void testMeterWith2Labels() {
        Labeled<Labeled<Meter>> meterWithTwoLabels = metrics.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(),
                "port", "mac");
        Meter meterA = meterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        meterA.mark(3);
        assertEquals(3, meterA.get());

        Meter meterB = meterWithTwoLabels.label(/* port */ "789").label(/* MAC */ "1A:0B:F2:25:1C:68");
        meterB.mark(1);
        assertEquals(1, meterB.get());
        assertEquals(3, meterA.get());

        Meter againMeterA = meterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        assertEquals(3, againMeterA.get());
    }

    @Test
    public void testMeterWith4Labels() {
        Labeled<Labeled<Labeled<Labeled<Meter>>>> meterWith4Labels = metrics
                .newMeter(MetricDescriptor.builder().anchor(this).project("infrautils")
                                .module("metrics").id("test_meter4").build(),
                        "label1", "label2", "label3", "label4");
        Meter meterA = meterWith4Labels.label("l1").label("l2").label("l3").label("l4");
        meterA.mark(4);
        assertEquals(4, meterA.get());

        Meter meterB = meterWith4Labels.label("l1value").label("l2value").label("l3value").label("l4value");
        meterB.mark(1);
        assertEquals(1, meterB.get());
        assertEquals(4, meterA.get());

        Meter againMeterA = meterWith4Labels.label("l1").label("l2").label("l3").label("l4");
        assertEquals(4, againMeterA.get());
    }

    @Test
    public void testMeterWith5Labels() {
        Labeled<Labeled<Labeled<Labeled<Labeled<Meter>>>>> meterWith5Lables = metrics
                .newMeter(MetricDescriptor.builder().anchor(this).project("infrautils")
                                .module("metrics").id("test_meter5").build(),
                        "label1", "label2", "label3",
                        "label4", "label5");
        Meter meterA = meterWith5Lables.label("l1").label("l2").label("l3").label("l4").label("l5");
        meterA.mark(5);
        assertEquals(5, meterA.get());

        Meter meterB = meterWith5Lables.label("l1value").label("l2value").label("l3value").label("l4value")
            .label("l5value");
        meterB.mark(1);
        assertEquals(1, meterB.get());
        assertEquals(5, meterA.get());

        Meter againMeterA = meterWith5Lables.label("l1").label("l2").label("l3").label("l4").label("l5");
        assertEquals(5, againMeterA.get());
    }

    @Test
    public void testCounterWith2Labels() {
        Labeled<Labeled<Counter>> counterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter1").build(),
                "port", "mac");
        Counter counterA = counterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        counterA.increment(3);
        assertEquals(3, counterA.get());

        Counter counterB = counterWithTwoLabels.label(/* port */ "789").label(/* MAC */ "1A:0B:F2:25:1C:68");
        counterB.increment(1);
        assertEquals(1, counterB.get());
        assertEquals(3, counterA.get());

        Counter againCounterA = counterWithTwoLabels.label(/* port */ "456").label(/* MAC */ "1A:0B:F2:25:1C:68");
        assertEquals(3, againCounterA.get());
    }

    @Test
    public void testCounterWith5Labels() {
        Labeled<Labeled<Labeled<Labeled<Labeled<Counter>>>>> counterWithFiveLabels =
                metrics.newCounter(MetricDescriptor.builder().anchor(this).project("infrautils").module("metrics")
                                .id("test_counter2").build(), "label1", "label2",
                                 "label3", "label4", "label5");
        Counter counterA = counterWithFiveLabels.label("l1").label("l2").label("l3")
                .label("l4").label("l5");
        counterA.increment(5);
        assertEquals(5, counterA.get());

        Counter againCounterA = counterWithFiveLabels.label("l1").label("l2").label("l3").label("l4").label("l5");
        assertEquals(5, againCounterA.get());
    }

    @Test
    public void testCounterOperationsWithLabels() {
        Labeled<Labeled<Counter>> counterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter_opers").build(),
                "l1", "l2");
        try (Counter counterA = counterWithTwoLabels.label("l1value").label("l2value")) {
            counterA.increment();
            counterA.increment();
            assertEquals(2, counterA.get());

            counterA.decrement();
            assertEquals(1, counterA.get());

            counterA.increment(5);
            assertEquals(6, counterA.get());
            counterA.decrement(2);
            assertEquals(4, counterA.get());
        }
    }

    @Test
    public void testSameCounterUpdateOperationsWithLabels() {
        Labeled<Labeled<Counter>> counterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter_upd_opers").build(), "l1", "l2");
        Counter counterA = counterWithTwoLabels.label("l1value").label("l2value");
        counterA.increment(51);
        assertEquals(51, counterA.get());

        counterA.decrement();
        assertEquals(50, counterA.get());

        Labeled<Labeled<Counter>> sameCounterWithTwoLabels = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_counter_upd_opers").build(),
                "l1", "l2");
        try (Counter sameCounterA = sameCounterWithTwoLabels.label("l1value").label("l2value")) {
            assertEquals(counterA, sameCounterA);
            assertEquals(50, sameCounterA.get());
            sameCounterA.increment(5);
            assertEquals(55, sameCounterA.get());

            sameCounterA.decrement(10);
            assertEquals(45, sameCounterA.get());
        }
    }

    @Test
    public void testCloseMeterAndCreateNewOneWithSameID() {
        Meter meter = metrics.newMeter(this, "test.meter");
        meter.close();
        Meter meterAgain = metrics.newMeter(this, "test.meter");
        meterAgain.mark();
    }

    @Test
    public void testUseClosedMeter() {
        Meter meter1 = metrics.newMeter(this, "test.meter1");
        meter1.close();
        assertThrows(IllegalStateException.class, meter1::mark);
    }

    @Test
    public void testUseClosedLabeledMeter() {
        Labeled<Meter> meterWithLabel = metrics.newMeter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(), "label1");
        meterWithLabel.label("label1value").mark();
        assertEquals(1, meterWithLabel.label("label1value").get());
        meterWithLabel.label("label1value").close();
        // NOT assertThrows(IllegalStateException.class, () -> meterWithLabel.label("label1value").mark());
        // because we can recreate a metric with the same label, but it will be a new one:
        meterWithLabel.label("label1value").mark();
        assertEquals(1, meterWithLabel.label("label1value").get());
    }

    @Test
    public void testUseClosedLabeledCounter() {
        Labeled<Counter> counterWithLabel = metrics.newCounter(MetricDescriptor.builder().anchor(this)
                .project("infrautils").module("metrics").id("test_meter1").build(), "label1");
        counterWithLabel.label("label1value").increment();
        assertEquals(1, counterWithLabel.label("label1value").get());
        counterWithLabel.label("label1value").close();
        // use counter after close operation
        counterWithLabel.label("label1value").increment();
        assertEquals(1, counterWithLabel.label("label1value").get());
    }

    @Test
    public void testTimeRunnableOK() {
        metrics.newTimer(this, "test.timer").time(() -> {
            for (@SuppressWarnings("unused") int sum = 0, i = 1; i < 101; i++) {
                sum += i;
            }
        });
    }

    @Test
    public void testTimeCallableOK() {
        assertEquals(5050, (int) metrics.newTimer(this, "test.timer").time(() -> {
            @Var int sum = 0;
            for (int i = 1; i < 101; i++) {
                sum += i;
            }
            return sum;
        }));
    }

    @Test
    public void testTimeRunnableOKWithLabels() {
        Labeled<Labeled<Timer>> timerWithTwoLabels = metrics.newTimer(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_timer_with_labels").build(), "l1", "l2");
        Timer timerA = timerWithTwoLabels.label("l1value").label("l2value");
        timerA.time(() -> {
            for (@SuppressWarnings("unused") int sum = 0, i = 1; i < 101; i++) {
                sum += i;
            }
        });
    }

    @Test
    public void testTimeCallableWithLabels() {
        Labeled<Labeled<Timer>> timerWithTwoLabels = metrics.newTimer(MetricDescriptor.builder().anchor(this)
                        .project("infrautils").module("metrics").id("test_timer_with_labels").build(),
                "l1", "l2");
        Timer timerA = timerWithTwoLabels.label("l1value").label("l2value");
        assertEquals(5050, (int) timerA.time(() -> {
            @Var int sum = 0;
            for (int i = 1; i < 101; i++) {
                sum += i;
            }
            return sum;
        }));
    }

    @Test
    public void testTimeCheckedCallableNOK() {
        assertThrows(FileNotFoundException.class, () -> metrics.newTimer(this, "test.timer").time(() -> {
            throw new FileNotFoundException();
        }));
    }

    @Test
    public void testTimeCallableNOK() {
        assertThrows(ArithmeticException.class, () -> metrics.newTimer(this, "test.timer").time(() -> {
            throw new ArithmeticException();
        }));
    }

    @Test
    public void testTimeCheckedRunnableNOK() {
        assertThrows(FileNotFoundException.class,
            () -> metrics.newTimer(this, "test.timer").time(new CheckedRunnable<FileNotFoundException>() {
                @Override
                public void run() throws FileNotFoundException {
                    throw new FileNotFoundException();
                }

                // This unused method is required so that IDEs like Eclipse do not turn this into a lambda;
                // because if it is a lambda, then it will invoked the variant of time() which takes a
                // CheckedCallable instead of the CheckedRunnable one we want to test here.
                @SuppressWarnings("unused")
                private void foo() {
                }
            }));
    }

    @Test
    public void testTimeRunnableNOK() {
        assertThrows(ArithmeticException.class,
            () -> metrics.newTimer(this, "test.timer").time(new CheckedRunnable<FileNotFoundException>() {
                @Override
                public void run() {
                    throw new ArithmeticException();
                }

                @SuppressWarnings("unused")
                private void foo() {
                }
            }));
    }

    @Test
    public void testDupeMeterID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> metrics.newMeter(this, "test.meter1"));
    }

    @Test
    public void testDupeAnyID() {
        metrics.newMeter(this, "test.meter1");
        assertThrows(IllegalArgumentException.class, () -> {
            // NB: We cannot register a Counter (not a Meter) with the same ID, either
            metrics.newCounter(this, "test.meter1");
        });
    }

    // TODO testReadJMX() using org.opendaylight.infrautils.utils.management.MBeanUtil from https://git.opendaylight.org/gerrit/#/c/65153/

}
