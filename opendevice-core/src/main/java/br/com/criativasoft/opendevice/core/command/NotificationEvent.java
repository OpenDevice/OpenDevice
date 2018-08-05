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

/**
 * Command used to send user-defined events / notifications.<br/>
 *
 * Using JS API, you can use:
 *
 * <pre>{@code
 * ODev.on("ui_notification", function(message){
 *      // logic
 * });
 * }</pre>
 *
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br)
 * @date 05/11/2016
 */
public class NotificationEvent extends UserEventCommand {

    private static final long serialVersionUID = -2155798878419286601L;


    public NotificationEvent(String title, String message, String type) {
        super("ui_notification");
        params.put("title", title);
        params.put("message", message);
        params.put("type", type);
    }


}
