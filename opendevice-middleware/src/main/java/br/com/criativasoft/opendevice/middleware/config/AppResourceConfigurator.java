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

import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;
import br.com.criativasoft.opendevice.middleware.persistence.PersistenceContextInjectableProvider;
import br.com.criativasoft.opendevice.middleware.persistence.TransactionFilter;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceConfigRegistry;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

// from : /src/main/resources/META-INF/services/jersey-server-components
public class AppResourceConfigurator implements ResourceConfigurator {

    private static Logger log = LoggerFactory.getLogger(AppResourceConfigurator.class);

    @Override
    public void configure(ResourceConfig config) {

        log.info("configuring app.....");

        // AUTO: config.getProviderClasses().add(DependencyConfig.class);
        final TransactionFilter transactionFilter = new TransactionFilter(LocalEntityManagerFactory.getInstance());
        config.getContainerRequestFilters().add(transactionFilter);
        config.getContainerResponseFilters().add(transactionFilter);

        Set<Class<?>> classes = config.getClasses();
        classes.add(PersistenceContextInjectableProvider.class);
        classes.add(EntityNotFoundMapper.class);

        log.info("AppResourceConfigurator : classes");
        for (Class<?> aClass : classes) {
            System.out.println(" - " + aClass);
        }

        //config.add(new MainApplication());

    }

}