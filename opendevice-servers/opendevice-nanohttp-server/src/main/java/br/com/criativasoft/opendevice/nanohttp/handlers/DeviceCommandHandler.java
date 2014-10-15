package br.com.criativasoft.opendevice.nanohttp.handlers;

import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.nanohttp.server.ControlServer;
import br.com.criativasoft.opendevice.nanohttp.server.NanoHTTPD;
import br.com.criativasoft.opendevice.nanohttp.server.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;


public class DeviceCommandHandler implements RequestHandler {
	
	private static final Logger log = LoggerFactory.getLogger(DeviceCommandHandler.class);

	@Override
	public NanoHTTPD.Response processRequest(String uri, Map<String, String> parms, ControlServer server) {
		StringBuffer msg = new StringBuffer(200);
		
		String deviceID = parms.get("id");
		String valueStr = parms.get("value");
		String cmdStr = parms.get("type");
		
		log.debug("ControlServer :: Received :: deviceID = " + deviceID + ", value = " + valueStr);
	
		if(! validate(parms, msg)){
			return new NanoHTTPD.Response( NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, msg.toString() );
		}
		
		CommandType type;
		int value = -1;
		
		if(cmdStr==null || cmdStr.trim().length() == 0){
			type = CommandType.DIGITAL;
		}else{
			type = CommandType.getByCode(Integer.parseInt(cmdStr));
		}
		
		if("true".equals(valueStr) || "false".equals(valueStr)){
			boolean bval = Boolean.parseBoolean(valueStr);
			value = (bval ? 1 : 0);
		}else{
			value = Integer.parseInt(valueStr);
		}
		
		// TODO: suportanto apenas DeviceCommand, poderia ter outros..
		Command command = new DeviceCommand(type, Integer.parseInt(deviceID), value);
		command.setTimestamp(new Date());
		
		// Notify Listeners about received command
		server.notifyListeners(command, Long.parseLong(deviceID));
		
		msg.append("{response:\"ok\", command: " + command + "}");
		
		return new NanoHTTPD.Response( NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, msg.toString() );
	}
	
	
	private boolean validate(Map<String, String> parms, StringBuffer msg){
		boolean valid =  true;
		
		String deviceID = parms.get("id");
		String value = parms.get("value");
		
		if(deviceID == null || deviceID.trim().length() == 0){
			valid = false;
			msg.append("Device Required!");
		}
		
		if(value == null || deviceID.trim().length() == 0){
			msg.append("Value Required!");
			valid = false;
		}
		
		// Checks 2
		if(valid){
			try{
				Long.parseLong(deviceID);
			}catch (Exception e) {
				valid = false;
				msg.append("Device must by a Number!");
			}
			
			try{
				if(!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
					Byte.parseByte(value);
			}catch (Exception e) {
				valid = false;
				msg.append("Value must by a Byte!");
			}
		}
		
		return valid;
	}

}
