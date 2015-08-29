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

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Use a proxy context to call OpenDevice API from user scripts
 * @see js/engine/JavaScriptDeviceManager.js
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class ScriptContextReader extends CharArrayReader {


    public ScriptContextReader(File file) throws FileNotFoundException {
        super("".toCharArray());

        Scanner inputFile = new Scanner(file);
        StringBuilder sb = new StringBuilder(1024);

        sb.append("with (proxy) {");

        while(inputFile.hasNext()){
            String line = inputFile.nextLine();
            sb.append(line).append("\n");
        }

        sb.append("}");

        this.buf = sb.toString().toCharArray();
        this.pos = 0;
        this.count = buf.length;
    }

}
