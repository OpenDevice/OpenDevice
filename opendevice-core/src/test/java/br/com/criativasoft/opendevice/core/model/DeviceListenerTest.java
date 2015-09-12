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

package br.com.criativasoft.opendevice.core.model;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Ricardo JL Rufino
 * @date 30/08/15.
 */
public class DeviceListenerTest {

    @Mock
    private DeviceConnection connection;
    private LocalDeviceManager manager;

    private Device led1;
    private Device led2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        manager = new LocalDeviceManager();

        led1 = new Device(1, DeviceType.DIGITAL);
        led2 = new Device(2, DeviceType.DIGITAL);

        when(connection.isConnected()).thenReturn(true);
        manager.connect();

        // Led2 follow Led1 changes...
        manager.addListener(new DeviceListener() {
            @Override
            public void onDeviceChanged(Device device) {
                if (device == led1) {

                    if(led1.isON()) manager.findDevice(2).on(); // using dao.
                    if(led1.isOFF()) manager.findDevice(2).off(); // using dao.

                }
            }
        });
    }

    @Test
    public void testLedON() throws Exception {

        led1.on();

        assertTrue(led1.isON());
        assertTrue(led2.isON());


    }

    @Test
    public void testLedOFF() throws Exception {

        led1.off();

        assertTrue(led1.isOFF());
        assertTrue(led2.isOFF());


    }
}