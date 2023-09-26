/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.serviceutils.srm.spi.RegistryControl;
import org.opendaylight.serviceutils.tools.rpc.FutureRpcResults;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.ServiceOps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.ServicesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.Operations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.service.ops.services.OperationsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.Recover;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.Reinstall;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailEntityId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailEntityName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailEntityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcFailUnknown;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RpcSuccess;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusIfm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusIfmInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItmTep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItmTz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAclInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAclInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtDhcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtElan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtElanInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gwConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gwNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtQos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtQosPolicyInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtVpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtVpnInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.Ofplugin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.ServiceOpRecover;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.ServiceOpReinstall;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = RegistryControl.class)
public final class RegistryControlImpl implements RegistryControl, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(RegistryControlImpl.class);
    private static final Boolean REINSTALL_FAILED = Boolean.FALSE;
    private static final Boolean REINSTALL_SUCCESS = Boolean.TRUE;

    private static final ImmutableMap<EntityNameBase, EntityTypeBase> NAME_TO_TYPE_MAP =
        ImmutableMap.<EntityNameBase, EntityTypeBase>builder()
            .put(GeniusItm.VALUE, EntityTypeService.VALUE)
            .put(GeniusIfm.VALUE, EntityTypeService.VALUE)
            .put(NetvirtVpn.VALUE, EntityTypeService.VALUE)
            .put(NetvirtElan.VALUE, EntityTypeService.VALUE)
            .put(NetvirtL2gw.VALUE, EntityTypeService.VALUE)
            .put(NetvirtDhcp.VALUE, EntityTypeService.VALUE)
            .put(NetvirtAcl.VALUE, EntityTypeService.VALUE)
            .put(Ofplugin.VALUE, EntityTypeService.VALUE)
            .put(GeniusItmTep.VALUE, EntityTypeInstance.VALUE)
            .put(GeniusItmTz.VALUE, EntityTypeInstance.VALUE)
            .put(GeniusIfmInterface.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtVpnInstance.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtElanInterface.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtL2gwConnection.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtL2gwNode.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtQos.VALUE, EntityTypeService.VALUE)
            .put(NetvirtQosPolicyInstance.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtAclInterface.VALUE, EntityTypeInstance.VALUE)
            .put(NetvirtAclInstance.VALUE, EntityTypeInstance.VALUE)
            .build();

    private static final ImmutableMap<EntityNameBase, EntityNameBase> NAME_TO_SERVICE_MAP =
        ImmutableMap.<EntityNameBase, EntityNameBase>builder()
            .put(GeniusItm.VALUE, GeniusItm.VALUE)
            .put(GeniusIfm.VALUE, GeniusIfm.VALUE)
            .put(GeniusItmTep.VALUE, GeniusItm.VALUE)
            .put(GeniusItmTz.VALUE, GeniusItm.VALUE)
            .put(GeniusIfmInterface.VALUE, GeniusIfm.VALUE)
            .put(NetvirtVpn.VALUE, NetvirtVpn.VALUE)
            .put(NetvirtVpnInstance.VALUE, NetvirtVpn.VALUE)
            .put(NetvirtElan.VALUE, NetvirtElan.VALUE)
            .put(NetvirtElanInterface.VALUE, NetvirtElan.VALUE)
            .put(NetvirtAcl.VALUE, NetvirtAcl.VALUE)
            .put(NetvirtAclInterface.VALUE, NetvirtAcl.VALUE)
            .put(NetvirtAclInstance.VALUE, NetvirtAcl.VALUE)
            .put(NetvirtL2gwConnection.VALUE, NetvirtL2gw.VALUE)
            .put(NetvirtL2gwNode.VALUE, NetvirtL2gw.VALUE)
            .put(NetvirtL2gw.VALUE, NetvirtL2gw.VALUE)
            .put(NetvirtDhcp.VALUE, NetvirtDhcp.VALUE)
            .put(Ofplugin.VALUE, Ofplugin.VALUE)
            .put(NetvirtQos.VALUE, NetvirtQos.VALUE)
            .put(NetvirtQosPolicyInstance.VALUE, NetvirtQos.VALUE)
            .build();

    private final DataBroker dataBroker;
    private final Registration reg;

    @Activate
    public RegistryControlImpl(@Reference DataBroker dataBroker, @Reference RpcProviderService rpcProvider) {
        this.dataBroker = requireNonNull(dataBroker);
        reg = rpcProvider.registerRpcImplementations(ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(Recover.class, input -> FutureRpcResults.fromListenableFuture(LOG, "recover", input,
                () -> recover(input)).build())
            .put(Reinstall.class, input -> FutureRpcResults.fromListenableFuture(LOG, "reinstall", input,
                () -> reinstall(input)).build())
            .build());
    }

    @Override
    @Deactivate
    @PreDestroy
    public void close() {
        reg.close();
    }

    @Override
    public ListenableFuture<RecoverOutput> recover(RecoverInput input) {
        var entityName = input.getEntityName();
        if (entityName == null) {
            return Futures.immediateFuture(new RecoverOutputBuilder()
                .setResponse(RpcFailEntityName.VALUE)
                .setMessage("EntityName is null")
                .build());
        }
        if (input.getEntityType() == null) {
            return Futures.immediateFuture(new RecoverOutputBuilder()
                .setResponse(RpcFailEntityType.VALUE)
                .setMessage("EntityType for %s can't be null".formatted(entityName))
                .build());
        }
        var entityId = input.getEntityId();
        if (EntityTypeInstance.VALUE.equals(input.getEntityType()) && entityId == null) {
            return Futures.immediateFuture(new RecoverOutputBuilder()
                .setResponse(RpcFailEntityId.VALUE)
                .setMessage("EntityId can't be null for %s".formatted(entityName))
                .build());
        }
        var serviceName = NAME_TO_SERVICE_MAP.get(entityName);
        if (serviceName == null) {
            return Futures.immediateFuture(new RecoverOutputBuilder()
                .setResponse(RpcFailEntityName.VALUE)
                .setMessage("EntityName %s has no matching service".formatted(entityName))
                .build());
        }
        var entityType = NAME_TO_TYPE_MAP.get(entityName);
        if (entityType == null || !input.getEntityType().equals(entityType)) {
            return Futures.immediateFuture(new RecoverOutputBuilder()
                .setResponse(RpcFailEntityType.VALUE)
                .setMessage("EntityName %s doesn't match with EntityType %s".formatted(entityName, entityType))
                .build());
        }

        var opsBuilder = new OperationsBuilder()
            .setEntityName(entityName)
            .setEntityType(entityType)
            .setTriggerOperation(ServiceOpRecover.VALUE);
        if (entityId != null) {
            opsBuilder.setEntityId(entityId);
        }
        var operation = opsBuilder.build();
        var opsIid = getInstanceIdentifier(operation, serviceName);
        var tx = dataBroker.newWriteOnlyTransaction();
        tx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, opsIid, operation);

        var ret = SettableFuture.<RecoverOutput>create();
        Futures.addCallback(tx.commit(), new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo result) {
                ret.set(new RecoverOutputBuilder()
                    .setResponse(RpcSuccess.VALUE)
                    .setMessage("Recovery operation successfully triggered")
                    .build());
            }

            @Override
            public void onFailure(Throwable cause) {
                LOG.error("Error writing RecoveryOp to datastore. path:{}, data:{}", opsIid, operation);
                ret.set(new RecoverOutputBuilder()
                    .setResponse(RpcFailUnknown.VALUE)
                    .setMessage(cause.getMessage())
                    .build());
            }
        }, MoreExecutors.directExecutor());

        return ret;
    }

    @Override
    public ListenableFuture<ReinstallOutput> reinstall(ReinstallInput input) {
        var entityName = input.getEntityName();
        if (entityName == null) {
            return Futures.immediateFuture(new ReinstallOutputBuilder()
                .setSuccessful(REINSTALL_FAILED)
                .setMessage("EntityName is null").build());
        }
        if (input.getEntityType() == null) {
            return Futures.immediateFuture(new ReinstallOutputBuilder()
                .setSuccessful(REINSTALL_FAILED)
                .setMessage("EntityType for %s can't be null".formatted(entityName))
                .build());
        }

        if (!EntityTypeService.VALUE.equals(input.getEntityType())) {
            return Futures.immediateFuture(new ReinstallOutputBuilder()
                .setSuccessful(REINSTALL_FAILED)
                .setMessage("EntityType is %s, Reinstall is only for EntityTypeService".formatted(
                    input.getEntityType()))
                .build());
        }

        var serviceName = NAME_TO_SERVICE_MAP.get(entityName);
        if (serviceName == null) {
            return Futures.immediateFuture(new ReinstallOutputBuilder()
                .setSuccessful(REINSTALL_FAILED)
                .setMessage("EntityName %s has no matching service".formatted(entityName))
                .build());
        }

        var entityType = NAME_TO_TYPE_MAP.get(entityName);
        if (entityType == null || !input.getEntityType().equals(entityType)) {
            return Futures.immediateFuture(new ReinstallOutputBuilder()
                .setSuccessful(REINSTALL_FAILED)
                .setMessage("EntityName %s doesn't match with EntityType %s".formatted(entityName, entityType))
                .build());
        }

        var operation = new OperationsBuilder()
            .setEntityName(entityName)
            .setEntityType(entityType)
            .setTriggerOperation(ServiceOpReinstall.VALUE)
            .build();
        var opsIid = getInstanceIdentifier(operation, serviceName);
        var tx = dataBroker.newWriteOnlyTransaction();
        tx.mergeParentStructurePut(LogicalDatastoreType.OPERATIONAL, opsIid, operation);

        var ret = SettableFuture.<ReinstallOutput>create();
        Futures.addCallback(tx.commit(), new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo result) {
                ret.set(new ReinstallOutputBuilder()
                    .setSuccessful(REINSTALL_SUCCESS)
                    .setMessage("Recovery operation successfully triggered")
                    .build());
            }

            @Override
            public void onFailure(Throwable cause) {
                LOG.error("Error writing RecoveryOp to datastore. path:{}, data:{}", opsIid, operation);
                ret.set(new ReinstallOutputBuilder()
                    .setSuccessful(REINSTALL_FAILED)
                    .setMessage(cause.getMessage())
                    .build());

            }
        }, MoreExecutors.directExecutor());

        return ret;
    }

    private static InstanceIdentifier<Operations> getInstanceIdentifier(
            Operations operation, EntityNameBase serviceName) {
        return InstanceIdentifier.create(ServiceOps.class)
            .child(Services.class, new ServicesKey(serviceName))
            .child(Operations.class, operation.key());
    }
}
