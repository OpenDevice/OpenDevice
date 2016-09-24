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

import br.com.criativasoft.opendevice.wsrest.filter.AuthenticationFilter;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * Autentication using accountID
 * @see AuthenticationFilter
 * @see BearerTokenRealm
 */
public class AccountAuth implements AuthenticationToken {

    private long accountID;

    public AccountAuth(long accountID) {
        this.accountID = accountID;
    }

    @Override
    public Object getPrincipal() {
        return accountID;
    }

    @Override
    public Object getCredentials() {
        return accountID;
    }

    public long getAccountID() {
        return accountID;
    }
}