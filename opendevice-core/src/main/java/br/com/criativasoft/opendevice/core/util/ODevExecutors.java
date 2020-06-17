/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Ricardo JL Rufino
 * @date 04/11/16
 */
public final class ODevExecutors {

    private ODevExecutors() {
    } //

    private static ExecutorService executorService;

    /**
     * Obtains a shared Executor used to perform small activities, The default implementation uses the ThreadPoolExecutor
     *
     * @return
     */
    public static ExecutorService getSharedExecutorService() {

        if (executorService == null) {

            ODevThreadFactory threadFactory = new ODevThreadFactory("Shared");

            // TODO: It would be interesting to set the maximum number of threads

            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), threadFactory
            );

        }

        return executorService;
    }
}
