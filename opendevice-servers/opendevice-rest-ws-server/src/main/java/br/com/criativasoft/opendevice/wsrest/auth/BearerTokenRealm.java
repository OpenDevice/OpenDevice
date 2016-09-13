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
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;

public class BearerTokenRealm extends AuthenticatingRealm  {

    private DeviceManager manager;

    public BearerTokenRealm(DeviceManager manager) {
        this.manager = manager;
        setAuthenticationTokenClass(BearerAuthenticationToken.class);
    }

    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        BearerAuthenticationToken authToken = (BearerAuthenticationToken)token;

        String authTokenS = (String) authToken.getPrincipal();

        // FIXME: Find user by Auth token...
        // NEED USE CACHE OU DATABASE

        DataManager context = manager.getDataManager();

        String apiKey = /* TODO : find on cache */ authTokenS;

        if(context instanceof ApiDataManager){

            AccountDao dao = ((ApiDataManager) context).getAccountDao();

            Account account = dao.getAccountByApiKey(apiKey);

            if(account != null){
                return new SimpleAuthenticationInfo(authToken.getPrincipal(), authToken.getCredentials(), "BearerTokenRealm");
            }
        }

        return null;
    }
}