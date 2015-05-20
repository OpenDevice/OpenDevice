/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.middleware.persistence;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;


public class TransactionFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);

    public static final String KEY = "javax.persistence.EntityManager";

    @Inject
    private EntityManagerFactory emf;

    public TransactionFilter() {
    }

    public TransactionFilter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {

        log.debug("filter: " + request.getPath() + " (" + request.getMethod() + ")");

        EntityManager em = emf.createEntityManager();

        request.getProperties().put(KEY, em);

        HibernateProvider.setInstance(em);

        EntityTransaction tx = em.getTransaction();

        tx.begin();

        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {

        EntityManager em = (EntityManager) request.getProperties().get(KEY);

        HibernateProvider.setInstance(null);

        EntityTransaction tx = em.getTransaction();

        try{

            if(tx.isActive()) tx.commit();
        } finally {
            if (tx.isActive()) {
                log.warn("rollback transaction");
                tx.rollback();
            }
            em.close();
        }

        return response;
    }
}