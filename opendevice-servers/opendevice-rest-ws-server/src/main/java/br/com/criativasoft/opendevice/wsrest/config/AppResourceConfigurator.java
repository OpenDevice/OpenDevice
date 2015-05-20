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

import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceConfigRegistry;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceConfigurator;

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
        classes.add(GuiceConfigRegistry.getConfigClass());
    }
}
