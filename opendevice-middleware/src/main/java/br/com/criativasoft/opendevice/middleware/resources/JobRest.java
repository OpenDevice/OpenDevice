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

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.middleware.jobs.JobManager;
import br.com.criativasoft.opendevice.middleware.model.jobs.JobSpec;
import br.com.criativasoft.opendevice.middleware.rules.RuleManager;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
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

        JobSpec spec = jobManager.getById(id);

        if(spec != null && !spec.getAccount().getUuid().equals( TenantProvider.getCurrentID())){
            throw new NotFoundException();
        }

        return spec;
    }

    @GET
    public List<JobSpec> list() throws IOException {
        List<JobSpec> list = jobManager.listAllByUser();
        return list;
    }

    @PUT @Path("/{id}/activate")
    public Response activate(@PathParam("id") long id, JobSpec spec, @QueryParam("value") boolean value) throws IOException {

        if(spec == null) throw new NotFoundException();

        if(spec != null && !spec.getAccount().getUuid().equals( TenantProvider.getCurrentID())){
            return ErrorResponse.UNAUTHORIZED("UNAUTHORIZED - Invalid Account !");
        }

        jobManager.update(spec);

        return Response.ok().build();
    }

    @POST
    public Response save(JobSpec rule) throws IOException {

        jobManager.persist(rule);

        return Response.ok().build();

    }

    @PUT
    @Path("{id}")
    public Response update(JobSpec spec) throws IOException {

        if(spec != null && !spec.getAccount().getUuid().equals( TenantProvider.getCurrentID())){
            return ErrorResponse.UNAUTHORIZED("UNAUTHORIZED - Invalid Account !");
        }

        jobManager.update(spec);

        return Response.ok().build();

    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) throws IOException {

        JobSpec spec = jobManager.getById(id);

        if(spec != null && !spec.getAccount().getUuid().equals( TenantProvider.getCurrentID())){
            return ErrorResponse.UNAUTHORIZED("UNAUTHORIZED - Invalid Account !");
        }

        jobManager.delete(spec);

        return Response.ok().build();

    }


}
