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

import com.google.inject.Provider;
import com.google.inject.Singleton;

import javax.inject.Scope;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Singleton
public class LocalEntityManagerFactory implements Provider<EntityManagerFactory> {

    private static EntityManagerFactory emf;

    public EntityManagerFactory get() {
        return getInstance();
    }

    public static EntityManagerFactory getInstance() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("neo4j_pu");
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