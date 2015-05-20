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

package br.com.criativasoft.opendevice.middleware.persistence.dao;

import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.model.DashboardItem;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 07/05/15.
 */
public class DashboardDaoNeo4j implements DashboardDao {

    @Inject
    private EntityManager em;

    public DashboardDaoNeo4j(){
    }

    public DashboardDaoNeo4j(EntityManager em) {
        this.em = em;
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public void activate(Dashboard dashboard) {

        List<Dashboard> dashboards = listAll();
        for (Dashboard current : dashboards) {
            if(current.getId() != dashboard.getId()) current.setActive(false);
            em.persist(current);

        }

        dashboard.setActive(true);
        em.persist(dashboard);

    }

    @Override
    public List<DashboardItem> listItems(long id) {

        Query query = em.createNativeQuery("MATCH (n:DashboardItem)-[:parent]->(d:Dashboard) where d.id = {dashID} RETURN n ORDER BY n.id", DashboardItem.class);
        query.setParameter("dashID",id);

        return query.getResultList();

    }

    @Override
    public void persistItem(DashboardItem DashboardItem) {

    }

    @Override
    public void deleteItem(DashboardItem DashboardItem) {

    }

    @Override
    public DashboardItem getItemByID(long id) {
        return em.find(DashboardItem.class, id);
    }

    @Override
    public Dashboard getById(long id){
        return em.find(Dashboard.class, id);
    }

    @Override
    public void persist(Dashboard entity) {

    }

    @Override
    public void update(Dashboard entity) {

    }

    @Override
    public void delete(Dashboard entity) {

    }

    @Override
    public void refresh(Dashboard entity) {

    }

    @Override
    public List<Dashboard> listAll() {

        TypedQuery<Dashboard> query = em.createQuery("from Dashboard", Dashboard.class);

        return query.getResultList();
    }
}
