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

import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.middleware.model.actions.ControlActionSpec;
import org.knowm.sundial.SundialJobScheduler;
import org.quartz.core.JobExecutionContext;
import org.quartz.core.Scheduler;
import org.quartz.exceptions.SchedulerException;
import org.quartz.listeners.TriggerListener;
import org.quartz.triggers.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 05/11/16
 */
public class JobManagerTest {

    private static final Logger log = LoggerFactory.getLogger(JobManagerTest.class);

    public static void main(String[] args) {

        DeviceManager manager = new LocalDeviceManager();
        manager.addDevice(new PhysicalDevice(1));

        // TODO: Criar triggers padrão:  SUNSET, MINDNIGHT

        SundialJobScheduler.createScheduler(new ODevSchedulerFactory());

        ControlActionSpec actionSpec = new ControlActionSpec();
        actionSpec.setResourceID(1);
        actionSpec.setValue(1);
        Map<String, Object> params = new HashMap<String, Object>();
        ActionFactory factory = new ActionFactory();
        params.put("action", factory.create(actionSpec));


        String name = "ActionJOB#"+actionSpec.getId();
        SundialJobScheduler.addJob(name, ActionJob.class, params, false);
        SundialJobScheduler.addSimpleTrigger("Trigger-"+name, name, 2, 1000);


        Scheduler scheduler = SundialJobScheduler.getScheduler();

        try {
            scheduler.getListenerManager().addTriggerListener(new TriggerListener() {
                @Override
                public String getName() {
                    return "OpenDevice";
                }

                @Override
                public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    //System.out.println("trigger : " + trigger + ", jobExecutionContext : " + jobExecutionContext);
                }

                @Override
                public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
                    return false;
                }

                @Override
                public void triggerMisfired(Trigger trigger) {
                    System.out.println("trigger : " + trigger );
                }

                @Override
                public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, Trigger.CompletedExecutionInstruction completedExecutionInstruction) {
                    // System.out.println("triggerComplete - trigger : " + trigger + ", jobExecutionContext : " + jobExecutionContext);
                }
            });
        } catch (SchedulerException e) {
            e.printStackTrace();
        }


        SundialJobScheduler.startScheduler();
        log.debug("SundialJobScheduler started.Jobs:[{}]", SundialJobScheduler.getAllJobNames());
    }
}
