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
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static java.lang.Thread.sleep;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 16/10/16
 */
public class SimulationTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SimulationTask.class);

    private TenantContext context;
    private Device device;
    private Random random;

    private int interval;


    public SimulationTask(TenantContext context, Device device, int interval) {
        this.context = context;
        this.device = device;
        this.interval = interval;
        random = new Random();
    }


    @Override
    public void run() {

        TenantProvider.setCurrentID(context.getId());

        while(!Thread.interrupted()){

            if(device.getType() == Device.DIGITAL){
                device.toggle();
            }
            if(device.getType() == Device.ANALOG){
                device.setValue(random.nextInt(1000));
            }

            try {
                sleep(interval * 1000);
            } catch (InterruptedException e) {
                log.info("InterruptedException - Simulation : " + device);
                break;
            }

        }

        log.info("Simulation Finish : " + device);
        TenantProvider.setCurrentID(null);
    }

    @Override
    public String toString() {
        return "SimulationTask[Device:"+device.getId()+"]";
    }

    public Device getDevice() {
        return device;
    }
}
