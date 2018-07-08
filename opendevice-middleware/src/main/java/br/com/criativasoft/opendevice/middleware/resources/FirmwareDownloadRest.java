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

import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.middleware.model.Firmware;
import br.com.criativasoft.opendevice.middleware.persistence.dao.FirmwareDao;
import com.sun.jersey.api.NotFoundException;
import org.atmosphere.cpr.AtmosphereResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;

/**
 *
 * @author Ricardo JL Rufino
 *         Date: 08/07/18
 */
@Path("/middleware/firmwares/download")
// @RequiresAuthentication
@Produces(MediaType.APPLICATION_JSON)
public class FirmwareDownloadRest {

    private static final Logger log = LoggerFactory.getLogger(FirmwareDownloadRest.class);

    @PersistenceContext
    private EntityManager em;

    @Inject
    private FirmwareDao dao;

    @GET
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public ByteArrayOutputStream download(@Context AtmosphereResource request, @PathParam("uuid") String uuid) {

        Firmware firmware = dao.findByUUID(uuid);

        log.info("Request firmware: " + uuid + ", found: " + firmware);

        if(firmware == null) throw new NotFoundException();

        //request.getRequest().header("Transfer-Encoding", "xxxxxxxx");

        String baseDirectory = OpenDeviceConfig.getHomeDirectory()  + "/data";
        File file = new File(baseDirectory + "/" + firmware.getFilePath());

        log.info("Firmware File: " + file + "(exist : "+file.exists()+")");

        if(!file.exists()) throw new NotFoundException();

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
            baos.write(bytes, 0, bytes.length);
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
    }
}
