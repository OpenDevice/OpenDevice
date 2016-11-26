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

import java.security.SecureRandom;
import java.util.*;

public class PasswordGenerator {

    private final List<PasswordCharacterSet> pwSets;
    private final char[] allCharacters;
    private final int minLength;
    private final int maxLength;
    private final int presetCharacterCount;

    public PasswordGenerator(Collection<PasswordCharacterSet> origPwSets, int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;

        // Make a copy of the character arrays and min-values so they cannot be changed after initialization
        int pwCharacters = 0;
        int preallocatedCharacters = 0;
        List<PasswordCharacterSet> pwSets = new ArrayList<PasswordCharacterSet>(origPwSets.size());
        for(PasswordCharacterSet origpwSet : origPwSets) {
            PasswordCharacterSet newPwSet = new PwSet(origpwSet);
            pwSets.add(newPwSet);
            pwCharacters += newPwSet.getCharacters().length;
            preallocatedCharacters += newPwSet.getMinCharacters();
        }
        this.presetCharacterCount = preallocatedCharacters;
        this.pwSets = Collections.unmodifiableList(pwSets);

        if (minLength < presetCharacterCount) {
            throw new IllegalArgumentException("Combined minimum lengths "
                    + presetCharacterCount
                    + " are greater than the minLength of " + minLength);
        }

        // Copy all characters into single array so we can evenly access all members when accessing this array
        char[] allChars = new char[pwCharacters];
        int currentIndex = 0;
        for(PasswordCharacterSet pwSet : pwSets) {
            char[] chars = pwSet.getCharacters();
            System.arraycopy(chars, 0, allChars, currentIndex, chars.length);
            currentIndex += chars.length;
        }
        this.allCharacters = allChars;
    }


    public String generatePassword() {
        SecureRandom rand = new SecureRandom();

        // Set pw length to minLength <= pwLength <= maxLength
        int pwLength = minLength + rand.nextInt(maxLength - minLength + 1);
        int randomCharacterCount = pwLength - presetCharacterCount;


        // Place each index in an array then remove them randomly to assign positions in the pw array
        List<Integer> remainingIndexes = new ArrayList<Integer>(pwLength);
        for(int i=0; i < pwLength; ++i) {
            remainingIndexes.add(i);
        }

        // Fill pw array
        char[] pw = new char[pwLength];
        for(PasswordCharacterSet pwSet : pwSets) {
            addRandomCharacters(pw, pwSet.getCharacters(), pwSet.getMinCharacters(), remainingIndexes, rand);
        }
        addRandomCharacters(pw, allCharacters, randomCharacterCount, remainingIndexes, rand);
        return new String(pw);
    }

    private static void addRandomCharacters(char[] pw, char[] characterSet,
            int numCharacters, List<Integer> remainingIndexes, Random rand) {
        for(int i=0; i < numCharacters; ++i) {
            // Get and remove random index from the remaining indexes
            int pwIndex = remainingIndexes.remove(rand.nextInt(remainingIndexes.size()));

            // Set random character from character index to pwIndex
            int randCharIndex = rand.nextInt(characterSet.length);
            pw[pwIndex] = characterSet[randCharIndex];
        }
    }

    public static interface PasswordCharacterSet {
        char[] getCharacters();
        int getMinCharacters();
    }

    /**
     * Defensive copy of a passed-in PasswordCharacterSet
     */
    private static final class PwSet implements PasswordCharacterSet {
        private final char[] chars;
        private final int minChars;

        public PwSet(PasswordCharacterSet pwSet) {
            this.minChars = pwSet.getMinCharacters();
            char[] pwSetChars = pwSet.getCharacters();
            // Defensive copy
            this.chars = Arrays.copyOf(pwSetChars, pwSetChars.length);
        }

        @Override
        public char[] getCharacters() {
            return chars;
        }

        @Override
        public int getMinCharacters() {
            return minChars;
        }
    }
}