/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.upgrade.impl;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.upgrade.UpgradeState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.upgrade.rev180702.UpgradeConfig;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Do *NOT* use annotation based DI with the blueprint-maven-plugin here; the issue is that this will cause
// problems in other projects having a dependency to this one (they would repeat and re-generate this project's BP XML).
// FIXME: this problem is solvable by splitting into API + implementation, in which case we end up having scope=test
public final class UpgradeStateListener implements UpgradeState, ClusteredDataTreeChangeListener<UpgradeConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(UpgradeStateListener.class);

    private final AtomicBoolean isUpgradeInProgress = new AtomicBoolean(false);
    private final ListenerRegistration<UpgradeStateListener> registration;

    public UpgradeStateListener(DataBroker dataBroker, UpgradeConfig upgradeConfig) {
        registration = dataBroker.registerDataTreeChangeListener(DataTreeIdentifier.create(
            LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(UpgradeConfig.class)), this);
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        //TODO: DS Writes should ideally be done from one node to avoid ConflictingModExceptions
        tx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(UpgradeConfig.class), upgradeConfig);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to write mdsal config", e);
        } // Possibility of OptimisticLockException?
    }

    public void close() {
        registration.close();
    }

    @Override
    public boolean isUpgradeInProgress() {
        return isUpgradeInProgress.get();
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<UpgradeConfig>> changes) {
        final UpgradeConfig change = Iterables.getLast(changes).getRootNode().getDataAfter();
        isUpgradeInProgress.set(change != null && change.getUpgradeInProgress());
    }
}
