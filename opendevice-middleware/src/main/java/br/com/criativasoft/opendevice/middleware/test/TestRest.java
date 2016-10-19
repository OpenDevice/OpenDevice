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

import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantContext;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.DeviceVO;
import br.com.criativasoft.opendevice.core.model.Board;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.core.model.PhysicalDevice;
import br.com.criativasoft.opendevice.middleware.tools.SimulationService;
import br.com.criativasoft.opendevice.restapi.DeviceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        TenantContext context = TenantProvider.getCurerntContext();

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

        TenantContext context = TenantProvider.getCurerntContext();

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

        // Apenas com relacionamento
        // MATCH (s:Device)-[r:board]->(t:Device) RETURN s,r

//        Devices sem relacionamento...
//        MATCH (s:Device)
//        OPTIONAL MATCH (s)-[r:board]->(t:Device)
//        WITH s,r
//        WHERE r IS NULL and s.classType <> "Board" and s.applicationID = "7262e4d6-7e3e-4fef-9267-92b12d7800af"
//        RETURN s


        Set<Device> devices = new HashSet();

//        TypedQuery<PhysicalDevice> query = em.createQuery("select x from PhysicalDevice x JOIN x.board where  x.applicationID = :TENANT", PhysicalDevice.class);
//
//        query.setParameter("TENANT", TenantProvider.getCurrentID());
//
//        devices.addAll(query.getResultList());
//
//
//        TypedQuery<Board> boardQR = em.createQuery("select  x from Board x JOIN FETCH x.devices WHERE x.applicationID = :TENANT", Board.class);
//
//        boardQR.setParameter("TENANT", TenantProvider.getCurrentID());
//
//        devices.addAll(boardQR.getResultList());


        TypedQuery<Device> anotherQR = em.createQuery("select x from Device x where  x.applicationID = ?1", Device.class);

        anotherQR.setParameter(1, TenantProvider.getCurrentID());

        devices.addAll(anotherQR.getResultList());


        System.out.println("Devices : " + devices.size());

        for (Device device : devices) {

            if(device instanceof PhysicalDevice){
                System.out.println("Device: " + device);
                System.out.println(":: is Phy.ParantID:" + ((PhysicalDevice) device).getBoard());
            }

            if(device instanceof Board){
                System.out.println("Board : " + device.getName() + ", devices: " + ((Board) device).getDevices());

//                TypedQuery<PhysicalDevice> anotherQR = em.createQuery("from PhysicalDevice where board.uid = " + device.getUid(), PhysicalDevice.class);
//                List<PhysicalDevice> resultList = anotherQR.getResultList();
//                System.out.println(" >> " + resultList);
            }
        }

        return "OK";
    }


//    TypedQuery<Device> query = em.createQuery("select x from Device x where x.applicationID = :TENANT", Device.class);
//
//        query.setParameter("TENANT", TenantProvider.getCurrentID());
//
//    List<Device> list = query.getResultList();
//
//        System.out.println("Devices : " + list.size());
//
//        for (Device device : list) {
//
//        if(device instanceof PhysicalDevice){
//            System.out.println("Phy:" + ((PhysicalDevice) device).getBoard());
//        }
//
//        if(device instanceof Board){
//            System.out.println("Board : " + device.getName() + ", devices: " + ((Board) device).getDevices());
//
//            TypedQuery<PhysicalDevice> query2 = em.createQuery("from PhysicalDevice where board.uid = " + device.getUid(), PhysicalDevice.class);
//
//            List<PhysicalDevice> resultList = query2.getResultList();
//            System.out.println(" >> " + resultList);
//
//
//        }
//    }


    @GET
    @Path("/generateHistory/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String generateHistory(@PathParam("uid") int uid) {

        Device device = manager.findDeviceByUID(uid);

        int batchSize = 100;
        int interval = 30;
        int months = 2;
        int numberOfValues = ((months*30) * 24 * 60) / interval;

        int valueMax = 1000;

        Random random = new Random();
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MONTH, -months);

        System.out.println("Generating " + numberOfValues + " records");


        for (int i = 0; i < numberOfValues; i++){

            DeviceHistory history = new DeviceHistory();
            history.setValue(random.nextInt(valueMax));
            history.setTimestamp(calendar.getTime().getTime());
            history.setDeviceID(device.getId());

            System.out.println(i + ": " + calendar.getTime());
            dao.persistHistory(history);
            if(i % batchSize == 0) {
                //em.flush();
                //em.clear();
            }
            calendar.add(Calendar.MINUTE,interval);
        }


        if(device != null){
            return Long.toString(device.getValue());
        }

        return "";// TODO: return error ?
    }
}
