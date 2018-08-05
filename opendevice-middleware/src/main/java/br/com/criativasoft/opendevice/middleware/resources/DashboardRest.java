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

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.model.DashboardItem;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DashboardDao;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import br.com.criativasoft.opendevice.wsrest.io.WebUtils;
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
import java.util.*;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 30/04/15.
 */
@Path("/middleware/dashboards")
@RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class DashboardRest {

    private static final Logger log = LoggerFactory.getLogger(DashboardRest.class);

    @PersistenceContext
    private EntityManager em;

    @Inject
    private DashboardDao dao;

    @GET
    @Path("/{id}")
    public Dashboard get(@PathParam("id") long id) throws IOException {
        return dao.getById(id);
    }

    @GET
    @Path("/{id}/activate")
    public Response activate(@PathParam("id") long id) throws IOException {

        dao.activate(dao.getById(id));

        return Response.status(Response.Status.OK).build();
    }

    @GET
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
    public List<DashboardItem> listItems(@PathParam("id") long id) throws IOException {

        Dashboard dashboard = dao.getById(id);

        return dao.listItems(dashboard.getId());

    }


    @DELETE @Path("/{id}")
    public Response remove(@PathParam("id") long id) throws IOException {

        Dashboard dashboard = dao.getById(id);

        if(dashboard == null) return ErrorResponse.status(Response.Status.NOT_FOUND, "Dashboard not found !");

        log.info("Removing Dash: " + dashboard.getTitle() + " #"+dashboard.getId());

        // change active
        if(dashboard.isActive()){
            List<Dashboard> list = dao.listAll();
            if(!list.isEmpty()) dao.activate(list.get(0));
        }

        Set<DashboardItem> items = dashboard.getItems();
        for (DashboardItem item : items) {
            dao.deleteItem(item);
        }

        dao.delete(dashboard);

        return Response.status(Response.Status.OK).build();
    }

    @POST
    public Dashboard save(Dashboard dashboard) throws IOException {

        if(dashboard.getId() > 0) dashboard = em.merge(dashboard);

        dao.persist(dashboard);

        if(dashboard.isActive()){
            dao.activate(dashboard);
        }

        return dashboard;

    }

    @GET @Path("/{id}/edit")
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
    public Response updateLayout(@PathParam("id") long id, DashboardItem item) throws IOException {
        System.err.println("DashboardRest.updateLayout: dash:" + id + "->" + item.getLayout());

        DashboardItem updated = em.find(DashboardItem.class, item.getId());

        updated.setLayout(item.getLayout());

        em.persist(updated);

        return Response.status(Response.Status.OK).build();
    }

    @POST @Path("/{id}/item")
    public Response saveItem(@PathParam("id") long id, DashboardItem item) throws IOException {

        boolean create = true;

        // do update !
        if(item.getId() > 0){
            DashboardItem updated = em.find(DashboardItem.class, item.getId());
            updated.setTitle(item.getTitle());
            updated.setType(item.getType());
            updated.setMonitoredDevices(item.getMonitoredDevices());
            updated.setPeriodType(item.getPeriodType());
            updated.setPeriodValue(item.getPeriodValue());
            updated.setPeriodEnd(item.getPeriodEnd());
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
    public List<String> deviceIcons() throws IOException {

        File homeDirectory = new File(ODev.getConfig().getHomeDirectory());
        List<String> externalResources = ODev.getConfig().getExternalResources();

        List<File> iconsPath = new LinkedList<>();

        // Check home
        File currentPath = new File(homeDirectory, "images/devices/on");
        if(currentPath.exists()){
            iconsPath.add(currentPath);
        }

        // Check external resouces
        for (String externalResource : externalResources) {
            currentPath = new File(externalResource, "images/devices/on");
            if(currentPath.exists()){
                iconsPath.add(currentPath);
            }
        }

        log.debug("Found icons directory: " + iconsPath.size());

        List<String> images = new LinkedList<String>();
        for (File path : iconsPath) {
            Collection<File> files = WebUtils.listFileTree(path);
            for (File file : files) {

                String absolutePath = file.getAbsolutePath();
                String relative = absolutePath.substring(absolutePath.indexOf("images/devices/on") + 18, absolutePath.length());
//                System.out.println("relative: "+ relative);
                images.add(relative);
            }
        }

        Collections.sort(images);

        return images;

    }
}
