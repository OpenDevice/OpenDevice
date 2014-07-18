package br.com.criativasoft.openhouse.api;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.CommandDelivery;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class CommandDeliveryTest {
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		DeviceCommand command1 = new DeviceCommand(CommandType.ON_OFF, 1, 1);
		DeviceCommand command2 = new DeviceCommand(CommandType.ANALOG, 1, 1);
		
		CommandDelivery delivery = new CommandDelivery(null);
		DeviceConnection connection1 = new FakeConnection(6000);
		DeviceConnection connection2 = new FakeConnection(2000);
		
		delivery.sendTo(command1, connection1);
		delivery.sendTo(command2, connection2);
		
		while(true){
			Thread.sleep(1000);
		}
		
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//
//            public void run() {
//            if(!eExecutor.isShutdown()) {
//                eExecutor.shutdown();
//                // await termination code
//              }
//            }
//
//        });
		
	}
	
	
	private static class FakeConnection extends AbstractConnection {
		
		private static final Logger log = LoggerFactory.getLogger(FakeConnection.class);
		
		private long timeout;
		
		public FakeConnection(int timeout) {
			super();
			setStatus(ConnectionStatus.CONNECTED);
			this.timeout = timeout;
		}

        @Override
        public void connect() throws ConnectionException {

        }

        @Override
        public void disconnect() throws ConnectionException {

        }

        @Override
        public void send(final Message message) throws IOException {

			log.debug("Send : " + message);
            final Command command = (Command) message;

			Thread thread = new Thread(){
				@Override
				public void run() {
					try {
						Thread.sleep(timeout);
						
						log.debug("wait finish: " + message);
						
						ResponseCommand responseCommand = new ResponseCommand(CommandType.DEVICE_COMMAND_RESPONSE, command.getConnectionUUID());
						
						notifyListeners(responseCommand);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			
			thread.start();

		}
		
	}

}
