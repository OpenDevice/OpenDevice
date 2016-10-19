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

package br.com.criativasoft.opendevice.middleware.persistence;

import com.google.inject.Provider;

import javax.persistence.EntityManager;
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

/**
 * Guice(@Inject) Provider for EntityManager (It works in partnership with TransactionFilter)
 * @see TransactionFilter
 * @author Ricardo JL Rufino on 02/05/15.
 */
public class HibernateProvider implements Provider<EntityManager> {

    private static InheritableThreadLocal<EntityManager> threadLocal = new InheritableThreadLocal<EntityManager>();

    public static void setInstance(EntityManager instance){
        threadLocal.set(instance);
    }

    public static EntityManager getInstance(){
        return threadLocal.get();
    }

    @Override
    public EntityManager get() {
        return threadLocal.get();
    }
}
