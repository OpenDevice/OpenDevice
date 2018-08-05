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

package br.com.criativasoft.opendevice.middleware.utils;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 *         Date: 21/07/18
 */
public class DeviceNameMatcherTest {

    private static final List<String> names = new ArrayList<>();
    private static final List<Device> devices = new ArrayList<>();

    static{
        names.add("Split da Sala");
        names.add("Split do Quarto");
        names.add("Ar-Condicionado do Escritório");
        names.add("Ar-Condicionado da Sala");
        names.add("Luz do Escritório");
        names.add("Luz do Banheiro");
        names.add("Fogão");
        names.add("TV da Sala");
        names.add("Luz da Sala");
        names.add("Luz do Quarto");
        names.add("Ventilador do Quarto");
        names.add("Luz da Entrada");
    }

    @BeforeClass
    public static void setUp() throws Exception {

        for (String name : names) {
            Device device = new PhysicalDevice(-1, name, Device.DIGITAL);
            device.setTitle(name);
            devices.add(device);
        }
    }

    @Test
    public void testUsingVerbsON() throws InterruptedException {

        DeviceNameMatcher matcher = new DeviceNameMatcher(devices, false);

        Device device = matcher.process("ligar a luz da sala");

        assertNotNull(device);

        assertEquals("Luz da Sala", device.getTitle());

        // test 2 - no article

        device = matcher.process("ligar luz da sala");

        assertNotNull(device);

        assertEquals("Luz da Sala", device.getTitle());

        // test 4 -  no verb

        device = matcher.process("luz da sala");

        assertNotNull(device);

        assertEquals("Luz da Sala", device.getTitle());

    }


    @Test
    public void testUsingVerbsON2() throws InterruptedException {

        DeviceNameMatcher matcher = new DeviceNameMatcher(devices, false);

        Device device = matcher.process("ligar o ar-condicionado do escritório");

        assertNotNull(device);

        assertEquals("Ar-Condicionado do Escritório", device.getTitle());

        // test 2 - no accents

        device = matcher.process("ligar o ar-condicionado do escritorio");

        assertNotNull(device);

        assertEquals("Ar-Condicionado do Escritório", device.getTitle());

        // test 2 - no hifen

        device = matcher.process("ligar ar condicionado do escritorio");

        assertNotNull(device);

        assertEquals("Ar-Condicionado do Escritório", device.getTitle());

    }


    @Test
    public void testUsingVerbsOFF() throws InterruptedException {

        DeviceNameMatcher matcher = new DeviceNameMatcher(devices, false);

        Device device = matcher.process("desligar a luz da sala");

        assertNotNull(device);

        assertEquals("Luz da Sala", device.getTitle());

        // test 2 - no article

        device = matcher.process("desligar luz da sala");

        assertNotNull(device);

        assertEquals("Luz da Sala", device.getTitle());


        // test other device

        device = matcher.process("desligar o split do quarto");

        assertNotNull(device);

        assertEquals("Split do Quarto", device.getTitle());


        // test - other device

        device = matcher.process("desligar o ar-condicionado do escritorio");

        assertNotNull(device);

        assertEquals("Ar-Condicionado do Escritório", device.getTitle());
    }

    @Test
    public void testNotFound() throws InterruptedException {

        DeviceNameMatcher matcher = new DeviceNameMatcher(devices, false);

        Device device = matcher.process("da sala");

        assertEquals(null, device);


        device = matcher.process("ar-condicionado");

        assertEquals(null, device);

    }

}
