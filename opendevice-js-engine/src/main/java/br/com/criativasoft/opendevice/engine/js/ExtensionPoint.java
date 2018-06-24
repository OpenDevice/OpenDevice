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

import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.extension.OpenDeviceExtension;
import br.com.criativasoft.opendevice.core.event.EventHookManager;

/**
 * OpenDevice ExtensionPoint Point
 * @author Ricardo JL Rufino
 * @date 30/08/15.
 */
public class ExtensionPoint extends OpenDeviceExtension {

    @Override
    public String getName() {
        return "opendevice-js-engine";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public void init(DeviceManager manager) {
        EventHookManager.registerHandler(JavaScriptEventHandler.class);
    }

    @Override
    public void destroy() {

    }
}
