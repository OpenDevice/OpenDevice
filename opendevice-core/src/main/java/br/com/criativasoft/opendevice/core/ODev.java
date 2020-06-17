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

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.core.util.ODevExecutors;

import java.util.concurrent.ExecutorService;

/**
 * Class for obtaining the main features of the OpenDevice.
 * Note: Many of the features are still concentrated in class: {@link BaseDeviceManager}
 *
 * @author Ricardo JL Rufino
 * @date 04/11/16
 * @see #getDeviceManager()
 */
public final class ODev {

    private ODev() {
    }

    public static BaseDeviceManager getDeviceManager() {
        return BaseDeviceManager.getInstance();
    }

    public static ExecutorService getSharedExecutorService() {
        return ODevExecutors.getSharedExecutorService();
    }

    public static OpenDeviceConfig getConfig() {
        return OpenDeviceConfig.get();
    }

    public static Device findDevice(int uid) {
        return BaseDeviceManager.getInstance().findDeviceByUID(uid);
    }

}
