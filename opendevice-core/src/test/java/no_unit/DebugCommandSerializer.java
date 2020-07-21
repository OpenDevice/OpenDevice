package no_unit;

import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;

public class DebugCommandSerializer extends CommandStreamSerializer{
    
    @Override
    public Message parse( byte[] pkg ) {
        // TODO Auto-generated method stub
        return super.parse(pkg);
    }
    
    
    @Override
    public byte[] serialize( Message message ) {
        
        byte[] serialize = super.serialize(message);
        
        
        return serialize;
    }
    

}
