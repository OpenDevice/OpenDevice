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

import br.com.criativasoft.opendevice.engine.js.utils.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class JavaScriptSnippetReader extends CharArrayReader {

    private static String template;

    public JavaScriptSnippetReader(String code) {
        super(new char[0]);

        if(template == null){
            InputStream input = this.getClass().getResourceAsStream("/js/engine/EventHook.js");
            try {
                template = IOUtils.toString(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String finalCode = template.replaceAll("#code#", code);

        this.buf = finalCode.toCharArray();
        this.pos = 0;
        this.count = buf.length;
    }
}
