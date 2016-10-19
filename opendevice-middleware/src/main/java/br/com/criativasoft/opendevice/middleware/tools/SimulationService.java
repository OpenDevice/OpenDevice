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

package br.com.criativasoft.opendevice.middleware.tools;

import br.com.criativasoft.opendevice.core.TenantContext;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.util.ODevThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Tool to generate random data.
 * @author Ricardo JL Rufino
 * @date 16/10/16
 */
public class SimulationService {

    /* TODO: Enchacement - Improve the simulation allowing you to add multiple devices on the same task,
     * and agrupas the Tasks for Tenant to allow use fewer threads in a multithred environment.
     * https://github.com/OpenDevice/OpenDevice/issues/71
     */

    private ThreadPoolExecutor executor;

    private ConcurrentHashMap<Device, Future> taskQueue;

    public SimulationService() {
        taskQueue = new ConcurrentHashMap<Device, Future>(4); // MaxSize + Queue Size
        executor = new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        executor.setThreadFactory(new ODevThreadFactory("Simulation"));
    }

    public boolean start(TenantContext context, Device device, int interval){

        if(taskQueue.get(device) != null) stop(device);

        try{
            Future<?> future = executor.submit(new SimulationTask(context, device, interval));

            taskQueue.put(device, future);

            return true;

        }catch (RejectedExecutionException e){

            return false;

        }

    }

    public void stop(Device device) {

        Future future = taskQueue.get(device);

        if(future != null){

            future.cancel(true);

            if(future.isDone() || future.isCancelled()){
                taskQueue.remove(device);
            }

        }
    }

    public List<Device> list(TenantContext context){
        List<Device> list = new LinkedList<Device>();
        for (Device device : taskQueue.keySet()) {
            if(context.getId().equals(device.getApplicationID()))
                list.add(device);
        }
        return  list;
    }
}
