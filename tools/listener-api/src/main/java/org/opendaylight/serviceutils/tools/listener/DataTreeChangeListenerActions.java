/*
 * Copyright (c) 2018 Ericsson, S.A. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.listener;

import java.util.List;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Interface to be implemented by classes interested in receiving notifications
 * about data tree changes. It implements a default method to handle the data
 * tree modifications. Those notifications will be forwarded to the appropriate
 * methods (add, update, remove) depending on their action type. The listeners
 * implementing this interface will need to be annotated as {@link Singleton}.
 *
 * @param <T> type of the data object the listener is registered to.
 * @author David Suárez (david.suarez.fuentes@gmail.com)
 */
interface DataTreeChangeListenerActions<T extends DataObject> {
    /**
     * Default method invoked upon data tree change, in turn it calls the
     * appropriate method (add, update, remove) depending on the type of change.
     *
     * @param changes          collection of changes
     * @param dataStoreMetrics data store metrics
     */
    @SuppressWarnings("checkstyle:MissingSwitchDefault") // http://errorprone.info/bugpattern/UnnecessaryDefaultInEnumSwitch
    default void onDataTreeChanged(List<DataTreeModification<T>> changes, DataStoreMetrics dataStoreMetrics) {
        // This code is also in DataTreeEventCallbackRegistrarImpl and any changes should be applied there as well
        for (var dataTreeModification : changes) {
            var instanceIdentifier = dataTreeModification.getRootPath().path();
            var  dataObjectModification = dataTreeModification.getRootNode();
            T dataBefore = dataObjectModification.dataBefore();
            T dataAfter = dataObjectModification.dataAfter();

            switch (dataObjectModification.modificationType()) {
                case SUBTREE_MODIFIED:
                    if (dataStoreMetrics != null) {
                        dataStoreMetrics.incrementUpdated();
                    }
                    update(instanceIdentifier, dataBefore, dataAfter);
                    break;
                case DELETE:
                    if (dataStoreMetrics != null) {
                        dataStoreMetrics.incrementDeleted();
                    }
                    remove(instanceIdentifier, dataBefore);
                    break;
                case WRITE:
                    if (dataBefore == null) {
                        if (dataStoreMetrics != null) {
                            dataStoreMetrics.incrementAdded();
                        }
                        add(instanceIdentifier, dataAfter);
                    } else {
                        if (dataStoreMetrics != null) {
                            dataStoreMetrics.incrementUpdated();
                        }
                        update(instanceIdentifier, dataBefore, dataAfter);
                    }
                    break;
            }
        }
    }

    /**
     * Invoked when a new data object is added.
     *
     * @param instanceIdentifier instance id for this data object
     * @param newDataObject      newly added object
     */
    @SuppressWarnings("InconsistentOverloads") // TODO remove when @Deprecated add() is removed
    default void add(@NonNull InstanceIdentifier<T> instanceIdentifier, @NonNull T newDataObject) {
        add(newDataObject);
    }

    /**
     * Invoked when a new data object added.
     *
     * @param newDataObject newly added object
     */
    @Deprecated
    void add(@NonNull T newDataObject);

    /**
     * Invoked when the data object has been removed.
     *
     * @param instanceIdentifier instance id for this data object
     * @param removedDataObject  existing object being removed
     */
    @SuppressWarnings("InconsistentOverloads") // TODO remove when @Deprecated remove() is removed
    default void remove(@NonNull InstanceIdentifier<T> instanceIdentifier, @NonNull T removedDataObject) {
        remove(removedDataObject);
    }

    /**
     * Invoked when the data object has been removed.
     *
     * @param removedDataObject existing object being removed
     */
    @Deprecated
    void remove(@NonNull T removedDataObject);

    /**
     * Invoked when there is a change in the data object.
     *
     * @param instanceIdentifier instance id for this data object
     * @param originalDataObject existing object being modified
     * @param updatedDataObject  modified data object
     */
    @SuppressWarnings("InconsistentOverloads") // TODO remove when @Deprecated update() is removed
    default void update(@NonNull InstanceIdentifier<T> instanceIdentifier, @NonNull T originalDataObject,
            @NonNull T updatedDataObject) {
        update(originalDataObject, updatedDataObject);
    }

    /**
     * Invoked when there is a change in the data object.
     *
     * @param originalDataObject existing object being modified
     * @param updatedDataObject  modified data object
     */
    @Deprecated
    void update(@NonNull T originalDataObject, @NonNull T updatedDataObject);
}
