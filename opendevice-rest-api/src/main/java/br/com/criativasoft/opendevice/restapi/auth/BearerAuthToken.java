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

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Autentication using token (see: AuthenticationFilter)
 * @see
 * @see BearerAuthRealm
 */
public class BearerAuthToken implements AuthenticationToken {

    private String token;

    private boolean apikey = false;  // Is ApiKey or Temp Token ?

    /**
     *
     * @param token
     * @param apikey  - true for ApiKey, false for Temporary token generated in OAuth
     */
    public BearerAuthToken(String token, boolean apikey) {
        this.token = token;
        this.apikey = apikey;
    }

    public BearerAuthToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    public boolean isApikey() {
        return apikey;
    }
}