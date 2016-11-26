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

package br.com.criativasoft.opendevice.restapi.model.vo;

import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.AccountType;
import br.com.criativasoft.opendevice.restapi.model.User;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author Ricardo JL Rufino
 * @date 26/11/16
 */
public class AccountVO {

    private long id;

    private String username;

    private int users;

    private Date creationDate;

    private Date lastLogin;

    public AccountVO(Account account){

        this.id = account.getId();
        Set<UserAccount> userAccounts = account.getUserAccounts();
        for (UserAccount userAccount : userAccounts) {

            users++;

            if(userAccount.getType() == AccountType.ACCOUNT_MANAGER){
                User user = userAccount.getUser();
                this.username = user.getUsername();
                this.creationDate = user.getCreationDate();
                this.lastLogin = user.getLastLogin();
            }
        }

    }

    public long getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }

    public int getUsers() {
        return users;
    }


    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastLogin() {
        return lastLogin;
    }
}
