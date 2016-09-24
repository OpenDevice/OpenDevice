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
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino
 * @date 22/09/16
 */
public class AccountDaoJPA implements AccountDao {

    @Inject
    private EntityManager em;

    public AccountDaoJPA() {
    }

    public AccountDaoJPA(EntityManager em) {
        this.em = em;
    }

    @Override
    public Account getAccountByApiKey(String key) {

        TypedQuery<ApiKey> query = em.createQuery("from ApiKey where key = :p1", ApiKey.class);
        query.setParameter("p1", key);
        query.setMaxResults(1);

        List<ApiKey> list = query.getResultList();

        if(list.isEmpty()) return null;

        ApiKey apiKey = list.iterator().next();

        return apiKey.getAccount();
    }

    @Override
    public Account getAccount(String username, String password) {
        TypedQuery<Account> query = em.createQuery("from Account where username = :p1 and password = :p2", Account.class);
        query.setParameter("p1", username);
        query.setParameter("p2", password);
        query.setMaxResults(1);

        List<Account> list = query.getResultList();

        if(list.isEmpty()) return null;

        return list.iterator().next();
    }

    @Override
    public Account getById(long id) {
        return em.find(Account.class, id);
    }

    @Override
    public void persist(Account entity) {
        em.persist(entity);
    }

    @Override
    public void update(Account entity) {
        em.persist(entity);
    }

    @Override
    public void delete(Account entity) {
        em.remove(entity);
    }

    @Override
    public void refresh(Account entity) {
        em.refresh(entity);
    }

    @Override
    public List<Account> listAll() {
        return em.createQuery("from Account", Account.class).getResultList();
    }
}
