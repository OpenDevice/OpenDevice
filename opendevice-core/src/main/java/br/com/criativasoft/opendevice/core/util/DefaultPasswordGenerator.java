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

package br.com.criativasoft.opendevice.core.util;

import java.util.EnumSet;
import java.util.HashSet;

/**
 *
 * @author Ricardo JL Rufino
 * @date 13/11/16
 */
public class DefaultPasswordGenerator extends PasswordGenerator{

    private static final char[] ALPHA_UPPER_CHARACTERS = { 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final char[] ALPHA_LOWER_CHARACTERS = { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    private static final char[] NUMERIC_CHARACTERS = { '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9' };

    public DefaultPasswordGenerator() {
        super(new HashSet<PasswordCharacterSet>(EnumSet.allOf(SummerCharacterSets.class)), 10, 14);
    }

    private enum SummerCharacterSets implements PasswordGenerator.PasswordCharacterSet {
        ALPHA_UPPER(ALPHA_UPPER_CHARACTERS, 1),
        ALPHA_LOWER(ALPHA_LOWER_CHARACTERS, 1),
        NUMERIC(NUMERIC_CHARACTERS, 1);

        private final char[] chars;
        private final int minUsage;

        private SummerCharacterSets(char[] chars, int minUsage) {
            this.chars = chars;
            this.minUsage = minUsage;
        }

        @Override
        public char[] getCharacters() {
            return chars;
        }

        @Override
        public int getMinCharacters() {
            return minUsage;
        }
    }

}
