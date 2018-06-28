/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.serviceutils.tools.mdsal.config.impl;

import static org.opendaylight.controller.md.sal.binding.api.WriteTransaction.CREATE_MISSING_PARENTS;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.serviceutils.tools.mdsal.config.UpgradeState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.tools.config.rev180628.Upgrade;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.ops4j.pax.cdi.api.OsgiService;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@OsgiServiceProvider(classes = UpgradeState.class)
public class UpgradeStateListener implements ClusteredDataTreeChangeListener<Upgrade>, UpgradeState {
    private static final Logger LOG = LoggerFactory.getLogger(UpgradeStateListener.class);

    private final ListenerRegistration<UpgradeStateListener> registration;
    private final DataTreeIdentifier<Upgrade> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
        InstanceIdentifier.create(Upgrade.class));
    private final AtomicBoolean isUpgradeInProgress = new AtomicBoolean(false);

    @Inject
    public UpgradeStateListener(@OsgiService DataBroker dataBroker, Upgrade upgrade) {
        registration = dataBroker.registerDataTreeChangeListener(treeId, UpgradeStateListener.this);
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Upgrade.class), upgrade,
            CREATE_MISSING_PARENTS);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to write mdsal config", e);
        } // Possibility of OptimisticLockException?
    }

    @PreDestroy
    public void close() {
        if (registration != null) {
            registration.close();
        }
    }

    @Override
    public boolean isUpgradeInProgress() {
        return isUpgradeInProgress.get();
    }

    @Override
    @SuppressWarnings("checkstyle:MissingSwitchDefault") // http://errorprone.info/bugpattern/UnnecessaryDefaultInEnumSwitch
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Upgrade>> changes) {
        for (DataTreeModification<Upgrade> change : changes) {
            DataObjectModification<Upgrade> mod = change.getRootNode();
            switch (mod.getModificationType()) {
                case DELETE:
                    isUpgradeInProgress.set(false);
                    break;
                case SUBTREE_MODIFIED:
                case WRITE:
                    isUpgradeInProgress.set(mod.getDataAfter().isUpgradeInProgress());
                    break;
            }
        }

    }
}
