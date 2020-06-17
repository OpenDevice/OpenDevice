package br.com.criativasoft.opendevice.core.command;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User-defined command, this is an easy way to extend OpenDevice protocol. <br/>
 * Allows you to perform custom method calls directly on device.
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br) 
 * @date 17/02/2015
 */
public class UserCommand extends Command implements ExtendedCommand{
    
    private static final long serialVersionUID = -2155798878419286601L;

    private String name;
    
    private List<Object> params = new LinkedList<Object>();
    
    public UserCommand(String commandName, Object ... params) {
        super(CommandType.USER_COMMAND);
        this.name = commandName;
        
        if(params != null){
            for (Object object : params) {
                 this.params.add(object);
            }
        }
    }

    public List<Object> getParams() {
        return params;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public void deserializeExtraData( String extradata ) {
        String[] strparams = extradata.split(Command.DELIMITER);
        params.addAll(Arrays.asList(Arrays.copyOfRange(strparams, 1, strparams.length)));
    }

    @Override
    public String serializeExtraData() {
        
        if(params.isEmpty()) return null;
        
        StringBuilder sb = new StringBuilder();
       
        Iterator<Object> it = params.iterator();
        
        sb.append(name);
        if(it.hasNext()) sb.append(DELIMITER);
        
        while (it.hasNext()) {
            Object object =  it.next();
            if(object instanceof Boolean){
                sb.append(((Boolean)object) ? 1 : 0);
            }else{
                sb.append(object.toString());
            }
            if(it.hasNext()) sb.append(DELIMITER);
            
        }
        return sb.toString();
    }

}
