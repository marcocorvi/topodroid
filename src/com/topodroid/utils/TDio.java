/** @file TDio.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief TopoDroid I/O utility
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TDio
{

  /** @return a buffered reader on the given input stream or filepath
   * @param isr      input stream reader (null = not specified)
   * @param filename filepath - used if the input stream is not specified
   *
   */
  public static BufferedReader getBufferedReader( InputStreamReader isr, String filename )
  {
    try {
      if ( isr == null ) {
        isr = new InputStreamReader( new FileInputStream( filename ) );
      }
      return new BufferedReader( isr );
    } catch ( FileNotFoundException e ) {
      TDLog.Error("File not found");
    }
    return null;
  }

  /** extract the name from a filepath - last token between '/' and '.'
   * @param filename   filepath
   * @return name
   */
  public static String extractName( String filename )
  {
    int pos = filename.lastIndexOf( '/' );
    if ( pos < 0 ) { pos = 0; } else { ++pos; }
    int ext = filename.lastIndexOf( '.' ); if ( ext < 0 ) ext = filename.length();
    return filename.substring( pos, ext );
  }

}
 
