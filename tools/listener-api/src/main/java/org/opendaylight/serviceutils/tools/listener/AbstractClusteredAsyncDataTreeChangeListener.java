/*
 * Copyright (c) 2018 Ericsson S.A. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.listener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.metrics.MetricProvider;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Abstract class providing some common functionality to specific listeners. This is the clustered version of the
 * {@link AbstractAsyncDataTreeChangeListener}.  Please carefully consider whether you really need an async listener,
 * or if {@link AbstractClusteredSyncDataTreeChangeListener} wouldn't work just fine for your use case.
 *
 * @param <T> type of the data object the listener is registered to.
 *
 * @see AbstractAsyncDataTreeChangeListener
 *
 * @author David Suárez (david.suarez.fuentes@gmail.com)
 */
public abstract class AbstractClusteredAsyncDataTreeChangeListener<T extends DataObject> extends
        AbstractDataTreeChangeListener<T> implements ClusteredDataTreeChangeListener<T> {
    private final ExecutorService executorService;

    public AbstractClusteredAsyncDataTreeChangeListener(DataBroker dataBroker, DataTreeIdentifier<T> dataTreeIdentifier,
                                                        ExecutorService executorService) {
        super(dataBroker, dataTreeIdentifier);
        this.executorService = executorService;
    }

    public AbstractClusteredAsyncDataTreeChangeListener(DataBroker dataBroker, LogicalDatastoreType datastoreType,
                                                        InstanceIdentifier<T> instanceIdentifier,
                                                        ExecutorService executorService) {
        super(dataBroker, datastoreType, instanceIdentifier);
        this.executorService = executorService;
    }

    public AbstractClusteredAsyncDataTreeChangeListener(DataBroker dataBroker,
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
