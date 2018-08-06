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

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;
import br.com.criativasoft.opendevice.middleware.persistence.PersistenceContextInjectableProvider;
import br.com.criativasoft.opendevice.middleware.persistence.TransactionFilter;
import br.com.criativasoft.opendevice.wsrest.io.EntityNotFoundMapper;
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

        log.info("Configuring app.....");

        // AUTO: config.getProviderClasses().add(DependencyConfig.class);

        OpenDeviceConfig odev = ODev.getConfig();

        if(odev.isDatabaseEnabled()){
            TransactionFilter transactionFilter = new TransactionFilter(LocalEntityManagerFactory.getInstance());
            config.getContainerRequestFilters().add(1, transactionFilter);
            config.getContainerResponseFilters().add(1, transactionFilter);
        }

        Set<Class<?>> classes = config.getClasses();
        classes.add(PersistenceContextInjectableProvider.class);
        classes.add(EntityNotFoundMapper.class);


        StringBuffer sb = new StringBuffer();
        for (Class<?> aClass : classes) {
            sb.append(" - " + aClass).append("\n");
        }
        log.debug("Configured Resources and Filters : \n {}", sb);

        //config.add(new MainApplication());

    }

}