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

package br.com.criativasoft.opendevice.engine.js;

import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.util.cmdline.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.util.Set;

/**
 * Command line tool to run script's
 *
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class OpenDeviceJSEngine {

    private static final Logger log = LoggerFactory.getLogger(OpenDeviceJSEngine.class);

    public static void main(String[] args) throws FileNotFoundException, ScriptException {

        Options opt = new Options(args, 1);

        opt.getSet().addOption("fx", Options.Multiplicity.ZERO_OR_ONE);

        if (!opt.check()) {
            // Print usage hints
            System.out.println("parameters error");
            System.exit(1);
        }

        String script = opt.getSet().getData().get(0);

        // Use JavaFX
        if (opt.getSet().isSet("fx")) {
            log.info("Using JavaFX");
            javafx.application.Application.launch(JavaScriptFXApp.class, new String[]{script});
        }else{
            run(script);
        }

    }

    public static void run(String file) throws FileNotFoundException, ScriptException {
        run(file, new SimpleBindings());
    }

    public static void run(String file, Bindings bindings) throws FileNotFoundException, ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        ClassLoader cl = OpenDeviceJSEngine.class.getClassLoader();
        InputStream imports = cl.getResourceAsStream("js/engine/JavaScriptDeviceManager.js");

        DeviceType[] values = DeviceType.values();
        for (DeviceType type : values) {
            engine.put(type.name(), type);
        }

        engine.put("out", Connections.out);
        engine.put("input", Connections.in);

        Set<String> bindingsKeys = bindings.keySet();
        for (String key : bindingsKeys) {
            engine.put(key, bindings.get(key));
        }

        engine.eval(new InputStreamReader(imports));
        engine.eval(new ScriptContextReader(new File(file)));

    }
}


