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

package opendevice.io.tests.ipcam;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.ActionCommand;
import br.com.criativasoft.opendevice.core.command.GetDevicesRequest;
import br.com.criativasoft.opendevice.core.command.SetPropertyCommand;
import br.com.criativasoft.opendevice.core.model.test.CategoryIPCam;
import com.ning.http.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 10/01/16
 */
public class IPCamConnection extends AbstractConnection {

    private final static Logger log = LoggerFactory.getLogger(IPCamConnection.class);

    private String connectionURI; // TODO: isso limita apenas para 1 camera , e é o IDEAL...

    private BasicAuth auth;

    private AsyncHttpClient asyncHttpClient;

    private SnapshotListener snapshotListener;

    public IPCamConnection(String connectionURI, BasicAuth auth) {
        this.connectionURI = connectionURI;
        this.auth = auth;
        setSerializer(new IPCamGenericProtocol(auth, this));
    }

    @Override
    public void connect() throws ConnectionException {

        // FIXME: fazer a coneção e pegar os atributos.

        AsyncHttpClientConfig conf = new AsyncHttpClientConfig.Builder()
                .setAllowPoolingConnections(false)
                .setConnectTimeout(3000)
                .setRequestTimeout(5000).build();

        asyncHttpClient = new AsyncHttpClient(conf);

        setStatus(ConnectionStatus.CONNECTED);

        doGet("index.html", new AsyncCompletionHandler < Response > () {
            public Response onCompleted(Response response) throws Exception {
                setStatus(ConnectionStatus.CONNECTED);
                return response;
            }

            public void onThrowable(Throwable t) {
                setStatus(ConnectionStatus.FAIL);
            }
        });

//        doGet("get_status.cgi", new AsyncCompletionHandler < Response > () {
//
//            @Override
//            public Response onCompleted (Response response)throws Exception {
//                // Do something with the Response
//                // ...
//                System.out.println("Type:" + response.getContentType());
//                System.out.println("Status" + response.getStatusText());
//                System.out.println(response.getResponseBody());
//
//                snapshot();
//
//                return response;
//            }
//
//            @Override
//            public void onThrowable (Throwable t){
//                setStatus(ConnectionStatus.FAIL);
//                t.printStackTrace();
//            }
//        });
    }


    protected <T> void doGet(String url, AsyncHandler<T> handler){
        String requestURL = connectionURI + "/" + url;
        AsyncHttpClient.BoundRequestBuilder get = asyncHttpClient.prepareGet(requestURL);
        log.debug("Calling : " + requestURL);
        if(handler != null)  get.execute(handler);
        else get.execute();
    }

    @Override
    public void disconnect() throws ConnectionException {

    }

    @Override
    public void send(Message message) throws IOException {

        if(message instanceof GetDevicesRequest){
            getSerializer().serialize(message);
        }

        if(message instanceof SetPropertyCommand || message instanceof ActionCommand){

            byte[] bytes = getSerializer().serialize(message);

            if(bytes == null) return;

            // Snapshot
            if(message instanceof ActionCommand){
                ActionCommand command = (ActionCommand) message;
                if(CategoryIPCam.snapshot.getName().equals(command.getAction())){
                    if(snapshotListener != null) snapshotListener.onSnapshot(bytes);
                    return;
                }
            }

            String url = new String(bytes);

            doGet(url,new AsyncCompletionHandler < Response > () {

            @Override
            public Response onCompleted (Response response)throws Exception {
                // Do something with the Response
                // ...
                System.out.println("Type:" + response.getContentType());
                System.out.println("Status" + response.getStatusText());
                System.out.println(response.getResponseBody());


                return response;
            }

            @Override
            public void onThrowable (Throwable t){
                log.error(t.getMessage(), t);
            }
        });

        }

    }

    public void setSnapshotListener(SnapshotListener snapshotListener) {
        this.snapshotListener = snapshotListener;
    }

    public String getConnectionURI() {
        return connectionURI;
    }
}
