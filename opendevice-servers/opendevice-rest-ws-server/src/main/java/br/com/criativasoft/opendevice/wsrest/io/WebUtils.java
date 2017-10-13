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

package br.com.criativasoft.opendevice.wsrest.io;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 23/09/16
 */
public final class WebUtils {

    private WebUtils(){}

    public static boolean isWebResource(String path){

        int dot = path.lastIndexOf(".");
        if (dot < 0) {
            return false;
        }

        String ext = path.substring(dot + 1);
        int queryString = ext.indexOf("?");
        if (queryString > 0) {
            ext = ext.substring(0, queryString);
        }

        // Ignore Web Resources.
        if(ext != null && (
                ext.endsWith("css") ||
                ext.endsWith("css.map") ||
                ext.endsWith("ico")  ||
                ext.endsWith("js")  ||
                ext.endsWith("js.map")  ||
                ext.endsWith("map")  ||
                ext.endsWith("ttf")  ||
                ext.endsWith("woff")  ||
                ext.endsWith("woff2")  ||
                ext.endsWith("png") ||
                ext.endsWith("jpeg") ||
                ext.endsWith("jpg")) ){

            return true;
        }

        return false;

    }

}
