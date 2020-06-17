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

import java.util.*;

/**
 * Command used to send user-defined events / notifications.<br/>
 * The initial intent is to notify client applications of state changes in user-defined extensions.
 * <p>
 * Using JS API, you can use:
 * <p>
 * <pre>{@code
 * ODev.on("cutom_event_name", function(message){
 *      // logic
 * });
 * }</pre>
 *
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br)
 * @date 05/11/2016
 */
public class UserEventCommand extends Command implements ExtendedCommand {

    private static final long serialVersionUID = -2155798878419286601L;

    private String name;

    protected Map<String, Object> params = new LinkedHashMap();

    public UserEventCommand(String name, Map<String, Object> params) {
        super(CommandType.USER_EVENT);
        this.name = name;
        if (params != null) {
            this.params = params;
        }
    }

    public UserEventCommand(String name) {
        super(CommandType.USER_EVENT);
        this.name = name;
    }


    public Map<String, Object> getParams() {
        return params;
    }

    public String getName() {
        return name;
    }

    @Override
    public void deserializeExtraData(String extradata) {
        String[] strparams = extradata.split(Command.DELIMITER);
        List<String> strings = Arrays.asList(strparams);
        for (int i = 0; i < strings.size(); i++) {
            this.params.put("" + (i), strings.get(i));
        }
    }

    @Override
    public String serializeExtraData() {

        if (params.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();

        Iterator<Object> it = params.values().iterator();

        sb.append(name);
        if (it.hasNext()) sb.append(DELIMITER);

        while (it.hasNext()) {
            Object object = it.next();
            if (object instanceof Boolean) {
                sb.append(((Boolean) object) ? 1 : 0);
            } else {
                sb.append(object.toString());
            }
            if (it.hasNext()) sb.append(DELIMITER);

        }
        return sb.toString();
    }

}
