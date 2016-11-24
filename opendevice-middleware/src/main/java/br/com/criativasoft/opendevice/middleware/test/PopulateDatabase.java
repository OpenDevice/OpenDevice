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

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;
import br.com.criativasoft.opendevice.middleware.persistence.dao.neo4j.DeviceNeo4J;
import br.com.criativasoft.opendevice.restapi.model.*;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class PopulateDatabase {

    static EntityManager em;
    static DeviceNeo4J dao;

    public static void main(String[] args) {

        // NOTE: must remove generated-value from DeviceHistory mapping config

//        OpenDeviceConfig.get().setDatabasePath("/media/ricardo/Dados/Codidos/Java/Projetos/opendevice-project/databases/graph.db");
        OpenDeviceConfig.get().setDatabaseEnabled(true);

        em = LocalEntityManagerFactory.getInstance().createEntityManager();
        dao = new DeviceNeo4J();
        dao.setEntityManager(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        List<Account> accounts = saveUsers();
        for (Account account : accounts) {
            saveDash(account);
        }

        for (Account account : accounts) {

            List<Board> boards = saveBoards(account, 2);

            for (Board board : boards) {
                saveDevices(account, board, 3, false);
                saveDevices(account, board, 5, true);
            }

        }


        for (Account account : accounts) {

            TenantProvider.setCurrentID(account.getUuid());

            System.out.println("Tenant : " + TenantProvider.getCurrentID());
            List<Device> devices = dao.listAll();
            System.out.println("Device : " + devices.size());
            for (Device device : devices) {
                if(device instanceof Sensor) {
                    saveHistory(device.getId());
                }
            }

        }


        System.out.println("============== FINISH ==============");

        tx.commit();
        em.close();
    }

    private static List<Board> saveBoards(Account account, int qtd) {

        List<Board> boards = new LinkedList<Board>();
        for (int i = 1; i < qtd+1; i++) {
            Board board = new Board(100 * i, "Board "+i+"."+account.getId(), DeviceType.BOARD, DeviceCategory.GENERIC_BOARD);
            board.setApplicationID(account.getUuid());
            boards.add(board);
            em.persist(board);
        }

        return boards;

    }


    private static void saveDevices(Account account, Board board, int qtd, boolean sensor) {

        List<Device> devices = new LinkedList<Device>();
        for (int i = 1; i < qtd+1; i++) {
            PhysicalDevice device;
            if(sensor){
                device = new Sensor(board.getUid() + i, "Sensor "+i+"."+board.getUid(), DeviceType.ANALOG, DeviceCategory.GENERIC_SENSOR);
            }else{
                device = new PhysicalDevice(board.getUid() + 10 + i, "Device "+i+"."+board.getUid(), DeviceType.DIGITAL);
            }
            device.setApplicationID(account.getUuid());
            if(i > 1) { // o primeiro n etem board
                device.setBoard(board);
                devices.add(device);
            }
            em.persist(device);

        }
        board.setDevices(devices);
        em.persist(board);
    }

    private static List<Account> saveUsers() {
        List<Account> list = new ArrayList<Account>();
        list.add(saveUser("admin", "admin", AccountType.ACCOUNT_MANAGER, "7262e4d6-7e3e-4fef-9267-92b12d7800af"));
        list.add(saveUser("user", "user", AccountType.USER, "6bde80c3-ad56-4e93-8fcb-275a125f9669"));

        return list;
    }

    private static Account saveUser(String u, String p, AccountType type, String key) {
        HashingPasswordService service = new DefaultPasswordService();

        System.out.println("Saving user: "+ u);
        User user = new User();
        user.setUsername(u);
        user.setPassword(service.encryptPassword(p));
        em.persist(user);

        Account account = new Account();
        account.setUuid(key);
        em.persist(account);

        UserAccount uaccount = new UserAccount();
        uaccount.setType(type);
        uaccount.setUser(user);
        uaccount.setOwner(account);
        em.persist(uaccount);

        ApiKey apiKey = new ApiKey();
        apiKey.setAccount(uaccount);
        apiKey.setAppName("ApplicationID");
        apiKey.setKey(account.getUuid());
        uaccount.getKeys().add(apiKey);
        em.persist(apiKey);
        em.persist(account);

        System.out.println("AccountUID :"  + account.getUuid());

        return account;
    }

    private static void saveDash(Account account){
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("Dash 1 Acc:" + account.getId());
        dashboard.setApplicationID(account.getUuid());
        em.persist(dashboard);

        dashboard = new Dashboard();
        dashboard.setTitle("Dash 2 Acc:" + account.getId());
        dashboard.setApplicationID(account.getUuid());
        em.persist(dashboard);
    }

    private static void saveHistory(long deviceID){
        int batchSize = 1000;
        int interval = 30;
        int months = 1;
        int numberOfValues = ((months*30) * 24 * 60) / interval;

        int valueMax = 1000; // device value

        Random random = new Random();
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, -months);

        System.out.println("Generating " + numberOfValues + " records");

        for (int i = 0; i < numberOfValues; i++){

            DeviceHistory history = new DeviceHistory();
//            history.setId(i + lastID); // CHANGE TO NEXT SEQUENCE
            history.setValue(random.nextInt(valueMax));
            history.setTimestamp(calendar.getTime().getTime());
            history.setDeviceID(deviceID);

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
