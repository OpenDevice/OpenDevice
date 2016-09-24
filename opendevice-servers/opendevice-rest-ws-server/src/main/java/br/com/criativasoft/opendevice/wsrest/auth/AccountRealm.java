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

package br.com.criativasoft.opendevice.wsrest.auth;

import br.com.criativasoft.opendevice.core.DataManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.restapi.ApiDataManager;
import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.AuthenticatingRealm;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 22/09/16
 */
public class AccountRealm  extends AuthenticatingRealm {


    private DeviceManager manager;

    public AccountRealm(DeviceManager manager) {
        this.manager = manager;
        setAuthenticationTokenClass(UsernamePasswordToken.class);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken user = (UsernamePasswordToken) token;

        DataManager context = manager.getDataManager();

        if( context instanceof ApiDataManager){

            AccountDao dao = ((ApiDataManager) context).getAccountDao();

            Account account = dao.getAccount(user.getUsername(), new String(user.getPassword()));

            if(account != null){
                // TODO: check if need return password
                return new SimpleAuthenticationInfo(account.getUuid(), account.getPassword(), "AccountRealm");
            }
        }



        return null;
    }
}
