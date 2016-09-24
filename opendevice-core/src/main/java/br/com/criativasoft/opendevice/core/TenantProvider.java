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

package br.com.criativasoft.opendevice.core;

/**
 * TenantProvider
 *
 * @author Ricardo JL Rufino on 05/10/14.
 */
public abstract class TenantProvider {

    private static TenantProvider provider = new LocalTenantProvider();

    public static final String HTTP_HEADER_KEY = "X-AppID";

    public static void setProvider(TenantProvider provider) {
        TenantProvider.provider = provider;
    }

    public static synchronized void setCurrentID(String appID){
        provider.setTenantID(appID);
    }

    public static String getCurrentID(){
        return provider.getTenantID();
    }

    public abstract void setTenantID(String appID);

    public abstract String getTenantID();
}
