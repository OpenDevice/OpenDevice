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
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 30/04/15.
 */
@Path("/middleware/dashboards")
@RequiresAuthentication
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
//
//        for (Dashboard dashboard : dao.listAll()) {
//            for (DashboardItem item : dashboard.getItems()) {
//                item.setLayout(null);
//            }
//        }

        return dao.listAll();
    }

    @GET @Path("{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DashboardItem> listItems(@PathParam("id") long id) throws IOException {

        Dashboard dashboard = dao.getById(id);

        return dao.listItems(dashboard.getId());

    }


    @DELETE @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") long id) throws IOException {

        Dashboard dashboard = dao.getById(id);

        dao.delete(dashboard);

        // change active
        if(dashboard.isActive()){
            List<Dashboard> list = dao.listAll();
            if(!list.isEmpty()) dao.activate(list.get(0));
        }

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Dashboard save(Dashboard dashboard) throws IOException {

        if(dashboard.getId() > 0) dashboard = em.merge(dashboard);

        dao.persist(dashboard);

        return dashboard;

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
            Dashboard dashboard = item.getParent();
            dashboard.getItems().remove(item);
            em.remove(item);
            em.persist(dashboard);
        }else{
            return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).build();
        }

        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).build();
    }

    @GET @Path("/deviceIcons")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> deviceIcons() throws IOException {
        // FIXME: get current path.
        File path = new File("/media/ricardo/Dados/Codidos/Java/Projetos/OpenDevice/opendevice-web-view/src/main/webapp/images/devices");
        List<String> images = new LinkedList<String>();
        File[] files = path.listFiles();
        for (File file : files) {
            images.add(file.getName());
        }
        return images;
    }
}
