/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.connection.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataUtils {
	
	public static int byteArrayToInt(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getInt();
	}
	
	public static long byteArrayToLong(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getLong();
	}

	public static byte[] toByteArray(int i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putInt(i);
	    return bb.array();
	}
	
	public static byte[] toByteArray(int[] values) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    for (int i : values) {
	    	bb.putInt(i);
		}
	    return bb.array();
	}
	
	public static byte[] toByteArray(long i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putLong(i);
	    return bb.array();
	}
	
	public static byte[] toByteArray(long[] values) {
	    final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    for (long i : values) {
	    	bb.putLong(i);
		}
	    return bb.array();		
	}
	
	/**
	 * NOTE: throws ArithmeticException if outside bounds
	 */
	public static int longToInt (long i) {
		return new BigDecimal(i).intValueExact();// 
	}


}
