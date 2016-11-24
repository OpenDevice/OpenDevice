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

package br.com.criativasoft.opendevice.wsrest.config;

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.wsrest.filter.AuthenticationFilter;
import br.com.criativasoft.opendevice.wsrest.filter.TenantFilter;
import br.com.criativasoft.opendevice.wsrest.guice.GuiceInjectProvider;
import br.com.criativasoft.opendevice.wsrest.io.AuthenticationExceptionMap;
import br.com.criativasoft.opendevice.wsrest.io.AuthorizationExceptionMap;
import br.com.criativasoft.opendevice.wsrest.io.EntityJacksonReader;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceConfigurator;
import org.secnod.shiro.jersey.ShiroResourceFilterFactory;
import org.secnod.shiro.jersey.SubjectInjectableProvider;

import java.util.Set;

/**
 *
 * @author Ricardo JL Rufino on 14/05/15.
 */
// from : /src/main/resources/META-INF/services/jersey-server-components
public class AppResourceConfigurator implements ResourceConfigurator {
    @Override
    public void configure(ResourceConfig config) {
        Set<Class<?>> classes = config.getClasses();
        classes.add(GuiceInjectProvider.class);
        classes.add(AuthenticationExceptionMap.class);
        classes.add(AuthorizationExceptionMap.class);
        classes.add(EntityJacksonReader.class);
//        classes.add(ByteArrayWriter.class);

        OpenDeviceConfig odevConfig = ODev.getConfig();

        if (odevConfig.isAuthRequired()) {
            // Shiro (Auth)
            config.getContainerRequestFilters().add(new AuthenticationFilter());
            classes.add(SubjectInjectableProvider.class);
            config.getResourceFilterFactories().add(new ShiroResourceFilterFactory());
        }

        config.getContainerRequestFilters().add(new TenantFilter());


    }
}
