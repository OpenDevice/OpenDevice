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

package br.com.criativasoft.opendevice.middleware.test;

import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantContext;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.FirmwareUpdateCommand;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.middleware.tools.SimulationService;
import br.com.criativasoft.opendevice.restapi.resources.DeviceRest;
import org.hibernate.*;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 13/10/16
 */
@Path("/tests")
public class TestRest {

    private static final Logger log = LoggerFactory.getLogger(DeviceRest.class);

    @Inject
    private DeviceManager manager;

    @Inject
    private DeviceDao dao;

    @Inject
    private SimulationService simulationService;

    @PersistenceContext
    private EntityManager em;


    @GET
    @Path("/simulation/start/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startSimulation(@PathParam("uid") int uid, @QueryParam("interval") int interval) {
        Device device = manager.findDeviceByUID(uid);
        TenantContext context = TenantProvider.getCurrentContext();

        boolean started = simulationService.start(context, device, interval);

        if(started){
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

    };

    @GET
    @Path("/simulation/stop/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopSimulation(@PathParam("uid") int uid) {
        Device device = manager.findDeviceByUID(uid);
        simulationService.stop(device);
        return Response.ok().build();
    }

    @GET
    @Path("/simulation/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceVO> listSimulation() {

        TenantContext context = TenantProvider.getCurrentContext();

        List<DeviceVO> devices = new LinkedList<DeviceVO>();

        Collection<Device> deviceList = simulationService.list(context);

        if(deviceList != null){
            for (Device device : deviceList) {
                devices.add(new DeviceVO(device));
            }
        }

        return devices;
    }

    @GET
    @Path("/teste1")
    @Produces(MediaType.APPLICATION_JSON)
    public String teste1() {

        DateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

        Calendar calendar  =  Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Session session = ((Session) em.getDelegate()).getSessionFactory().openSession();

        System.out.println("Runing...");

        for (int i = 0; i < 31; i++) {

            // Set MIN
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long min = calendar.getTimeInMillis();

            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            long max = calendar.getTimeInMillis();


            Query query = session.createQuery("select min(value), max(value) from DeviceHistory where deviceID = 82815 and timestamp between "+min+" and " + max);

            Object[] result = (Object[]) query.uniqueResult();

            if(result[0] != null){
                int rMin = (int) Double.parseDouble(result[0].toString());
                int rMax = (int) Double.parseDouble(result[1].toString());
                float calc = ((rMax - rMin) / 1600f);
                System.out.println(sdf.format(calendar.getTime())  + ";" + rMin +";" + rMax +";" + (calc+"").replace(".", ","));
            }


            calendar.add(Calendar.DAY_OF_MONTH, 1);

        }


//        StatelessSession session = ((Session) em.getDelegate()).getSessionFactory().openStatelessSession();
//
//        double lastValue = 0;
//
//        Query query = session.createQuery("SELECT a FROM DeviceHistory a where a.deviceID = 82815 ORDER BY timestamp");
//        query.setFetchSize(Integer.valueOf(100));
//        query.setReadOnly(true);
//        query.setLockMode("a", LockMode.NONE);
//        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
//
//        DateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
//
//        System.out.println("Running ....");
//
//        while (results.next()) {
//            DeviceHistory addr = (DeviceHistory) results.get(0);
//
//            double value = addr.getValue();
//
//            // Report breaks
//            if( (value - lastValue) < -1){
//                System.out.println("Break in:" +  lastValue + ", to: " + value + ", at: " + sdf.format(new Date(addr.getTimestamp())));
//            }
//
//            lastValue = value;
//
//            // Do stuff
//        }
//        results.close();
        session.close();

        return "OK";
    }

//
//    @GET
//    @Path("/medidor-problemas")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String medidorProblemas() {
//
//        StatelessSession session = ((Session) em.getDelegate()).getSessionFactory().openStatelessSession();
//
//        double lastValue = 0;
//
//        Query query = session.createQuery("SELECT a FROM DeviceHistory a where a.deviceID = 82815 ORDER BY timestamp");
//        query.setFetchSize(Integer.valueOf(100));
//        query.setReadOnly(true);
//        query.setLockMode("a", LockMode.NONE);
//        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
//
//        DateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
//
//        System.out.println("Running ....");
//
//        while (results.next()) {
//            DeviceHistory addr = (DeviceHistory) results.get(0);
//
//            double value = addr.getValue();
//
//            // Report breaks
//            if( (value - lastValue) < -1){
//                System.out.println("Break in:" +  lastValue + ", to: " + value + ", at: " + sdf.format(new Date(addr.getTimestamp())));
//            }
//
//            lastValue = value;
//
//            // Do stuff
//        }
//        results.close();
//        session.close();
//
//        return "OK";
//    }

}
