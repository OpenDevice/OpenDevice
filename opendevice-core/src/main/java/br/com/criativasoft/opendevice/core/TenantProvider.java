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
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 05/10/14.
 */
public class TenantProvider {

    private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static final String HTTP_HEADER_KEY = "X-AppID";

    public static synchronized void setCurrentID(String appID){
        threadLocal.set(appID);
    }

    public static String getCurrentID(){
        return threadLocal.get();
    }
}
