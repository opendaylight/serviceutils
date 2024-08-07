/*
 * Copyright (c) 2018 Ericsson S.A. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.mdsal.listener;

import java.util.List;
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
 * {@link AbstractSyncDataTreeChangeListener}.
 *
 * @param <T> type of the data object the listener is registered to.
 *
 * @see AbstractSyncDataTreeChangeListener
 *
 * @author David Suárez (david.suarez.fuentes@gmail.com)
 * @deprecated Use {@code listener-api} instead.
 */
@Deprecated
public abstract class AbstractClusteredSyncDataTreeChangeListener<T extends DataObject> extends
        AbstractDataTreeChangeListener<T> implements ClusteredDataTreeChangeListener<T> {

    public AbstractClusteredSyncDataTreeChangeListener(DataBroker dataBroker,
                                                       DataTreeIdentifier<T> dataTreeIdentifier) {
        super(dataBroker, dataTreeIdentifier);
    }

    public AbstractClusteredSyncDataTreeChangeListener(DataBroker dataBroker, LogicalDatastoreType datastoreType,
                                                       InstanceIdentifier<T> instanceIdentifier) {
        super(dataBroker, datastoreType, instanceIdentifier);
    }

    public AbstractClusteredSyncDataTreeChangeListener(DataBroker dataBroker,
                                                       LogicalDatastoreType datastoreType,
                                                       InstanceIdentifier<T> instanceIdentifier,
                                                       MetricProvider metricProvider) {
        super(dataBroker, datastoreType, instanceIdentifier, metricProvider);
    }

    @Override
    public final void onDataTreeChanged(List<DataTreeModification<T>> collection) {
        super.onDataTreeChanged(collection, getDataStoreMetrics());
    }
}
