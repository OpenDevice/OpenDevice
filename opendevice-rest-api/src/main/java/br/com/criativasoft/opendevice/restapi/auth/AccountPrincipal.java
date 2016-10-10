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

import java.security.Principal;

/**
 * AccountPrincipal bounded to SecurityContext
 *
 * @author Ricardo JL Rufino
 * @date 09/10/16
 */
public class AccountPrincipal implements Principal {

    private long userID;

    private long userAccountID;

    private String accountUUID;

    public AccountPrincipal(long userID, long userAccountID, String accountUUID) {
        this.userID = userID;
        this.userAccountID = userAccountID;
        this.accountUUID = accountUUID;
    }

    public String getAccountUUID() {
        return accountUUID;
    }

    public long getUserAccountID() {
        return userAccountID;
    }

    public long getUserID() {
        return userID;
    }

    @Override
    public String getName() {
        return "User#"+userID;
    }
}
