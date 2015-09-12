package br.com.criativasoft.opendevice.core.event;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Vector;

public class Teste {


    public static void main(String[] args) {
        System.out.println("Running ...");
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine nashorn = scriptEngineManager.getEngineByName("JavaScript");

        System.out.println(nashorn);

        Vector<byte[]> v = new Vector();
        while (true) {
            byte b[] = new byte[512];
            v.add(b);
            Runtime rt = Runtime.getRuntime();
            System.out.println("free memory: " + rt.freeMemory());
        }

    }
}
