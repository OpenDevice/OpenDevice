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
package br.com.criativasoft.opendevice.core.filter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;

public class OnlyFilesWithExtension implements FilenameFilter {

  String extensions[];
  Collection<String> ignoredFiles;

  public OnlyFilesWithExtension(String... ext) {
    this.extensions = ext;
  }
  

  public OnlyFilesWithExtension(Collection<String> ignoredFiles, String... ext) {
    super();
    this.extensions = ext;
    this.ignoredFiles = ignoredFiles;
  }


  public boolean accept(File dir, String name) {
    
    if(ignoredFiles != null){
      if(ignoredFiles.contains(name)){
        return false;
      }
    }
    
    for (String ext : extensions) {
      if (name.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

}
