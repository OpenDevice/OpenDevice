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

package br.com.criativasoft.opendevice.core.event.impl;

import br.com.criativasoft.opendevice.core.event.EventContext;
import br.com.criativasoft.opendevice.core.event.EventException;
import br.com.criativasoft.opendevice.core.event.EventHandler;

import java.io.IOException;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 28/08/15.
 */
public class SystemEventHandler implements EventHandler {

    @Override
    public void execute(String code, EventContext context) throws EventException {
        try {
            Process p = Runtime.getRuntime().exec(code);
        } catch (IOException e) {
            throw new EventException("Error executing system command", e);
        }
    }

    @Override
    public String getHandlerType() {
        return "System";
    }
}
