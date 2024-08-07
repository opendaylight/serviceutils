/*
 * Copyright (c) 2018 Ericsson S.A. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.mdsal.listener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.metrics.MetricProvider;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Abstract class providing some common functionality to specific listeners. This listener launches the received
 * notifications by using an {@link ExecutorService}.
 *
 * <p>The {@link ExecutorService} passed to the constructor will depend on the use case. Here we have some examples:
 *
 * <p>- If the listener is fast enough and non-blocking: MoreExecutors.directExecutor might be used, or even better,
 * just use the {@link AbstractSyncDataTreeChangeListener}.
 *
 * <p>- If the listener is heavy or could be blocked: use a multi-threaded executor.
 * We recommend using one of the factory methods in {@link SpecialExecutors}.
 * You could and probably should share such an Executor among several listeners in your project.
 *
 * <p>- If the listener needs to preserve the order of notifications, then (only) use a single thread executor typically
 * an {@code org.opendaylight.infrautils.utils.concurrent.Executors#newSingleThreadExecutor(String, org.slf4j.Logger)}.
 *
 * <p>- If there are multiple listeners: they could even share an Executor as the ones in {@link SpecialExecutors},
 *
 * <p>Subclasses are also encouraged to, in addition to passing the ExecutorService for use in
 * production (by Blueprint wiring) based on above via super(), expose a public constructor letting tests specify
 * an alternative ExecutorService; this is useful e.g. to inject infrautils' AwaitableExecutorService for testing.
 *
 * @param <T> type of the data object the listener is registered to.
 *
 * @author David Suárez (david.suarez.fuentes@gmail.com)
 * @deprecated Use {@code listener-api} instead.
 */
@Deprecated
public abstract class AbstractAsyncDataTreeChangeListener<T extends DataObject> extends
        AbstractDataTreeChangeListener<T> {
    private final ExecutorService executorService;

    public AbstractAsyncDataTreeChangeListener(DataBroker dataBroker, DataTreeIdentifier<T> dataTreeIdentifier,
                                               ExecutorService executorService) {
        super(dataBroker, dataTreeIdentifier);
        this.executorService = executorService;
    }

    public AbstractAsyncDataTreeChangeListener(DataBroker dataBroker, LogicalDatastoreType datastoreType,
                                               InstanceIdentifier<T> instanceIdentifier,
                                               ExecutorService executorService) {
        super(dataBroker, datastoreType, instanceIdentifier);
        this.executorService = executorService;
    }

    public AbstractAsyncDataTreeChangeListener(DataBroker dataBroker,
                                               LogicalDatastoreType datastoreType,
                                               InstanceIdentifier<T> instanceIdentifier,
                                               ExecutorService executorService,
                                               MetricProvider metricProvider) {
        super(dataBroker, datastoreType, instanceIdentifier, metricProvider);
        this.executorService = executorService;
    }

    @Override
    public final void onDataTreeChanged(List<DataTreeModification<T>> collection) {
        executorService.execute(() -> super.onDataTreeChanged(collection, getDataStoreMetrics()));
    }

    /**
     * Returns the ExecutorService provided when constructing this instance. If the subclass owns the
     * ExecutorService, it should be shut down when closing the listener using this getter.
     *
     * @return executor service
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }
}
