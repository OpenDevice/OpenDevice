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

package br.com.criativasoft.opendevice.middleware.jobs;

import org.quartz.QuartzScheduler;
import org.quartz.core.*;
import org.quartz.exceptions.SchedulerException;
import org.quartz.plugins.management.ShutdownHookPlugin;

/**
 * QuartzScheduler configuration
 * @author Ricardo JL Rufino
 * @date 06/11/16
 */
public class ODevSchedulerFactory extends SchedulerFactory {

    private int threadPoolSize = 2;

    private QuartzScheduler quartzScheduler;

    @Override
    public Scheduler getScheduler() throws SchedulerException {
        // Setup SimpleThreadPool
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //
        SimpleThreadPool threadPool = new SimpleThreadPool();
        threadPool.setThreadCount(threadPoolSize);

        // Setup RAMJobStore
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //
        JobStore jobstore = new RAMJobStore();

        // Set up any TriggerListeners
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        boolean tpInited = false;
        boolean qsInited = false;

        // Fire everything up
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        try {

            JobRunShellFactory jrsf = new StandardJobRunShellFactory(); // Create correct run-shell factory...

            QuartzSchedulerResources quartzSchedulerResources = new QuartzSchedulerResources();
            quartzSchedulerResources.setThreadName("Quartz Scheduler Thread");
            quartzSchedulerResources.setJobRunShellFactory(jrsf);
            quartzSchedulerResources.setMakeSchedulerThreadDaemon(false);
            quartzSchedulerResources.setThreadsInheritInitializersClassLoadContext(true);
            quartzSchedulerResources.setBatchTimeWindow(0L);
            quartzSchedulerResources.setMaxBatchSize(1);
            quartzSchedulerResources.setInterruptJobsOnShutdown(true);
            quartzSchedulerResources.setInterruptJobsOnShutdownWithWait(true);
            quartzSchedulerResources.setThreadPool(threadPool);
            threadPool.setThreadNamePrefix("Quartz_Scheduler_Worker");
            threadPool.initialize();
            tpInited = true;

            quartzSchedulerResources.setJobStore(jobstore);

            quartzScheduler = new QuartzScheduler(quartzSchedulerResources);
            qsInited = true;


            ShutdownHookPlugin shutdownHookPlugin = new ShutdownHookPlugin();
            quartzSchedulerResources.addSchedulerPlugin(shutdownHookPlugin);

            // fire up job store, and runshell factory
            jobstore.initialize(quartzScheduler.getSchedulerSignaler());
            jobstore.setThreadPoolSize(threadPool.getPoolSize());

            jrsf.initialize(quartzScheduler);

            quartzScheduler.initialize(); // starts the thread

            return quartzScheduler;

        } catch (SchedulerException e) {
            if (qsInited) {
                quartzScheduler.shutdown(false);
            } else if (tpInited) {
                threadPool.shutdown(false);
            }
            throw e;
        } catch (RuntimeException re) {
            if (qsInited) {
                quartzScheduler.shutdown(false);
            } else if (tpInited) {
                threadPool.shutdown(false);
            }
            throw re;
        } catch (Error re) {
            if (qsInited) {
                quartzScheduler.shutdown(false);
            } else if (tpInited) {
                threadPool.shutdown(false);
            }
            throw re;
        }
    }
}
