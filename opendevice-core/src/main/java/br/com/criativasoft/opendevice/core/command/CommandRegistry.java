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

package br.com.criativasoft.opendevice.core.command;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ricardo JL Rufino
 * @date 02/11/15
 */
public class CommandRegistry {

    // extra commands
    private static final Map<Integer, Class<? extends Command>> map = new HashMap<Integer, Class<? extends Command>>();

    public static void addCommand(int code, Class<? extends Command> kclass){

        CommandType[] values = CommandType.values();
        for (CommandType value : values) {
            if(code == value.getCode()) throw new IllegalArgumentException("command already registred : " + value);
        }

        map.put(code, kclass);

    }

    public static Command getCommand(int code) throws CommandException{

        CommandType type = CommandType.getByCode(code);
        Class<? extends Command> kclass =  null;

        if(type != null){
            kclass = type.getCommandClass();
        }

        if(kclass == null) kclass = map.get(code);

        if(kclass == null) return null;

        try {
            return kclass.newInstance();
        } catch (Exception e) {
            throw new CommandException(e);
        }


    }

}
