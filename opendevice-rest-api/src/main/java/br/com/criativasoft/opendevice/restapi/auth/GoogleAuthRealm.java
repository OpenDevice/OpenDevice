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
import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.restapi.ApiDataManager;
import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.AccountType;
import br.com.criativasoft.opendevice.restapi.model.User;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 *         Date: 12/10/17
 */
public class GoogleAuthRealm extends AbstractAuthorizingRealm {

    // KEY = Google Auth Token, VALUE = userAccountID
    public static final String TOKEN_CACHE = "AuthTokenCache";

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthRealm.class);

    private DeviceManager manager;

    public GoogleAuthRealm(DeviceManager manager) {
        this.manager = manager;
        setAuthenticationCachingEnabled(true);
        setAuthenticationTokenClass(GoogleAuthToken.class);
    }

    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        GoogleAuthToken authToken = (GoogleAuthToken)token;

        String authTokenS = (String) authToken.getPrincipal();

        DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
        Cache<Object, Object> cache = securityManager.getCacheManager().getCache(TOKEN_CACHE);

        DataManager context = manager.getDataManager();
        AccountDao dao = ((ApiDataManager) context).getAccountDao();

        String userAccountID = (String) cache.get(authTokenS);

        if(userAccountID == null){

            log.warn("ApiKey not found for token : " + authTokenS);

            try{
                String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=";
                CloseableHttpClient client = HttpClientBuilder.create().build();
                CloseableHttpResponse response = client.execute(new HttpGet(url+authTokenS));
                String bodyAsString = EntityUtils.toString(response.getEntity());

                if(response.getStatusLine().getStatusCode() == 200){

                    String appID = ODev.getConfig().getString(OpenDeviceConfig.ConfigKey.google_appid);

                    if(appID == null){
                        throw new AuthenticationException("Google AppID not configured !");
                    }

                    JsonNode json = new ObjectMapper().readTree(bodyAsString);

                    String aud = json.get("aud").asText();

                    // TODO: need validate, but this may ne used for another appletavions IDs (ALEXA, MIDDLEWARE)
//                    if(!appID.equals(aud)){
//                        throw new AuthenticationException("Invalid Google Token");
//                    }

                    UserDao userDao = ((ApiDataManager) context).getUserDao();
                    User user = userDao.getUser(json.get("email").asText());

                    // Store in cahe
                    if(user != null){
                        userAccountID = ""+user.getLasLoginAccount().getId();
                        cache.put(authTokenS, userAccountID);
                    }

                }else{
                    throw new AuthenticationException("Invalid Google Token");
                }

            }catch (IOException ex){
                throw new AuthenticationException(ex.getMessage());
            }
        }

        if(  userAccountID != null && context instanceof ApiDataManager){

            UserAccount userAccount = dao.getUserAccountByID(Long.parseLong(userAccountID));

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
