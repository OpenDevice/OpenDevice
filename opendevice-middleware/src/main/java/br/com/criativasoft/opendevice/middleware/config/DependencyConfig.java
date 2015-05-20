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

package br.com.criativasoft.opendevice.middleware.config;

import br.com.criativasoft.opendevice.middleware.persistence.HibernateProvider;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DashboardDao;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DashboardDaoNeo4j;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceConfigRegistry;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceModule;
import com.google.inject.Binder;

import javax.persistence.EntityManager;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 02/05/15.
 */

public class DependencyConfig extends GuiceModule {

    public void configure(Binder binder) {

        super.configure(binder);

        // binder.bind(EntityManagerFactory.class).toProvider(LocalEntityManagerFactory.class);
        binder.bind(EntityManager.class).toProvider(HibernateProvider.class);
        binder.bind(DashboardDao.class).to(DashboardDaoNeo4j.class);

    }

}
