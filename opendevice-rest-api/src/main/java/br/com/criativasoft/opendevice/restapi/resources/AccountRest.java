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

package br.com.criativasoft.opendevice.restapi.resources;

import br.com.criativasoft.opendevice.restapi.auth.AccountPrincipal;
import br.com.criativasoft.opendevice.restapi.model.ApiKey;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * Note: This resource is used in RestServerConnection
 *
 * @author Ricardo JL Rufino
 * @date 09/10/16
 */
@Path("/api/account")
@RequiresAuthentication
public class AccountRest {

    @Inject
    private AccountDao dao;

//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createKey(String appName) {
//
//    }

    @GET @Path("keys")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiKey> listKeys(@Auth Subject subject) {

        AccountPrincipal principal = (AccountPrincipal) subject.getPrincipal();

        List<ApiKey> keys = dao.listKeys(principal.getUserAccountID());

        return keys;
    }


}
