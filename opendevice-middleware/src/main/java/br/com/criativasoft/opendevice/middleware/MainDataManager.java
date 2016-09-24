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
import br.com.criativasoft.opendevice.restapi.ApiDataManager;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/09/16
 */
public class MainDataManager implements ApiDataManager {

    @Inject
    private EntityManager em;

    @Inject
    private DeviceDao deviceDao;

    @Inject
    private AccountDao accountDao;

    @Inject
    private UserDao userDao;

    public MainDataManager() {
    }

    public MainDataManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public AccountDao getAccountDao() {
        return accountDao;
    }

    @Override
    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

    @Override
    public void setDeviceDao(DeviceDao dao) {
        this.deviceDao = dao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
