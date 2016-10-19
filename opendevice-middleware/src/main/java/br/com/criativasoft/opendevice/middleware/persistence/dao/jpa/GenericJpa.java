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

import br.com.criativasoft.opendevice.core.dao.Dao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

public abstract class GenericJpa<T> implements Dao<T> {

    private EntityManager em;

    private Class<T> persistentClass;

    protected GenericJpa(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Inject
    public void setEntityManager(EntityManager em) {

        this.em = em;
    }

    public EntityManager em() {
        return em;
    }


    @Override
    public T getById(long id) {
        return em.find(persistentClass, id);
    }

    @Override
    public void persist(T entity) {
        em.persist(entity);
    }

    @Override
    public T update(T entity) {
        return em.merge(entity);
    }

    @Override
    public void delete(T entity) {
        em.remove(entity);
    }

    @Override
    public void refresh(T entity) {
        em.refresh(entity);
    }

    @Override
    public List<T> listAll() {
        return em.createQuery("from " + persistentClass.getSimpleName(), persistentClass).getResultList();
    }

}
