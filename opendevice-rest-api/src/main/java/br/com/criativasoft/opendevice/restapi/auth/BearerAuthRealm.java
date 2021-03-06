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

package br.com.criativasoft.opendevice.restapi.auth;

import br.com.criativasoft.opendevice.core.DataManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.restapi.ApiDataManager;
import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.AccountType;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BearerAuthRealm extends AbstractAuthorizingRealm {

    public static final String TOKEN_CACHE = "AuthTokenCache";

    private static final Logger log = LoggerFactory.getLogger(BearerAuthRealm.class);

    private DeviceManager manager;

    public BearerAuthRealm(DeviceManager manager) {
        this.manager = manager;
        setAuthenticationCachingEnabled(true);
        setAuthenticationTokenClass(BearerAuthToken.class);
    }

    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        BearerAuthToken authToken = (BearerAuthToken)token;

        String authTokenS = (String) authToken.getPrincipal();

        DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
        Cache<Object, Object> cache = securityManager.getCacheManager().getCache(TOKEN_CACHE);

        DataManager context = manager.getDataManager();

        String apiKey = (String) cache.get(authTokenS);

        // The token is API_KEY
        if(apiKey == null && authToken.isApikey()){
            apiKey = authTokenS;
        }

        if(apiKey == null) log.warn("ApiKey not found for token : " + authTokenS);

        if(  apiKey != null && context instanceof ApiDataManager){

            AccountDao dao = ((ApiDataManager) context).getAccountDao();

            UserAccount userAccount = dao.getUserAccountByApiKey(apiKey);

            if(userAccount != null){
                Account account = userAccount.getOwner();

                AccountType type = userAccount.getType();

                AccountPrincipal principal = new AccountPrincipal(userAccount.getUser().getId(), userAccount.getId(), account.getUuid(), type);

                // todo: load permission tags into AuthenticationInfo
                return new SimpleAuthenticationInfo(principal, authToken.getCredentials(), "BearerTokenRealm");
            }
        }

        return null;
    }

}