/*
 * Copyright (c) 2017 - 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.rpc;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.opendaylight.serviceutils.tools.rpc.FutureRpcResults;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Assertion utilities for {@link FutureRpcResults}.
 *
 * @author Michael Vorburger.ch
 */
public final class TestFutureRpcResults {
    private TestFutureRpcResults() {
        // Hidden on purpose
    }

    private static <T> T getResult(RpcResult<T> rpcResult) {
        assertTrue("rpcResult.isSuccessful", rpcResult.isSuccessful());
        T result = rpcResult.getResult();
        assertNotNull("result", result);
        return result;
    }

    public static <T> T getResult(Future<RpcResult<T>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        return getResult(futureRpcResult.get(1, MINUTES));
    }

    public static void assertVoidRpcSuccess(Future<RpcResult<Void>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        RpcResult<Void> rpcResult = futureRpcResult.get(1, MINUTES);
        assertTrue(rpcResult.isSuccessful());
        assertEquals(List.of(), rpcResult.getErrors());
    }

    public static <O extends DataObject> void assertRpcSuccess(Future<RpcResult<O>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        RpcResult<O> rpcResult = futureRpcResult.get(1, MINUTES);
        assertTrue(rpcResult.isSuccessful());
        assertEquals(List.of(), rpcResult.getErrors());
    }

    public static <T> void assertRpcErrorWithoutCausesOrMessages(Future<RpcResult<T>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        RpcResult<T> rpcResult = futureRpcResult.get(1, MINUTES);
        assertFalse("rpcResult.isSuccessful", rpcResult.isSuccessful());
        assertEquals("rpcResult.errors", List.of(), rpcResult.getErrors());
    }

    public static <T> void assertRpcErrorCause(Future<RpcResult<T>> futureRpcResult, Class<?> expectedExceptionClass,
            String expectedRpcErrorMessage) throws InterruptedException, ExecutionException, TimeoutException {
        assertRpcErrorCause(futureRpcResult.get(1, MINUTES), expectedExceptionClass, expectedRpcErrorMessage);
    }

    private static <T> void assertRpcErrorCause(RpcResult<T> rpcResult, Class<?> expected1stExceptionClass,
            String expected1stRpcErrorMessage) {
        assertFalse("rpcResult.isSuccessful", rpcResult.isSuccessful());
        List<RpcError> errors = rpcResult.getErrors();
        assertEquals("rpcResult.errors", 1, errors.size());
        RpcError error1 = errors.iterator().next();
        assertEquals("rpcResult.errors[0].errorType", ErrorType.APPLICATION, error1.getErrorType());
        assertEquals("rpcResult.errors[0].message", expected1stRpcErrorMessage, error1.getMessage());
        if (error1.getCause() != null) {
            // Check needed because FutureRpcResults does not propagate cause if OperationFailedException
            assertTrue("rpcResult.errors[0].cause", expected1stExceptionClass.isInstance(error1.getCause()));
        }
    }
}
