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

import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.metamodel.PeriodType;
import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DeviceDaoNeo4j;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class PopulateDatabase {

    static EntityManager em;
    static DeviceDao dao;

    public static void main(String[] args) {

        // NOTE: must remove generated-value from DeviceHistory mapping config

        em = LocalEntityManagerFactory.getInstance().createEntityManager();
        dao = new DeviceDaoNeo4j(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();

//        saveDevices();
//        saveDash();

//        List<Device> devices = dao.listAll();
//        for (Device device : devices) {
//            saveHistory(device.getId());
//        }

        tx.commit();
        em.close();
    }


    private static void saveDevices() {
        em.persist(new Sensor(100, "Sensor 1x", DeviceType.ANALOG, DeviceCategory.GENERIC_SENSOR));
        em.persist(new Sensor(101, "Sensor 2x", DeviceType.ANALOG, DeviceCategory.GENERIC_SENSOR));
        em.persist(new Sensor(102, "Sensor 3x", DeviceType.ANALOG, DeviceCategory.GENERIC_SENSOR));
    }

    private static void saveDash(){
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("Default");
//        dashboard.add(new DashboardItem("Item 2.1", DashboardItem.DashboardType.LINE_CHART, ""));
//        dashboard.add(new DashboardItem("Item 2.2", DashboardItem.DashboardType.LINE_CHART, ""));
        em.persist(dashboard);
    }

    private static void testFind(){
        List<DeviceHistory> historyList = dao.getDeviceHistory(new DeviceHistoryQuery(1, PeriodType.MONTH, 2));
        for (DeviceHistory history : historyList) {
            System.out.println(history.getId()+  " - " + history.getTimestamp());
        }
    }

    private static void saveHistory(int deviceUID){
        int batchSize = 1000;
        int interval = 30;
        int months = 2;
        int numberOfValues = ((months*30) * 24 * 60) / interval;

        int valueMax = 1000;

        Random random = new Random();
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, -months);

        System.out.println("Generating " + numberOfValues + " records");


        long lastID = 1;
        List resultList = em.createQuery("SELECT id FROM DeviceHistory ORDER BY id desc").setMaxResults(1).getResultList();
        if(!resultList.isEmpty()){
            lastID = (Long) resultList.get(0) + 1;
        }

        for (int i = 0; i < numberOfValues; i++){

            DeviceHistory history = new DeviceHistory();
            history.setId(i + lastID); // CHANGE TO NEXT SEQUENCE
            history.setValue(random.nextInt(valueMax));
            history.setTimestamp(calendar.getTime().getTime());
            history.setDeviceID(deviceUID);

            System.out.println(i + ": " + calendar.getTime());
            em.persist(history);
            if(i % batchSize == 0) {
                em.flush();
                em.clear();
            }
            calendar.add(Calendar.MINUTE,interval);
        }

    }

}
