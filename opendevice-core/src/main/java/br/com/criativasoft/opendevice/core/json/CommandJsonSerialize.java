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

package br.com.criativasoft.opendevice.core.json;

import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * MixIn Jackson configuration
 * Ref: http://wiki.fasterxml.com/JacksonMixInAnnotations
 *
 * @author Ricardo JL Rufino
 */
public class CommandJsonSerialize {

    abstract class ResponseCommandIgnoreMixin {
        @JsonIgnore
        int timeout;
        @JsonIgnore
        private ResponseCommand response;
        @JsonIgnore
        private String uid;            // Logic level user ID.
        @JsonIgnore
        private String applicationID;  // id of client (for Multitenancy support)


    }

}
