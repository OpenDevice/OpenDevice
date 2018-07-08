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

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.middleware.model.IAccountEntity;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */

public abstract class GenericTenantJpa<T extends IAccountEntity> extends GenericJpa<T> {

    protected GenericTenantJpa(Class<T> persistentClass) {
        super(persistentClass);
    }

    @Override
    public void persist(T entity) {
        entity.setAccount(getCurrentAccount());
        super.persist(entity);
    }

    @Override
    public T getById(long id) {
        TypedQuery<T> query = em().createQuery("from "+persistentClass.getSimpleName()+" where id = :ID and account.uuid = :TENANT", persistentClass);
        query.setParameter("TENANT", TenantProvider.getCurrentID());
        query.setParameter("ID", id);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    @Override
    public List<T> listAll() {
        TypedQuery<T> query = em().createQuery("from "+persistentClass.getSimpleName()+" where account.uuid = :TENANT", persistentClass);
        query.setParameter("TENANT", TenantProvider.getCurrentID());
        return query.getResultList();
    }
}
