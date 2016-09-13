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

package br.com.criativasoft.opendevice.middleware;

import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.dao.memory.DeviceMemoryDao;
import br.com.criativasoft.opendevice.restapi.ApiDataManager;
import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/09/16
 */
public class MainDataManager implements ApiDataManager {

    private DeviceDao deviceDao = new DeviceMemoryDao();

    // TODO: Add dashboards.

    @Override
    public AccountDao getAccountDao() {
        return new AccountDao() {
            @Override
            public Account getAccountByApiKey(String key) {
                throw new IllegalStateException("not implemented");
            }
        };
    }

    @Override
    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

    @Override
    public void setDeviceDao(DeviceDao dao) {
        this.deviceDao = dao;
    }
}
