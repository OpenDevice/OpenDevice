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
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 22/09/16
 */
public class AccountDaoRealm extends AbstractAuthorizingRealm {

    private DeviceManager manager;

    public AccountDaoRealm(DeviceManager manager) {
        this.manager = manager;
        setAuthenticationTokenClass(AccountAuth.class);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        AccountAuth accountAuth = (AccountAuth) token;

        DataManager context = manager.getDataManager();

        if( context instanceof ApiDataManager){

            AccountDao dao = ((ApiDataManager) context).getAccountDao();

            UserAccount userAccount = dao.getUserAccountByID(accountAuth.getUserAccountID());

            if(userAccount != null){
                Account account = userAccount.getOwner();

                AccountType type = userAccount.getType();

                AccountPrincipal principal = new AccountPrincipal(userAccount.getUser().getId(), userAccount.getId(), account.getUuid(), type);

                // todo: load permission tags into AuthenticationInfo
                return new SimpleAuthenticationInfo(principal, userAccount.getId(), "AccountDaoRealm");
            }
        }

        return null;
    }

}
