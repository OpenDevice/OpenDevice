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
import br.com.criativasoft.opendevice.wsrest.resource.AuthRest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BearerTokenRealm extends AuthenticatingRealm  {

    private static final Logger log = LoggerFactory.getLogger(BearerTokenRealm.class);

    private DeviceManager manager;

    public BearerTokenRealm(DeviceManager manager) {
        this.manager = manager;
        setAuthenticationCachingEnabled(true);
        setAuthenticationTokenClass(BearerAuthenticationToken.class);
    }

    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        BearerAuthenticationToken authToken = (BearerAuthenticationToken)token;

        String authTokenS = (String) authToken.getPrincipal();

        DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
        Cache<Object, Object> cache = securityManager.getCacheManager().getCache(AuthRest.TOKEN_CACHE);

        DataManager context = manager.getDataManager();

        String apiKey = (String) cache.get(authTokenS);

        if(apiKey == null) log.warn("ApiKey not found for token : " + authTokenS);

        if(  apiKey != null && context instanceof ApiDataManager){

            AccountDao dao = ((ApiDataManager) context).getAccountDao();

            Account account = dao.getAccountByApiKey(apiKey);

            if(account != null){
                return new SimpleAuthenticationInfo(account.getUuid(), authToken.getCredentials(), "BearerTokenRealm");
            }
        }

        return null;
    }
}