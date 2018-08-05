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

import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.core.model.Sensor;
import br.com.criativasoft.opendevice.middleware.model.actions.ControlActionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.ActiveTimeConditionSpec;
import br.com.criativasoft.opendevice.middleware.model.rules.RuleEnums;
import br.com.criativasoft.opendevice.middleware.model.rules.StateRuleSpec;
import br.com.criativasoft.opendevice.restapi.model.Account;
import org.junit.Before;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;

/**
 *
 * @author Ricardo JL Rufino
 * @date 03/11/16
 */
public class StateRuleActiveTimeTest {

    private DeviceManager manager;
    private RuleManager ruleManager;
    private Timer timer;

    @Before
    public void setUp() throws Exception {
        manager = new LocalDeviceManager();
        manager.addDevice(new Sensor(1, Device.DIGITAL));
        manager.addDevice(new PhysicalDevice(2, Device.DIGITAL));

        ruleManager = new RuleManager();
        ruleManager.setRuleSpecDao(new RuleSpecDaoTest());
        manager.addListener(ruleManager);
    }

    private void startTimer(){
        if(timer != null) timer.cancel();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ruleManager.eval(null, null);
            }
        };

        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public StateRuleSpec createSpecActiveTime(int value, int time){
        return createSpecActiveTime(value, time, 2);
    }

    public StateRuleSpec createSpecActiveTime(int value, int time, int targetDevice){

        Account account = new Account();
        account.setUuid("1");

        StateRuleSpec spec = new StateRuleSpec();
        spec.setResourceID(1);
        spec.setValue(value);
        spec.setAccount(account);
        spec.setEnabled(true);

        ActiveTimeConditionSpec condition = new ActiveTimeConditionSpec();
        condition.setTime(time);
        condition.setIntervalType(RuleEnums.IntervalType.SECOND);
        spec.setCondition(condition);

        ControlActionSpec action = new ControlActionSpec();
        action.setValue(1);
        action.setResourceID(targetDevice);

        spec.setAction(action);

        return spec;
    }

    public StateRuleSpec createFollow(int value, int followDeviceID, int targetDevice){

        Account account = new Account();
        account.setUuid("1");

        StateRuleSpec spec = new StateRuleSpec();
        spec.setResourceID(followDeviceID);
        spec.setValue(value);
        spec.setAccount(account);
        spec.setEnabled(true);

        ControlActionSpec action = new ControlActionSpec();
        action.setValue(value);
        action.setResourceID(targetDevice);

        spec.setAction(action);

        return spec;
    }

    @Test
    public void testWithoutCondition() throws InterruptedException {

        StateRuleSpec spec1 = new StateRuleSpec();
        spec1.setResourceID(1);
        spec1.setValue(0);

        StateRuleSpec spec2 = new StateRuleSpec();
        spec2.setResourceID(1);
        spec2.setValue(1);

        ruleManager.addRule(spec1);
        ruleManager.addRule(spec2);

        startTimer();
        Thread.sleep(100);

        assertNotNull(spec1.getLastExecution());
        assertNull(spec2.getLastExecution());

    }

    @Test
    public void testWithoutConditionInvalidDevice() throws InterruptedException {

        StateRuleSpec spec1 = new StateRuleSpec();
        spec1.setResourceID(1000);
        spec1.setValue(0);

        ruleManager.addRule(spec1);

        startTimer();
        Thread.sleep(100);

        assertEquals(RuleEnums.ExecutionStatus.FAIL, spec1.getStatus());

    }

    @Test
    public void testActiveTimeON() throws InterruptedException {

        Device device = manager.findDeviceByUID(1);
        device.setValue(1);

        StateRuleSpec spec1 = createSpecActiveTime(/*value=*/1, /*time=*/1);

        ruleManager.addRule(spec1);

        startTimer();

        Thread.sleep(500);
        assertNull(spec1.getLastExecution());
        Thread.sleep(500);
        assertNull(spec1.getLastExecution());
        Thread.sleep(500);
        assertNotNull(spec1.getLastExecution());

    }

    @Test
    public void testActiveTimeOFF() throws InterruptedException {

        Device device = manager.findDeviceByUID(1);
        device.setValue(0);

        StateRuleSpec spec1 = createSpecActiveTime(/*value=*/0, /*time=*/1);

        ruleManager.addRule(spec1);

        startTimer();

        Thread.sleep(500);
        assertNull(spec1.getLastExecution());

        Thread.sleep(1000);
        assertNotNull(spec1.getLastExecution());

    }

    @Test
    public void testActiveTimeDetectLowChange() throws InterruptedException {
        Device device = manager.findDeviceByUID(1);
        device.setValue(1);

        StateRuleSpec spec1 = createSpecActiveTime(/*value=*/0, /*time=*/1);

        ruleManager.addRule(spec1);

        startTimer();
        Thread.sleep(500);
        assertNull(spec1.getLastExecution());

        device.setValue(0); // CHANGE!
        Thread.sleep(1600); // Used because off time error between ruleManager checks
        assertNotNull(spec1.getLastExecution());
    }


    @Test
    public void testDeactiveRule() throws InterruptedException {
        Device device = manager.findDeviceByUID(1);
        device.setValue(1);

        StateRuleSpec spec1 = createSpecActiveTime(/*value=*/1, /*time=*/1);

        ruleManager.addRule(spec1);

        startTimer();
        Thread.sleep(2000);
        assertNotNull(spec1.getLastExecution());
        assertEquals(spec1.getStatus(), RuleEnums.ExecutionStatus.ACTIVE);

        device.setValue(0);
        Thread.sleep(2000);
        assertEquals(spec1.getStatus(), RuleEnums.ExecutionStatus.INACTIVE);
    }


    /**
     * Rule1: Self controller
     * - On device 1 ON, after 2 secons goto OFF
     *
     * Rule2: Device 2 Follow chante on device 1
     *
     * @throws InterruptedException
     */
    @Test
    public void testActiveTimeRuleChain() throws InterruptedException {
        Device device = manager.findDeviceByUID(1);
        Device device2 = manager.findDeviceByUID(2);

        StateRuleSpec spec1 = createSpecActiveTime(/*value=*/1, /*time=*/1, /*target=*/1);

        //  change to OFF ofter time
        ControlActionSpec action = (ControlActionSpec) spec1.getAction();
        action.setValue(0);

        ruleManager.addRule(spec1);
        ruleManager.addRule(createFollow(/*value=*/0, /*source=*/1, /*target=*/2));
        ruleManager.addRule(createFollow(/*value=*/1, /*source=*/1, /*target=*/2));

        startTimer();

        device.setValue(1);
        Thread.sleep(1000);
        assertEquals(1f, device2.getValue(), 0); // follow change ???

        Thread.sleep(3000);
        assertNotNull(spec1.getLastExecution());
        assertEquals(0f, device.getValue(), 0); // action trigger ?

        assertEquals(0f, device2.getValue(), 0); // follow change (over time) ??? (rule chain)

    }


}
