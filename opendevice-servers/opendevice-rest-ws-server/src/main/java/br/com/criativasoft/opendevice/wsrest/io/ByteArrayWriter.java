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

package br.com.criativasoft.opendevice.wsrest.io;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Ricardo JL Rufino
 * @date 19/11/16
 */
public class ByteArrayWriter implements MessageBodyWriter<ByteArrayOutputStream> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == ByteArrayOutputStream.class;
    }

    @Override
    public long getSize(ByteArrayOutputStream byteArrayOutputStream, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return byteArrayOutputStream.size();
    }

    @Override
    public void writeTo(ByteArrayOutputStream byteArrayOutputStream, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        httpHeaders.remove("Transfer-Encoding");
        entityStream.write(byteArrayOutputStream.toByteArray());
    }
}
