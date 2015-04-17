package br.com.criativasoft.opendevice.core.command;

/**
 * Interface that indicates that the command has additional data, 
 * in which case the command is responsible to make such data parser
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br) 
 * @date 29/01/2015
 */
public interface ExtendedCommand {
    
    void deserializeExtraData(String extradata);
    
    String serializeExtraData();

}
