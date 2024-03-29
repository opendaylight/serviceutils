/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.metrics.internal;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.opendaylight.infrautils.utils.concurrent.Executors.newListeningSingleThreadScheduledExecutor;
import static org.opendaylight.infrautils.utils.concurrent.LoggingFutures.addErrorLogging;

import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import com.codahale.metrics.jvm.ThreadDump;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayOutputStream;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatic JVM thread limit and deadlock detection logging.
 *
 * @author Michael Vorburger.ch
 */
class ThreadsWatcher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadsWatcher.class);

    private final int maxThreads;
    private final ScheduledExecutorService scheduledExecutor;
    private final ThreadDeadlockDetector threadDeadlockDetector;
    private final ThreadMXBean threadMXBean;
    private final ThreadDump threadDump;

    private final Duration interval;
    private final Duration maxDeadlockLog;
    private final Duration maxMaxThreadsLog;

    private volatile Instant lastDeadlockLog;
    private volatile Instant lastMaxThreadsLog;

    ThreadsWatcher(ThreadMXBean threadMXBean, int maxThreads, Duration interval,
            Duration maxThreadsMaxLogInterval, Duration deadlockedThreadsMaxLogInterval) {
        this.threadMXBean = requireNonNull(threadMXBean);
        this.threadDeadlockDetector = new ThreadDeadlockDetector(threadMXBean);
        this.threadDump = new ThreadDump(threadMXBean);

        this.maxThreads = maxThreads;
        this.interval = interval;
        this.maxDeadlockLog = deadlockedThreadsMaxLogInterval;
        this.maxMaxThreadsLog = maxThreadsMaxLogInterval;
        this.scheduledExecutor = newListeningSingleThreadScheduledExecutor("serviceutils.metrics.ThreadsWatcher", LOG);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    void start() {
        addErrorLogging(scheduledExecutor.scheduleAtFixedRate(this, 0, interval.toNanos(), NANOSECONDS), LOG,
                "scheduleAtFixedRate");
    }

    void close() {
        scheduledExecutor.shutdown();
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public Duration getInterval() {
        return interval;
    }

    public Duration getMaxThreadsMaxLogInterval() {
        return maxMaxThreadsLog;
    }

    public Duration getDeadlockedThreadsMaxLogInterval() {
        return maxDeadlockLog;
    }

    @Override
    public void run() {
        int currentNumberOfThreads = threadMXBean.getThreadCount();
        Set<String> deadlockedThreadsStackTrace = threadDeadlockDetector.getDeadlockedThreads();
        if (!deadlockedThreadsStackTrace.isEmpty()) {
            LOG.error("Oh nose - there are {} deadlocked threads!! :-(", deadlockedThreadsStackTrace.size());
            for (String deadlockedThreadStackTrace : deadlockedThreadsStackTrace) {
                LOG.error("Deadlocked thread stack trace: {}", deadlockedThreadStackTrace);
            }
            if (isConsidered(lastDeadlockLog, Instant.now(), maxDeadlockLog)) {
                logAllThreads();
                lastDeadlockLog = Instant.now();
            }

        } else if (currentNumberOfThreads >= maxThreads) {
            LOG.warn("Oh nose - there are now {} threads, more than maximum threshold {}! "
                    + "(totalStarted: {}, peak: {}, daemons: {})",
                    currentNumberOfThreads, maxThreads, threadMXBean.getTotalStartedThreadCount(),
                    threadMXBean.getPeakThreadCount(), threadMXBean.getDaemonThreadCount());
            if (isConsidered(lastMaxThreadsLog, Instant.now(), maxMaxThreadsLog)) {
                logAllThreads();
                lastMaxThreadsLog = Instant.now();
            }
        }
    }

    @VisibleForTesting
    boolean isConsidered(Instant lastOccurence, Instant now, Duration maxFrequency) {
        return lastOccurence == null || Duration.between(lastOccurence, now).compareTo(maxFrequency) >= 0;
    }

    @VisibleForTesting
    void logAllThreads() {
        try (LoggingOutputStream loggingOutputStream = new LoggingOutputStream()) {
            threadDump.dump(loggingOutputStream);
        }
    }

    private static final class LoggingOutputStream extends ByteArrayOutputStream {
        @Override
        public void close() {
            // UTF-8 because that is what ThreadDump writes it in
            LOG.warn("Thread Dump:\n{}", toString(StandardCharsets.UTF_8));
        }
    }
}
