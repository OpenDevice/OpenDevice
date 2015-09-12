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

package br.com.criativasoft.opendevice.core.event;

import org.junit.Before;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class FileHookScannerTest {

    private FileHookScanner scanner;

    @Before
    public void setUp() throws Exception {
        scanner = new FileHookScanner();
    }

    @org.junit.Test
    public void testParse() throws Exception {
        URL resource = getClass().getResource("/js/TestFileHookScanner1.js");
        assertNotNull(resource);

        EventHook hook = scanner.parse(new File(resource.getPath()));

        assertEquals("EventHookTest", hook.getName());
        assertEquals("JavaScript",hook.getType());
        assertNotNull(hook.getDescription());
        assertNotNull(hook.getDeviceIDs());
        assertNotNull(hook.getHandler());
        assertTrue(hook.getDeviceIDs().size() == 3);

    }

    @org.junit.Test
    public void testParse2() throws Exception {
        URL resource = getClass().getResource("/js/TestFileHookScanner2.js");
        assertNotNull(resource);
        EventHook hook = scanner.parse(new File(resource.getPath()));
        assertEquals("EventHookTest", hook.getName());
        assertArrayEquals(hook.getDeviceIDs().toArray(), new Integer[]{1, 2, 3});

    }
}