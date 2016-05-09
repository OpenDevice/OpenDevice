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

package br.com.criativasoft.opendevice.engine.js;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.event.EventHookManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.script.SimpleBindings;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

/**
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class EventHandler2Test {

    @Mock
    private DeviceConnection connection;

    private LocalDeviceManager manager;
    private EventHookManager eventManager;
    private Device led1;
    private Device led2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        manager = new LocalDeviceManager();
        eventManager = manager.getEventManager();

        EventHookManager.registerHandler(JavaScriptEventHandler.class);
        String path = getClass().getResource("/js/TestEventHandler2.js").getFile();
        eventManager.addHook(new File(path));

        led1 = new Device(1, DeviceType.DIGITAL);
        led2 = new Device(2, DeviceType.DIGITAL);

        when(connection.isConnected()).thenReturn(true);
        manager.connect();

        led2.addListener(device -> {
            if (device == led2 && device.isON()) System.out.println("manager led(2) change.... " + led2.getName());
        });

        // Initialize
        OpenDeviceJSEngine.run("print(\"Setup\")",  new SimpleBindings());
    }



    @Test
    public void testFollowChanges() throws Exception {

        led1.on();

        await().atMost(1, TimeUnit.SECONDS).until(() -> {
            return led2.isON();
        });

        Assert.assertTrue(led1.isON());
        Assert.assertTrue(led2.isON());

        led1.off();

        await().atMost(1, TimeUnit.SECONDS).until(() -> {
            return led2.isOFF();
        });

        Assert.assertTrue(led1.isOFF());
        Assert.assertTrue(led2.isOFF());

    }

}