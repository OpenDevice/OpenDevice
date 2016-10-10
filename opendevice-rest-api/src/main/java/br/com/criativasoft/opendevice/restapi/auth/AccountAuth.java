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
 * Autentication using accountID (see AuthenticationFilter)
 * @see BearerTokenRealm
 */
public class AccountAuth implements AuthenticationToken {

    private long userAccountID;

    private long userID;

    public AccountAuth(long userAccountID, long userID) {
        this.userAccountID = userAccountID;
        this.userID = userID;
    }

    @Override
    public Object getPrincipal() {
        return userAccountID;
    }

    @Override
    public Object getCredentials() {
        return userAccountID;
    }

    public long getUserAccountID() {
        return userAccountID;
    }
}