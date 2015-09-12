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

package br.com.criativasoft.opendevice.core.event;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class responsible for scan events from files. <br/>
 * Files must have the following header: <pre>
     @name NameOfEvent
     @devices 1,2,3
     @description User Description
     @type JavaScript
 </pre>
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class FileHookScanner {

    private static final Pattern REGEX_NAME =  Pattern.compile("@name\\s(.*)");
    private static final Pattern REGEX_DESC =  Pattern.compile("@description\\s(.*)");
    private static final Pattern REGEX_DEVs =  Pattern.compile("@devices\\s(.*)");
    private static final Pattern REGEX_TYPE =  Pattern.compile("@type\\s(.*)");
    private static final Pattern REGEX_END =  Pattern.compile("/*/\\s*$");

    public FileHookScanner() {

    }

    protected EventHook parse(File file){

        EventHook hook = new EventHook();

        try {

            Scanner input = new Scanner(file);
            StringBuffer code = new StringBuffer((int)file.length());
            boolean cancelMatcher = false;

            while(input.hasNext()) {
                String nextLine = input.nextLine();
                code.append(nextLine).append("\n");
                if(!cancelMatcher){

                    if(hook.getName() == null) hook.setName(getValue(REGEX_NAME, nextLine));
                    if(hook.getDescription() == null)  hook.setDescription(getValue(REGEX_DESC, nextLine));
                    if(hook.getType() == null) hook.setType(getValue(REGEX_TYPE, nextLine));

                    Matcher matcher = REGEX_DEVs.matcher(nextLine);
                    if (matcher.find()) {
                        String[] devices = matcher.group(1).split(",");
                        List<Integer> list = new LinkedList<Integer>();
                        for (int i = 0; i < devices.length; i++) {
                            list.add(Integer.parseInt(devices[i].trim()));
                        }
                        hook.setDeviceIDs(list);
                    }

                    matcher = REGEX_END.matcher(nextLine);
                    if (matcher.find()) {
                        cancelMatcher = true;
                    }
                }

            }

            input.close();

            hook.setHandler(code.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hook;

    }

    private String getValue(Pattern pattern, String line){
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String group = matcher.group(1);
            return group;
        }

        return null;
    }

}
