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

import br.com.criativasoft.opendevice.core.util.StringUtils;
import br.com.criativasoft.opendevice.middleware.model.jobs.CronJobSpec;
import br.com.criativasoft.opendevice.middleware.model.jobs.JobSpec;
import br.com.criativasoft.opendevice.middleware.persistence.dao.JobSpecDao;
import br.com.criativasoft.opendevice.middleware.rules.RuleManager;
import br.com.criativasoft.opendevice.middleware.rules.RuleManagerJob;
import br.com.criativasoft.opendevice.middleware.rules.action.ActionFactory;
import org.knowm.sundial.SundialJobScheduler;
import org.quartz.core.JobExecutionContext;
import org.quartz.core.Scheduler;
import org.quartz.exceptions.SchedulerException;
import org.quartz.listeners.TriggerListener;
import org.quartz.triggers.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Ricardo JL Rufino
 * @date 05/11/16
 */
@Singleton
public class JobManager implements JobSpecDao{

    private static final Logger log = LoggerFactory.getLogger(JobManager.class);

    @Inject
    private JobSpecDao dao;

    @Inject
    private RuleManager ruleManager;

    private TriggerListener triggerListener;

    private Scheduler scheduler;

    public JobManager(){
        triggerListener = new TriggerListenerImpl();
        SundialJobScheduler.createScheduler(new ODevSchedulerFactory());
        scheduler = SundialJobScheduler.getScheduler();
    }

    public void start(){

        createDefaultJobs();

        // Load Initial Specs from datasource.
        List<JobSpec> specs = dao.listAll();
        for (JobSpec spec : specs) {
            if(spec.isEnabled()){
                addJob(spec);
            }
        }

        try {
            scheduler.getListenerManager().addTriggerListener(triggerListener);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }

        SundialJobScheduler.startScheduler();
    }


    private void addJob(JobSpec spec) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action", new ActionFactory().create(spec.getAction()));
        params.put("spec", spec);
        String name = getJobName(spec);
        SundialJobScheduler.addJob(name, ActionJob.class, params, false);

        if(spec instanceof CronJobSpec){

            String exp  = "0 " + ((CronJobSpec) spec).getCronExpression();

            // Replace day-of-week
            if(exp.endsWith("*")) exp = exp.substring(0, exp.length() - 1) + "?";
            // replace day-of-month
            else {
                String[] expList = exp.split(" ");
                expList[3] = "?";
                exp = StringUtils.join(expList, " ");
            }

            SundialJobScheduler.addCronTrigger("Trigger-"+name, name, exp);
        }

    }


    private String getJobName(JobSpec spec) {
        return spec.getClass().getSimpleName()+ "#" + spec.getId();
    }

    private void createDefaultJobs(){

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("instance", ruleManager);
        String name = "RuleManager";
        SundialJobScheduler.addJob(name, RuleManagerJob.class, params, false);
        SundialJobScheduler.addSimpleTrigger("Trigger-"+name, name, -1, 1000);

        // HACK:Rule Manager, does not always run in the range of 1 second when the previous execution takes more time,
        // forcing the Quartz to sleep for 30sec. This Job makes him active.
        name = "QuartzWakeupJob";
        SundialJobScheduler.addJob(name, QuartzWakeupJob.class);
        SundialJobScheduler.addSimpleTrigger("Trigger-"+name, name, -1, 1000);

    }

    private class TriggerListenerImpl implements TriggerListener{

        @Override
        public String getName() {
            return "JobManager";
        }

        @Override
        public void triggerFired(Trigger trigger, JobExecutionContext context) {

        }

        @Override
        public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
            return false;
        }

        @Override
        public void triggerMisfired(Trigger trigger) {

        }

        @Override
        public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {

        }
    }



    // =========================================================================
    // JobSpecDao
    // =========================================================================

    @Override
    public JobSpec getById(long id) {
        return dao.getById(id);
    }

    @Override
    public void persist(JobSpec entity) {

        dao.persist(entity);

        entity.setEnabled(true);

        addJob(entity);
    }


    @Override
    public JobSpec update(JobSpec entity) {

        entity = dao.update(entity); // rync database...

        try {

            scheduler.deleteJob(getJobName(entity));

            if(entity.isEnabled())  addJob(entity);

        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }

        return dao.update(entity); // sync addJob changes...
    }

    @Override
    public void delete(JobSpec entity) {

        try {
            scheduler.deleteJob(getJobName(entity));
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }

        dao.delete(entity);
    }

    @Override
    public void refresh(JobSpec entity) {
        dao.refresh(entity);
    }

    @Override
    public List<JobSpec> listAll() {
        return dao.listAll();
    }

    @Override
    public List<JobSpec> listAllByUser() {
        return dao.listAllByUser();
    }
}
