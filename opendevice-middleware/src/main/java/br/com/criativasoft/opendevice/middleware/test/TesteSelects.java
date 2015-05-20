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

package br.com.criativasoft.opendevice.middleware.test;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.model.DashboardItem;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class TesteSelects {

    public static void main(String[] args) throws ParseException {

        EntityManager em = LocalEntityManagerFactory.getInstance().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();


//        TypedQuery<Device> query = em.createQuery("from Device", Device.class);
//        for (Device history : query.getResultList()) {
//            System.out.println(history.getId() + " : " + history.getName());
////            em.remove(history);
//        }


//        TypedQuery<Dashboard> query = em.createQuery("from Dashboard", Dashboard.class);
//        List<Dashboard> resultList = query.getResultList();
//        for (Dashboard dashboard : resultList) {
//            System.out.println(dashboard.getId() + " : " + dashboard.getItems());
//        }

        //        List<DeviceHistory> historyList = dao.getDeviceHistory(new DeviceHistoryQuery(1, DeviceHistoryQuery.PeriodType.MONTH, 2));
//        for (DeviceHistory history : historyList) {
//            System.out.println(history.getId()+  " - " + history.getTimestamp());
//        }


        Dashboard dashboard = null;
        List<Dashboard> resultList = em.createQuery("from Dashboard order by id desc", Dashboard.class).setMaxResults(1).getResultList();
        if(!resultList.isEmpty()) dashboard = resultList.get(0);

        System.out.println("--dashboard: " + dashboard.getId());

//        DashboardItem dashboardItem = new DashboardItem("Item1", DashboardItem.DashboardType.LINE_CHART, "0,0,0,0");
//        dashboard.add(dashboardItem);
//        em.persist(dashboard);
//        em.persist(dashboardItem);

        System.out.println("-- Reference");
        Collection<DashboardItem> referenceList = dashboard.getItems();
        for (DashboardItem restItem : referenceList) {
            System.out.println(restItem.getId() + " : " + restItem.getTitle());
        }

//        System.out.println("-- from DashboardItem WHERE parent.id");
//        TypedQuery<DashboardItem> query = em.createQuery("from DashboardItem WHERE parent = :parentID ORDER BY id", DashboardItem.class);
//        query.setParameter("parentID", dashboard);
//        Collection<DashboardItem> resultList1 = query.getResultList();
//
        System.out.println("-- Cypher queries");
        Query query = em.createNativeQuery("MATCH (n:DashboardItem)-[:parent]->(d:Dashboard) where d.id = {dashID} RETURN n ORDER BY n.id", DashboardItem.class);
        query.setParameter("dashID", dashboard.getId());
        Collection<DashboardItem> resultList1 = query.getResultList();

        for (DashboardItem restItem : resultList1) {
            System.out.println(restItem.getId() + " : " + restItem.getTitle());
        }

        System.out.println("-- from DashboardItem");
        TypedQuery<DashboardItem> query2 = em.createQuery("from DashboardItem", DashboardItem.class);
        List<DashboardItem> resultList2 = query2.getResultList();
        for (DashboardItem item : resultList2) {
            System.out.println(item.getId() + " : " + item.getTitle());
//            em.remove(item);
        }
        tx.commit();
        em.close();
    }
}
