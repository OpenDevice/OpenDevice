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

import br.com.criativasoft.opendevice.middleware.jobs.JobManager;
import br.com.criativasoft.opendevice.middleware.model.jobs.JobSpec;
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
 * @date 12/11/16
 * @see RuleManager
 */
@Path("/middleware/jobs")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class JobRest {

    @Inject
    private JobManager jobManager;

    @GET
    @Path("/{id}")
    public JobSpec get(@PathParam("id") long id) throws IOException {
        return jobManager.getById(id);
    }

    @GET
    public List<JobSpec> list() throws IOException {
        List<JobSpec> list = jobManager.listAll();
        return list;
    }

    @PUT @Path("/{id}/activate")
    public Response activate(JobSpec rule, @QueryParam("value") boolean value) throws IOException {

        if(rule == null) throw new NotFoundException();

        jobManager.update(rule);

        return Response.ok().build();
    }

    @POST
    public Response save(JobSpec rule) throws IOException {

        jobManager.persist(rule);

        return Response.ok().build();

    }

    @PUT
    @Path("{id}")
    public Response update(JobSpec rule) throws IOException {

        jobManager.update(rule);

        return Response.ok().build();

    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) throws IOException {

        JobSpec spec = jobManager.getById(id);

        jobManager.delete(spec);

        return Response.ok().build();

    }


}
