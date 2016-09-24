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

package br.com.criativasoft.opendevice.middleware.persistence.dao.jpa;

import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.ApiKey;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino
 * @date 22/09/16
 */
public class AccountDaoJPA extends GenericJpa<Account> implements AccountDao {


    @Override
    public UserAccount getUserAccountByApiKey(String key) {

        TypedQuery<ApiKey> query = em().createQuery("select x from ApiKey x where x.key = :p1", ApiKey.class);
        query.setParameter("p1", key);
        query.setMaxResults(1);

        List<ApiKey> list = query.getResultList();

        if(list.isEmpty()) return null;

        ApiKey apiKey = list.iterator().next();

        return apiKey.getAccount();
    }

    @Override
    public UserAccount getUserAccountByID(long id) {
        return em().find(UserAccount.class, id);
    }

    @Override
    public Account getAccountByApiKey(String key) {

        UserAccount account = getUserAccountByApiKey(key);

        if(account != null){
            return account.getOwner();
        }

        return null;
    }

}
