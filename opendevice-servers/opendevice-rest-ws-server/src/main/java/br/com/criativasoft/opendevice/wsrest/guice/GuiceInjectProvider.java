/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.wsrest.guice;

import com.google.inject.*;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Type;

/**
 * JAX-RS Provider - Guice Integration. <br/>
 * @author Ricardo JL Rufino
 */
public abstract class GuiceInjectProvider implements InjectableProvider<Inject, Type>, Module {

    private static final Logger log = LoggerFactory.getLogger(GuiceInjectProvider.class);

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    private static Injector injector;

    private void initInjector() {
        log.info("Initializing Guice Configuration: " + GuiceInjectProvider.this.getClass());
        injector = Guice.createInjector(this);
    }



    @Override
    public Injectable getInjectable(final ComponentContext context, final Inject annotation, final Type targetClass) {
        if (injector == null) initInjector();
        return new AbstractHttpContextInjectable() {
            @Override
            public Object getValue(HttpContext c) {
                return injector.getInstance((Class) targetClass);
            }
        };
    }
}