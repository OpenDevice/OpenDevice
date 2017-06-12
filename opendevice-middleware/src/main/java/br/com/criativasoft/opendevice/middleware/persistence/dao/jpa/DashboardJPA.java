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
import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.model.DashboardItem;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DashboardDao;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Ricardo JL Rufino on 07/05/15.
 */
public class DashboardJPA extends GenericJpa<Dashboard> implements DashboardDao {


    public DashboardJPA() {
        super(Dashboard.class);
    }

    @Override
    public void activate(Dashboard dashboard) {

        List<Dashboard> dashboards = listAll();
        for (Dashboard current : dashboards) {
            if(current.getId() != dashboard.getId()) current.setActive(false);
            em().persist(current);
        }

        dashboard.setActive(true);
        em().persist(dashboard);

    }


    @Override
    public void persistItem(DashboardItem item) {
        em().persist(item);
    }

    @Override
    public void deleteItem(DashboardItem item) {
        em().remove(item);
    }

    @Override
    public DashboardItem getItemByID(long id) {
        return em().find(DashboardItem.class, id);
    }


    @Override
    public void persist(Dashboard entity) {
        entity.setApplicationID(TenantProvider.getCurrentID());
        super.persist(entity);
    }

    @Override
    public List<Dashboard> listAll() {

        TypedQuery<Dashboard> query = em().createQuery("from Dashboard where applicationID = :TENANT", Dashboard.class);

        query.setParameter("TENANT", TenantProvider.getCurrentID());

        return query.getResultList();
    }

    @Override
    public List<DashboardItem> listItems(long id) {
        Query query = em().createNativeQuery("MATCH (n:DashboardItem)-[:parent]->(d:Dashboard) where d.id = {dashID} RETURN n ORDER BY n.id", DashboardItem.class);
        query.setParameter("dashID",id);
        // FIXME: add tenantID for security...
        return query.getResultList();

    }
}
