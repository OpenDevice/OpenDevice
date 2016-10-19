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

package br.com.criativasoft.opendevice.middleware.persistence;

import br.com.criativasoft.opendevice.core.extension.PersistenceExtension;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

@Singleton
public class LocalEntityManagerFactory implements Provider<EntityManagerFactory> {

    private static final Logger log = LoggerFactory.getLogger(LocalEntityManagerFactory.class);

    private static EntityManagerFactory emf;

    public EntityManagerFactory get() {
        return getInstance();
    }

    public static EntityManagerFactory getInstance() {
        if (emf == null) {

            Properties properties = new Properties();

            // Load: Persistence Extensions
            // ======================================

            ServiceLoader<PersistenceExtension> service = ServiceLoader.load(PersistenceExtension.class);

            Iterator<PersistenceExtension> iterator = service.iterator();
            List<Class> persistentClasses = new ArrayList<Class>();

            while (iterator.hasNext()) {
                PersistenceExtension extension = iterator.next();
                persistentClasses.addAll(extension.loadClasses());
            }

            log.info("Additional persistence classes: " + persistentClasses);

            properties.put("hibernate.ejb.loaded.classes", persistentClasses);

            System.setProperty("objectdb.home", System.getProperty("user.dir"));
//            properties.put("hibernate.ogm.neo4j.database_path", OpenDeviceConfig.get().getDatabasePath());
//            properties.put(Neo4jProperties.HOST, "localhost:7474");
//            properties.put(Neo4jProperties.USERNAME, "admin");
//            properties.put(Neo4jProperties.PASSWORD, "admin");

            emf = Persistence.createEntityManagerFactory("opendevice_pu", properties);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("Closing EntityManagerFactory");
                    emf.close();
                }
            });
        }
        return emf;
    }
}