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

import br.com.criativasoft.opendevice.core.event.EventContext;
import br.com.criativasoft.opendevice.core.event.EventException;
import br.com.criativasoft.opendevice.core.event.EventHandler;
import br.com.criativasoft.opendevice.core.event.EventHook;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * This class implements the event processing ({@link EventHook}) using JavaScript.
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

        try {
            OpenDeviceJSEngine.run(code, bindings);

        } catch (ScriptException e) {
           throw new EventException(e);
        }

    }


}
