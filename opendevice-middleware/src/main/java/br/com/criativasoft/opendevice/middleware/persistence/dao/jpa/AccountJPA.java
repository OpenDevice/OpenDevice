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
import br.com.criativasoft.opendevice.restapi.model.User;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino
 * @date 22/09/16
 */
public class AccountJPA extends GenericJpa<Account> implements AccountDao {


    public AccountJPA() {
        super(Account.class);
    }

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

    @Override
    public List<ApiKey> listKeys(long userAccountID) {

        TypedQuery<ApiKey> query = em().createQuery("select x from ApiKey x where x.account.id = :p1", ApiKey.class);

        query.setParameter("p1", userAccountID);

        List<ApiKey> list = query.getResultList();

        return list;
    }

    @Override
    public List<User> listUsers(Account account) {

        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, UserAccount> accounts = root.join("accounts");
        Join<User, UserAccount> owner = accounts.join("owner");
        query.where(cb.equal(owner.get("id"), account.getId()));
        List<User> list = em().createQuery(query).getResultList();

        return list;
    }

    @Override
    public Account getAccountByUID(String uid) {
        TypedQuery<Account> query = em().createQuery("select x from Account x where x.uuid = :p1", Account.class);

        query.setParameter("p1", uid);

        return query.getSingleResult();
    }

    @Override
    public boolean existUser(Account account, User user) {

        List<User> users = listUsers(account);

        // Validate
        for (User user1 : users) {
            if(user1.getId() == user.getId()){
                return  true;
            }

        }

        return false;
    }
}
