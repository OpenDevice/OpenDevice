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

package br.com.criativasoft.opendevice.engine.js.utils;

import java.io.*;
import java.util.stream.Collectors;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 30/08/15.
 */
public class IOUtils {

    public static String toString(final InputStream inputStream) throws IOException {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining("\n"));
        } finally {

        }
    }

    public static String toString(File file) throws IOException {
        return toString(new FileInputStream(file));
    }

    public static void write(String target, InputStream inputStream) throws IOException {
        int read = 0;
        byte[] bytes = new byte[1024];

        OutputStream out = new FileOutputStream(new File(target));
        while ((read = inputStream.read(bytes)) != -1)
        {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }


}
