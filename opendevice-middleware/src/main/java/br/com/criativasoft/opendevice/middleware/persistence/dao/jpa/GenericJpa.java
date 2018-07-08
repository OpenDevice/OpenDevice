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

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.dao.Dao;
import br.com.criativasoft.opendevice.middleware.persistence.HibernateProvider;
import br.com.criativasoft.opendevice.restapi.model.Account;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public abstract class GenericJpa<T> implements Dao<T> {

    protected Class<T> persistentClass;

    protected GenericJpa(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Inject
    public void setEntityManager(EntityManager em) {
        HibernateProvider.setInstance(em);
    }

    public EntityManager em() {
        return HibernateProvider.getInstance();
    }

    @Override
    public T getById(long id) {
        return em().find(persistentClass, id);
    }

    @Override
    public void persist(T entity) {
        try{
            em().persist(entity);
        }catch (RuntimeException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public T update(T entity) {
        try{
            return em().merge(entity);
        }catch (RuntimeException ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void delete(T entity) {
        try{
            em().remove(entity);
        }catch (RuntimeException ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void refresh(T entity) {
        try{
            em().refresh(entity);
        }catch (RuntimeException ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public List<T> listAll() {
        return em().createQuery("from " + persistentClass.getSimpleName(), persistentClass).getResultList();
    }

    public Account getCurrentAccount() {
        TypedQuery<Account> query = em().createQuery("select x from Account x where x.uuid = :uuid", Account.class);
        query.setParameter("uuid", TenantProvider.getCurrentID());
        return query.getSingleResult();
    }

}
