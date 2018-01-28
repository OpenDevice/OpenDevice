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

package br.com.criativasoft.opendevice.middleware.resources;

import br.com.criativasoft.opendevice.middleware.model.rules.RuleSpec;
import br.com.criativasoft.opendevice.middleware.rules.RuleManager;
import com.sun.jersey.api.NotFoundException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author Ricardo JL Rufino
 * @date 01/11/16
 * @see RuleManager
 */
@Path("/middleware/rules")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class RuleRest {

    @Inject
    private RuleManager ruleManager;

    @GET
    @Path("/{id}")
    public RuleSpec get(@PathParam("id") long id) throws IOException {
        return ruleManager.getById(id);
    }

    @GET
    public List<RuleSpec> list() throws IOException {
        List<RuleSpec> list = ruleManager.listAll();
        return list;
    }

    @PUT @Path("/{id}/activate")
    public Response activate(@PathParam("id") long id, RuleSpec rule, @QueryParam("value") boolean value) throws IOException {

        if(rule == null) throw new NotFoundException();

//        rule.setEnabled(value);

        ruleManager.update(rule);

        return Response.ok().build();
    }

    @POST
    public Response save(RuleSpec rule) throws IOException {

        rule.setEnabled(true);

        ruleManager.persist(rule);

        return Response.ok().build();

    }

    @PUT
    @Path("{id}")
    public Response update(RuleSpec rule) throws IOException {

        ruleManager.update(rule);

        return Response.ok().build();

    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) throws IOException {

        RuleSpec spec = ruleManager.getById(id);

        ruleManager.delete(spec);

        return Response.ok().build();

    }


}
