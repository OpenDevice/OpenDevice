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

package br.com.criativasoft.opendevice.core;

/**
 * TenantProvider with holds TenantID/AccountUUID in ThreadLocal
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class ThreadLocalTenantProvider extends TenantProvider {


    private static InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<String>();

    public void  setTenantID(String appID){
        threadLocal.set(appID);
    }

    public String getTenantID(){
        return threadLocal.get();
    }

    @Override
    protected TenantContext createContext(String id) {
        return new LocalTenantContext(id);
    }
}
