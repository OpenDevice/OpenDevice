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

package br.com.criativasoft.opendevice.core.extension;

import java.util.LinkedList;
import java.util.List;

/**
 * Replacement to Java ServiceLoader Interface
 * Ref: https://stackoverflow.com/a/7237152
 *
 * @author Ricardo JL Rufino
 * Date: 24/06/18
 */
public class PluginLoader {

    public static List<OpenDeviceExtension> load(){
        List<OpenDeviceExtension> extenions = new LinkedList<>();
        try{
            ResourceFinder finder = new ResourceFinder("META-INF/services/");
            List<Class> impls = finder.findAllImplementations(OpenDeviceExtension.class);

            for (Class impl : impls) {
                extenions.add((OpenDeviceExtension) impl.newInstance());
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return  extenions;
    }

}
