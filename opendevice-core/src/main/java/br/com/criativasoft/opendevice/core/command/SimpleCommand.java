package br.com.criativasoft.opendevice.core.command;

/**
 * Simple command with Type and Value (long)
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br) 
 * @date 17/02/2015
 */
public class SimpleCommand extends Command {

    private static final long serialVersionUID = -7718731300309607286L;
    
    public SimpleCommand(CommandType type, long value) {
        super(type);
        this.value = value;
    }

    private long value;
    
    public long getValue() {
        return value;
    }

        /**@see br.com.criativasoft.opendevice.core.command.CommandType#isDeviceCommand(CommandType) */
    public static final boolean isCompatible(CommandType type){
        return CommandType.isSimpleCommand(type);
    }
    
}
