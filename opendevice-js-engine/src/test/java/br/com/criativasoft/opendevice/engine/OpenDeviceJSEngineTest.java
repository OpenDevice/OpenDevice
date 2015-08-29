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

package br.com.criativasoft.opendevice.engine;

import br.com.criativasoft.opendevice.engine.js.OpenDeviceJSEngine;

import javax.script.ScriptException;
import java.io.FileNotFoundException;

/**
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class OpenDeviceJSEngineTest {

    public static void main(String[] args) throws FileNotFoundException, ScriptException {

        String dir = "/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-js-engine/src/test/java/";

        OpenDeviceJSEngine.main(new String[]{dir+ "SwingDemo.js"});

//        OpenDeviceJSEngine.main(new String[]{"-fx", dir+ "JavaFXDemo.js"});

    }
}
