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

import br.com.criativasoft.opendevice.core.event.impl.SystemEventHandler;
import br.com.criativasoft.opendevice.core.model.Device;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 28/08/15.
 */
public class SystemEventHandlerTest extends TestCase {

    public void testExecute(){

        EventHookManager manager = new EventHookManager();

        Class<SystemEventHandler> systemEventHandlerClass = SystemEventHandler.class;

        EventHook hook = new EventHook();
        hook.setHandler("gedit");
        hook.setType("System");
        hook.setDeviceIDs(Arrays.asList(1, 2));

        manager.setDao(new EventHookDaoFake(hook));

        Device led = new Device(1, Device.DIGITAL);

        manager.onDeviceChanged(led);

    }
}
