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

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.persistence.PersistenceContext;
import java.lang.reflect.Type;

/**
 * Jersey Injector for @PersistenceContext (only on Resources)
 *
 * @author Ricardo JL Rufino on 02/05/15.
 */
public class PersistenceContextInjectableProvider implements InjectableProvider<PersistenceContext, Type> {

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }


    @Override
    public Injectable getInjectable(ComponentContext componentContext, PersistenceContext persistenceContext, Type type) {

        return new AbstractHttpContextInjectable() {
            @Override
            public Object getValue(HttpContext c) {
                Object o = c.getProperties().get(TransactionFilter.KEY);
                return o;
            }
        };

    }
}
