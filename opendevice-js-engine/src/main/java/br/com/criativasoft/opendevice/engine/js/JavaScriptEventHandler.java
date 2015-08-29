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

import br.com.criativasoft.opendevice.core.event.EventException;
import br.com.criativasoft.opendevice.core.event.EventHandler;
import br.com.criativasoft.opendevice.core.event.EventHook;

/**
 * This class implements the event processing system ({@link EventHook}) using JavaScript.
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class JavaScriptEventHandler implements EventHandler {

    @Override
    public void execute(String code) throws EventException {

    }

    @Override
    public String getHandlerType() {
        return "JavaScript";
    }
}
