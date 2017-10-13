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

package br.com.criativasoft.opendevice.wsrest.io;

import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Map AuthorizationException to Http Response
 *
 * @author Ricardo JL Rufino
 * @date 08/09/16
 */
public class AuthorizationExceptionMap implements ExceptionMapper<AuthorizationException> {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationExceptionMap.class);

    @Override
    public Response toResponse(AuthorizationException exception) {

        log.debug(exception.getMessage());

        if(exception instanceof UnauthenticatedException){
            return ErrorResponse.UNAUTHORIZED("Authorization Required", "WWW-Authenticate");
        }

        return ErrorResponse.status(Response.Status.UNAUTHORIZED,"Unauthorized");

    }
}
