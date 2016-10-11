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

package br.com.criativasoft.opendevice.core.dao.memory;

import br.com.criativasoft.opendevice.core.DataManager;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;

/**
 * DataManager using {@link DeviceDaoMemory}
 *
 * @author Ricardo JL Rufino
 * @date 10/10/16
 */
public class LocalDataManager implements DataManager {

    private DeviceDao deviceDao = new DeviceDaoMemory();

    @Override
    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

    @Override
    public void setDeviceDao(DeviceDao dao) {
        this.deviceDao = dao;
    }
}
