package br.com.criativasoft.opendevice.core.command.ext;

import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.ExtendedCommand;
import br.com.criativasoft.opendevice.core.util.StringUtils;

public class IrCommand extends DeviceCommand implements ExtendedCommand {

    private static final long serialVersionUID = -3696076229217533028L;

    enum Protocol {
        UNKNOWN(0),
        NEC(1),
        SONY(2),
        RC5(3),
        RC6(4),
        DISH(5),
        SHARP(6),
        PANASONIC(7),
        JVC(8),
        SANYO(9),
        MITSUBISHI(10),
        SAMSUNG(11),
        LG(12);

        int code;

        private Protocol(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return code;
        }
        
        public static Protocol getByCode(int code){
            Protocol[] values = Protocol.values();
            for (Protocol protocol : values) {
                if(protocol.getCode() == code)
                    return protocol;
            }
            
            return null;
        }

    }

    private Protocol protocol;

    private String rawValues;

    public IrCommand() {
        super();
    }

    public IrCommand(int deviceID, long value) {
        super(CommandType.INFRA_RED, deviceID, value);
    }

    public void setProtocol( Protocol protocol ) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return protocol;
    }
    
    public void setRawValues( String rawValues ) {
        this.rawValues = rawValues;
    }
    
    public String getRawValues() {
        return rawValues;
    }

    @Override
    public void deserializeExtraData( String extradata ) {
        System.out.println("setExtraData1:" + extradata);
        
        if(extradata.length() == 0) return;
        
        String[] data = extradata.split(""+Command.DELIMITER);
        String values = data[data.length - 1];
        char[] charvalues = values.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charvalues.length; i++) {
            char c = (char) (charvalues[i]);
            int v1 = (c >> 4) & 0xFF;
            int v2 = (c & 0xf) & 0xFF;
            sb.append(Integer.toString(v1));
            if(v2 != 0){ // zero is not allowed, all numbers are started from 1.
                sb.append(Integer.toString(v2));
            }
        }
        data[data.length - 1] = sb.toString();
        
        setRawValues(StringUtils.join(data, Command.DELIMITER));
        
        System.out.println("setExtraData2:" + rawValues);
        
//        Protocol protocol = Protocol.getByCode(Integer.parseInt(extradata[0]));
//        
//        System.out.println("protocolStr : " + protocol);
//        if(protocol != Protocol.UNKNOWN) return;
//
//        String labelsStr = extradata[1];
//        labelsStr = labelsStr.substring(1, labelsStr.length() - 1);
//        String[] labelsStrArray = labelsStr.split(",");
//        
//        System.out.println("Labels: " + Arrays.asList(labelsStrArray));
//
//        Map<Integer, Integer> labelValues = new HashMap<Integer, Integer>();
//        for (int i = 0; i < labelsStrArray.length; i++) {
//            String labelVal = labelsStrArray[i];
//            labelValues.put(i, Integer.parseInt(labelVal));
//        }
//
//        char[] valueStr = extradata[2].toCharArray();
//        for (char c : valueStr) {
//            System.out.print(labelValues.get(Character.digit(c, 10)));
//            System.out.print(",");
//        }
//
//        System.out.println();
    }

//    public static IrCommand RAW( int... values ) {
//        return RAW(-1, values); // -1 auto !
//    }
//
    
    @Override
    public String serializeExtraData() {
        return rawValues;
    }
    
    public static IrCommand RAW(int deviceID, String rawdata) {
        IrCommand command = new IrCommand(deviceID, 0);
        command.setRawValues(rawdata);
        return command;
    }

//  public static IrCommand RAW(int[] labels, int[]values){
//      
//  }
//  
}
