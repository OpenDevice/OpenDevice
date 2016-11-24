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

import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.middleware.jobs.JobManager;
import br.com.criativasoft.opendevice.middleware.persistence.HibernateProvider;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DashboardDao;
import br.com.criativasoft.opendevice.middleware.persistence.dao.JobSpecDao;
import br.com.criativasoft.opendevice.middleware.persistence.dao.RuleSpecDao;
import br.com.criativasoft.opendevice.middleware.persistence.dao.jpa.AccountJPA;
import br.com.criativasoft.opendevice.middleware.persistence.dao.jpa.JobSpecJPA;
import br.com.criativasoft.opendevice.middleware.persistence.dao.jpa.RuleSpecJPA;
import br.com.criativasoft.opendevice.middleware.persistence.dao.jpa.UserJPA;
import br.com.criativasoft.opendevice.middleware.persistence.dao.neo4j.DashboardDaoNeo4j;
import br.com.criativasoft.opendevice.middleware.persistence.dao.neo4j.DeviceNeo4J;
import br.com.criativasoft.opendevice.middleware.rules.RuleManager;
import br.com.criativasoft.opendevice.middleware.tools.SimulationService;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceModule;
import com.google.inject.Binder;

import javax.persistence.EntityManager;

/**
 *
 * @author Ricardo JL Rufino on 02/05/15.
 */
public class DependencyConfig extends GuiceModule {

    public void configure(Binder binder) {

        super.configure(binder);

        binder.bind(EntityManager.class).toProvider(HibernateProvider.class);
        binder.bind(SimulationService.class);
        binder.bind(RuleManager.class);
        binder.bind(JobManager.class);

        // Daos
        binder.bind(DashboardDao.class).to(DashboardDaoNeo4j.class);
        binder.bind(DeviceDao.class).to(DeviceNeo4J.class);
        binder.bind(AccountDao.class).to(AccountJPA.class);
        binder.bind(UserDao.class).to(UserJPA.class);
        binder.bind(RuleSpecDao.class).to(RuleSpecJPA.class);
        binder.bind(JobSpecDao.class).to(JobSpecJPA.class);


    }

}
