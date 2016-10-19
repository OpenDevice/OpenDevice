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

import com.google.inject.Injector;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.inject.Inject;
import java.lang.reflect.Type;

/**
 * JAX-RS Provider - Guice Integration. <br/>
 * @author Ricardo JL Rufino
 */
public class GuiceInjectProvider implements InjectableProvider<Inject, Type> {

    private static Injector injector;

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    public static void setInjector(Injector injector) {
        GuiceInjectProvider.injector = injector;
    }

    public static Injector getInjector() {
        return injector;
    }

    @Override
    public Injectable getInjectable(final ComponentContext context, final Inject annotation, final Type targetClass) {

        return new AbstractHttpContextInjectable() {
            @Override
            public Object getValue(HttpContext c) {
                return injector.getInstance((Class) targetClass);
            }
        };
    }

}