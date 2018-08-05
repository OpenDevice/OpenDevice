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

package br.com.criativasoft.opendevice.middleware.rules;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.TenantContext;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.UserEventCommand;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.middleware.jobs.AbstractAction;
import br.com.criativasoft.opendevice.middleware.jobs.ActionException;
import br.com.criativasoft.opendevice.middleware.model.rules.ConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleEnums.ExecutionStatus;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleSpec;
import br.com.criativasoft.opendevice.middleware.persistence.dao.RuleSpecDao;
import br.com.criativasoft.opendevice.restapi.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * Rule System - responsible for checking the registered rules and conditions and performing the corresponding actions. <br/>
 * Specifications and definitions are available in class implementations: {@link RuleSpec}, {@link ConditionSpec}, {@link AbstractAction}
 * @author Ricardo JL Rufino
 * @date 01/11/16
 */
@Singleton
public class RuleManager implements RuleSpecDao, DeviceListener {

    private static final Logger log = LoggerFactory.getLogger(RuleManager.class);

    private Set<AbstractRule> rules = new LinkedHashSet();

    private RuleFactory ruleFactory;

    private Executor executor;

    private boolean started;

    @Inject
    private RuleSpecDao ruleSpecDao;

    public RuleManager(){
        ruleFactory = new RuleFactory();
        executor = ODev.getSharedExecutorService();
        BaseDeviceManager deviceManager = ODev.getDeviceManager();
        deviceManager.addListener(this);
    }

    public void setRuleSpecDao(RuleSpecDao ruleSpecDao) {
        this.ruleSpecDao = ruleSpecDao;
    }

    public void start(){


        // Load Initial Specs from datasource.
        List<RuleSpec> specs = ruleSpecDao.listAll();
        for (RuleSpec spec : specs) {
            spec.setStatus(ExecutionStatus.INACTIVE);
            addRule(spec);
        }


        started = true;

//        if(timer ==  null){
//
//            timer = new Timer();
//
//            log.info("Starting RuleManager Task");
//
//            TimerTask task = new TimerTask() {
//                @Override
//                public void run() {
//                    eval();
//                }
//            };
//
//            timer.scheduleAtFixedRate(task, 0, 1000);
//
//        }else{
//            if(log.isDebugEnabled()) log.debug("RuleManager already started ...");
//        }

    }

    public void addRule(RuleSpec spec) {
        synchronized (rules){
            AbstractRule rule = ruleFactory.create(spec);
            rules.add(rule);
        }

        if(started){

            fireRulesUpdate();

            if(spec.isEnabled() && spec.getCondition() != null){
                                // TODO: getDevice frim
                eval(spec.getAccount().getUuid(), null); // TODO: A direct call can affect performance in certain situations
            }
        }


    }


    /**
     * Checks the rules and conditions recorded, the ones that meet the specifications will be executed. <br/>
     * Execution is controlled through the class {@link ActionTask}
     * @see #executeActionsFor(List)
     */
    protected void eval(String tenantID, Device triggerDevice){

        boolean timeBased = (triggerDevice == null);

        List<AbstractRule> toExecute = new LinkedList<AbstractRule>();

        Iterator<AbstractRule> iterator = rules.iterator();

        while (iterator.hasNext()){

            AbstractRule rule =  iterator.next();

            if(!rule.getSpec().isEnabled()) continue;

            RuleSpec spec = rule.getSpec();

            long resourceID = spec.getResourceID();

            // If has tenantID, only execute of the same context
            if(tenantID != null && !spec.getAccount().getUuid().equals(tenantID)){
                continue;
            }

            if(rule instanceof IResourceRule && resourceID > 0){

                TenantProvider.setCurrentID(spec.getAccount().getUuid());
                TenantContext context = TenantProvider.getCurerntContext();

                Device device = context.getDeviceByUID((int) resourceID);
                if(device == null) {
                    spec.setStatus(ExecutionStatus.FAIL);
                    spec.setExecutionMessage("Device/Resource not found !");
                    continue;
                }

                ((IResourceRule) rule).setResource(device);

                // Ignore rules with don't match with triggerDevice
                if(triggerDevice != null && device.getId() != triggerDevice.getId()){
                    continue;
                }
            }

            // Timebased (called from RuleManagerJob) only work with TimeCondition
            if(timeBased && !AbstractRule.isTimedRule(spec)){
                continue;
            }

            if(rule.evaluate()){

                if(rule.allowExection()){ // This check if action can be executed.
                    toExecute.add(rule);
                }
            }else{
                if(spec.getStatus() != ExecutionStatus.INACTIVE){
                    spec.setStatus(ExecutionStatus.INACTIVE);
                    try{
                        ruleSpecDao.update(spec);
                    }catch (Exception ex){
                        log.error(ex.getMessage(), ex);
                    }
                    fireRulesUpdate();
                }

            }

        }

        if(!toExecute.isEmpty()){
            executeActionsFor(toExecute);
        }else{
            if(log.isTraceEnabled()) log.trace("No rules were met");
        }


    }

    private void executeActionsFor(List<AbstractRule> rules){
        if(log.isDebugEnabled()) log.debug("Executing rules : " + rules.size());

        for (AbstractRule rule : rules) {

            executor.execute(new ActionTask(rule));

            RuleSpec spec = rule.getSpec();
            spec.setStatus(ExecutionStatus.ACTIVE);
            spec.setLastExecution(new Date());

            try{
                ruleSpecDao.update(spec);
            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }

            // FIXME: Save executions history  ?!?!?
        }

        fireRulesUpdate();

    }

    private AbstractRule findRule(RuleSpec spec){

        for (AbstractRule rule : rules) {

            if(rule.getSpec().getId() == spec.getId()){
                return rule;
            }

        }

        return  null;
    }

    private void  fireRulesUpdate(){
        try {
            ODev.getDeviceManager().send(new UserEventCommand("rules_update"), false, true);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onDeviceChanged(Device device) {
        eval(device.getApplicationID(), device);
    }

    @Override
    public void onDeviceRegistred(Device device) {

    }


    @Override
    public RuleSpec getById(long id) {
        return ruleSpecDao.getById(id);
    }

    @Override
    public void persist(RuleSpec entity) {

        ruleSpecDao.persist(entity);

        addRule(entity);

    }

    @Override
    public RuleSpec update(RuleSpec entity) {

        if(!entity.isEnabled()) entity.setStatus(ExecutionStatus.INACTIVE);

        entity = ruleSpecDao.update(entity);

        AbstractRule rule = findRule(entity);

        if (rule != null) {
            rules.remove(rule);
            addRule(entity);
        }

        return entity;
    }

    @Override
    public void delete(RuleSpec entity) {

        AbstractRule rule = findRule(entity);

        rules.remove(rule);

        ruleSpecDao.delete(entity);
    }

    @Override
    public void refresh(RuleSpec entity) {
        ruleSpecDao.refresh(entity);
    }

    @Override
    public List<RuleSpec> listAll() {
        return ruleSpecDao.listAll();
    }

    @Override
    public List<RuleSpec> listAllByUser() {
        return ruleSpecDao.listAllByUser();
    }


    // ============================================================================
    // ActionTask
    // ============================================================================

    private static class ActionTask implements Runnable{

        private AbstractRule abstractRule;

        public ActionTask(AbstractRule abstractRule) {
            this.abstractRule = abstractRule;
        }

        @Override
        public void run() {

            ODev.getDeviceManager().transactionBegin();

            RuleSpec spec = abstractRule.getSpec();

            AbstractAction action = abstractRule.getAction();

            long resourceID = spec.getResourceID();

            if(resourceID > 0){
                TenantProvider.setCurrentID(spec.getAccount().getUuid());
                TenantContext context = TenantProvider.getCurerntContext();
                Device device = context.getDeviceByUID((int) resourceID);
                action.setTargetDevice(device);
            }

            Account account = spec.getAccount();

            TenantProvider.setCurrentID(account.getUuid());

            try {
                action.execute();
            } catch (ActionException e) {
                spec.setStatus(ExecutionStatus.FAIL);
                spec.setExecutionMessage(e.getMessage());
                log.error(e.getMessage(), e);
            }

            spec.setLastExecution(new Date());
            spec.setExecutionMessage("Success");

            TenantProvider.setCurrentID(null);

            ODev.getDeviceManager().transactionEnd();
        }
    }
}
