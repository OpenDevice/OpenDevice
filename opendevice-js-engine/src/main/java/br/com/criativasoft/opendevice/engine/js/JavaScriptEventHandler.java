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

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.event.EventContext;
import br.com.criativasoft.opendevice.core.event.EventException;
import br.com.criativasoft.opendevice.core.event.EventHandler;
import br.com.criativasoft.opendevice.core.event.EventHook;
import br.com.criativasoft.opendevice.core.model.Device;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.io.FileNotFoundException;

/**
 * This class implements the event processing system ({@link EventHook}) using JavaScript.
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class JavaScriptEventHandler implements EventHandler {

    @Override
    public String getHandlerType() {
        return "JavaScript";
    }

    @Override
    public void execute(String code, EventContext context) throws EventException {

        SimpleBindings bindings = new SimpleBindings(context);

        // FIXME remove this !!!!
        Device device = (Device) context.get("device");
        bindings.put("testdesc", "Device " + device.getUid() + " -> " + device.getValue());

        try {
            OpenDeviceJSEngine.run(code, bindings);

        } catch (ScriptException e) {
           throw new EventException(e);
        }

    }


}
