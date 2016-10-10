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

        // Ignore Web Resources.
        if(path != null && (
                path.endsWith(".css") ||
                path.endsWith(".css.map") ||
                path.endsWith(".ico")  ||
                path.endsWith(".js")  ||
                path.endsWith(".js.map")  ||
                path.endsWith(".png") ||
                path.endsWith(".jpg")) ){

            return true;
        }

        return false;

    }

}
