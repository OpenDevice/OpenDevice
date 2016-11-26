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

package br.com.criativasoft.opendevice.restapi.auth;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;

/**
 * Class responsible for encrypting data using a randonomic key created at runtime when the server is started.
 * @see AesCipherService#generateNewKey()
 * @author Ricardo JL Rufino
 * @date 26/11/16
 */
public class AesRuntimeCipher {

    private AesCipherService cipher;

    private byte key[];

    public AesRuntimeCipher(){
        cipher = new AesCipherService();
        key = cipher.generateNewKey().getEncoded();
    }


    /**
     * Encript text
     * If you encrypt the same text with the same key twice, you will get two different encrypted texts.
     * @param text
     * @return encrypted text in base64 format
     */
    public String encript(String text){
        ByteSource encrypt = cipher.encrypt(text.getBytes(), key);
        return encrypt.toBase64();
    }

    public String decript(String text){
        ByteSource decrypt = cipher.decrypt(Base64.decode(text), key);
        return new String(decrypt.getBytes());
    }

}
