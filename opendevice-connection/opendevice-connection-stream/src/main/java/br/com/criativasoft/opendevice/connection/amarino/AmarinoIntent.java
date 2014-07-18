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

package br.com.criativasoft.opendevice.connection.amarino;

public interface AmarinoIntent {
	
	/**
	 * boolean in Android is in Arduino 0=false, 1=true
	 */
	public static final int BOOLEAN_EXTRA = 1;
	public static final int BOOLEAN_ARRAY_EXTRA = 2;
	/**
	 * byte is byte. In Arduino a byte stores an 8-bit unsigned number, from 0
	 * to 255.
	 */
	public static final int BYTE_EXTRA = 3;
	public static final int BYTE_ARRAY_EXTRA = 4;
	/**
	 * char is char. In Arduino stored in 1 byte of memory
	 */
	public static final int CHAR_EXTRA = 5;
	public static final int CHAR_ARRAY_EXTRA = 6;
	/**
	 * double is too large for Arduinos, better not to use this datatype
	 */
	public static final int DOUBLE_EXTRA = 7;
	public static final int DOUBLE_ARRAY_EXTRA = 8;
	/**
	 * float in Android is float in Arduino (4 bytes)
	 */
	public static final int FLOAT_EXTRA = 9;
	public static final int FLOAT_ARRAY_EXTRA = 10;
	/**
	 * int in Android is long in Arduino (4 bytes)
	 */
	public static final int INT_EXTRA = 11;
	public static final int INT_ARRAY_EXTRA = 12;
	/**
	 * long in Android does not fit in Arduino data types, better not to use it
	 */
	public static final int LONG_EXTRA = 13;
	public static final int LONG_ARRAY_EXTRA = 14;
	/**
	 * short in Android is like int in Arduino (2 bytes) 2^15
	 */
	public static final int SHORT_EXTRA = 15;
	public static final int SHORT_ARRAY_EXTRA = 16;
	/**
	 * String in Android is char[] in Arduino
	 */
	public static final int STRING_EXTRA = 17;
	public static final int STRING_ARRAY_EXTRA = 18;

}
