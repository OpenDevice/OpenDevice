/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.middleware.resources;

import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.model.DashboardItem;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DashboardDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 30/04/15.
 */
@Path("dashboards")
public class DashboardRest {

    private static final Logger log = LoggerFactory.getLogger(DashboardRest.class);

    @PersistenceContext
    private EntityManager em;

    @Inject
    private DashboardDao dao;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Dashboard get(@PathParam("id") long id) throws IOException {
        return dao.getById(id);
    }

    @GET
    @Path("/{id}/activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activate(@PathParam("id") long id) throws IOException {

        dao.activate(dao.getById(id));

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Dashboard> list() throws IOException {
        return dao.listAll();
    }

    @GET @Path("{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DashboardItem> listItems(@PathParam("id") long id) throws IOException {

        Dashboard dashboard = em.find(Dashboard.class, id);

        // find default
        // FIXME: REMOVE THIS LOGIN, THIS MUST BY HANDLED IN VIEW
        if(dashboard == null){
            List<Dashboard> resultList = em.createQuery("from Dashboard order by id desc", Dashboard.class).setMaxResults(1).getResultList();
            if(!resultList.isEmpty()) dashboard = resultList.get(0);
        }

        return dao.listItems(dashboard.getId());

    }


    @GET @Path("/remove")
    @Produces(MediaType.APPLICATION_JSON)
    public String remove() throws IOException {

        TypedQuery<Dashboard> query = em.createQuery("from Dashboard", Dashboard.class);
        List<Dashboard> list = query.getResultList();

        TypedQuery<DashboardItem> query2 = em.createQuery("from DashboardItem", DashboardItem.class);
        List<DashboardItem> list2 = query2.getResultList();

        for (Dashboard dashboard : list) {
            em.remove(dashboard);
        }
        for (DashboardItem item : list2) {
            em.remove(item);
        }

        return "remove";
    }

    @GET @Path("/save")
    @Produces(MediaType.APPLICATION_JSON)
    public String save() throws IOException {

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("MyDash - " + new Date());
        em.persist(dashboard);

        return "Saved : " + new Date();

    }

    @GET @Path("/{id}/edit")
    @Produces(MediaType.APPLICATION_JSON)
    public Dashboard edit(@PathParam("id") long id) throws IOException {
        System.out.println("DashboardRest.get - createEntityManager instance : " + em.hashCode());

        Dashboard dashboard = em.find(Dashboard.class, id);
        Set<DashboardItem> items = dashboard.getItems();
        for (DashboardItem item : items) {
            item.setTitle("Chart" + item.getId() + " " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }

        return dashboard;
    }


    @PUT @Path("/{id}/updateLayout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLayout(@PathParam("id") long id, DashboardItem item) throws IOException {
        System.err.println("DashboardRest.updateLayout: dash:" + id + "->" + item.getLayout());

        DashboardItem updated = em.find(DashboardItem.class, item.getId());

        updated.setLayout(item.getLayout());

        em.persist(updated);

        return Response.status(Response.Status.OK).build();
    }

    @POST @Path("/{id}/item")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveItem(@PathParam("id") long id, DashboardItem item) throws IOException {

        System.err.println("DashboardRest.saveItem: dash:" + id + "->" + item.getLayout());

        boolean create = true;

        // do update !
        if(item.getId() > 0){
            DashboardItem updated = em.find(DashboardItem.class, item.getId());
            updated.setTitle(item.getTitle());
            updated.setType(item.getType());
            updated.setMonitoredDevices(item.getMonitoredDevices());
            updated.setPeriodType(item.getPeriodType());
            updated.setPeriodValue(item.getPeriodValue());
            updated.setRealtime(item.getRealtime());
            updated.setContent(item.getContent());
            updated.setScripts(item.getScripts());
            updated.setAggregation(item.getAggregation());
            updated.setItemGroup(item.getItemGroup());
            updated.setViewOptions(item.getViewOptions());
            item = updated;
            create = false;
        }else{
            Dashboard dashboard = em.find(Dashboard.class, id);
            dashboard.add(item);
            em.persist(dashboard);
        }

        em.persist(item);

        return Response.status(create ? Response.Status.CREATED : Response.Status.OK).entity(item).build();
    }

    @DELETE @Path("/{id}/item")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeItem(@PathParam("id") long id, @QueryParam("id") long itemID) throws IOException {

        DashboardItem item = em.find(DashboardItem.class, itemID);
        if(item != null) {
            //Dashboard dashboard = em.find(Dashboard.class, id);
            em.remove(item);
        }else{
            Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).build();
    }
}
