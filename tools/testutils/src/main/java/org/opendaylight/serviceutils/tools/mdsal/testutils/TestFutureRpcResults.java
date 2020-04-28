/*
 * Copyright (c) 2017 - 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.tools.mdsal.testutils;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.concurrent.TimeUnit.MINUTES;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.opendaylight.serviceutils.tools.mdsal.rpc.FutureRpcResults;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Assertion utilities for {@link FutureRpcResults}.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings("BC_UNCONFIRMED_CAST") // see https://wiki.opendaylight.org/view/BestPractices/Coding_Guidelines#Unchecked.2Funconfirmed_cast_from_com.google.common.truth.Subject_to_com.google.common.truth.BooleanSubject_etc.
public final class TestFutureRpcResults {

    private TestFutureRpcResults() {

    }

    private static <T> T getResult(RpcResult<T> rpcResult) {
        assertWithMessage("rpcResult.isSuccessful").that(rpcResult.isSuccessful()).isTrue();
        T result = rpcResult.getResult();
        assertWithMessage("result").that(result).isNotNull();
        return result;
    }

    public static <T> T getResult(Future<RpcResult<T>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        return getResult(futureRpcResult.get(1, MINUTES));
    }

    public static void assertVoidRpcSuccess(Future<RpcResult<Void>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        RpcResult<Void> rpcResult = futureRpcResult.get(1, MINUTES);
        assertThat(rpcResult.isSuccessful()).isTrue();
        assertThat(rpcResult.getErrors()).isEmpty();
    }

    public static <O extends DataObject> void assertRpcSuccess(Future<RpcResult<O>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        RpcResult<O> rpcResult = futureRpcResult.get(1, MINUTES);
        assertThat(rpcResult.isSuccessful()).isTrue();
        assertThat(rpcResult.getErrors()).isEmpty();
    }

    public static <T> void assertRpcErrorWithoutCausesOrMessages(Future<RpcResult<T>> futureRpcResult)
            throws InterruptedException, ExecutionException, TimeoutException {
        RpcResult<T> rpcResult = futureRpcResult.get(1, MINUTES);
        assertWithMessage("rpcResult.isSuccessful").that(rpcResult.isSuccessful()).isFalse();
        assertWithMessage("rpcResult.errors").that(rpcResult.getErrors()).isEmpty();
    }

    public static <T> void assertRpcErrorCause(Future<RpcResult<T>> futureRpcResult, Class<?> expectedExceptionClass,
            String expectedRpcErrorMessage) throws InterruptedException, ExecutionException, TimeoutException {
        assertRpcErrorCause(futureRpcResult.get(1, MINUTES), expectedExceptionClass, expectedRpcErrorMessage);
    }

    private static <T> void assertRpcErrorCause(RpcResult<T> rpcResult, Class<?> expected1stExceptionClass,
            String expected1stRpcErrorMessage) {
        assertWithMessage("rpcResult.isSuccessful").that(rpcResult.isSuccessful()).isFalse();
        Collection<RpcError> errors = rpcResult.getErrors();
        assertWithMessage("rpcResult.errors").that(errors).hasSize(1);
        RpcError error1 = errors.iterator().next();
        assertWithMessage("rpcResult.errors[0].errorType").that(error1.getErrorType()).isEqualTo(ErrorType.APPLICATION);
        assertWithMessage("rpcResult.errors[0].message").that(error1.getMessage())
            .isEqualTo(expected1stRpcErrorMessage);
        if (error1.getCause() != null) {
            // Check needed because FutureRpcResults does not propagate cause if OperationFailedException
            assertWithMessage("rpcResult.errors[0].cause").that(error1.getCause())
                .isInstanceOf(expected1stExceptionClass);
        }
    }

}
