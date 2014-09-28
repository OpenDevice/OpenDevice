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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.inject.Inject;
import java.lang.reflect.Type;

/**
 * JAX-RS Provider - Guice Integration. <br/>
 * @author Ricardo JL Rufino
 */
public abstract class GuiceResourceProvider implements InjectableProvider<Inject, Type>, Module {

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    private static Injector injector;

    private void initInjector() {
        injector = Guice.createInjector(this);
    }

    @Override
    public Injectable getInjectable(final ComponentContext context, final Inject annotation, final Type targetClass) {
        if (injector == null) initInjector();
        return new Injectable<Object>() {
            @Override
            public Object getValue() {
                return injector.getInstance((Class) targetClass);
            }
        };
    }
}