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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Command used to send user-defined events / notifications.<br/>
 * The initial intent is to notify client applications of state changes in user-defined extensions.
 *
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br)
 * @date 05/11/2016
 */
public class UserEventCommand extends Command implements ExtendedCommand{

    private static final long serialVersionUID = -2155798878419286601L;

    private String name;

    private Set<Object> params = new LinkedHashSet<Object>();

    public UserEventCommand(String name, Object ... params) {
        super(CommandType.USER_EVENT);
        this.name = name;
        
        if(params != null){
            for (Object object : params) {
                 this.params.add(object);
            }
        }
    }

    public Set<Object> getParams() {
        return params;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public void deserializeExtraData( String extradata ) {
        String[] strparams = extradata.split(Command.DELIMITER);
        params.addAll(Arrays.asList(strparams));
    }

    @Override
    public String serializeExtraData() {
        
        if(params.isEmpty()) return null;
        
        StringBuilder sb = new StringBuilder();
       
        Iterator<Object> it = params.iterator();
        
        sb.append(name);
        if(it.hasNext()) sb.append(DELIMITER);
        
        while (it.hasNext()) {
            Object object =  it.next();
            if(object instanceof Boolean){
                sb.append(((Boolean)object) ? 1 : 0);
            }else{
                sb.append(object.toString());
            }
            if(it.hasNext()) sb.append(DELIMITER);
            
        }
        return sb.toString();
    }

}
