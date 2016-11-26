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

package br.com.criativasoft.opendevice.restapi.io;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Ricardo JL Rufino
 * @date 17/10/16
 */
public class ErrorResponse {

    public static class ErrorMessage {

        private int status;

        private String message;

        public ErrorMessage(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Protected constructor, use one of the static methods to obtain a Response
     */
    protected ErrorResponse() {}

    public static Response status(Response.Status status, String message){
        return noCache(Response.status(status).entity(new ErrorMessage(status.getStatusCode(), message)).type(MediaType.APPLICATION_JSON_TYPE));
    }

    public static Response noCache(Response.ResponseBuilder resp) {
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setMaxAge(-1);
        cc.setMustRevalidate(true);
        return resp.cacheControl(cc).build();
    }

    public static Response UNAUTHORIZED(String message){
        int status = Response.Status.UNAUTHORIZED.getStatusCode();
        return Response.status(status).entity(new ErrorMessage(status, message)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public static Response BAD_REQUEST(String message){
        int status = Response.Status.BAD_REQUEST.getStatusCode();
        return Response.status(status).entity(new ErrorMessage(status, message)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
