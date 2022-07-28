/* @file ParserException.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D parser exception
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

public class ParserException extends Exception
{
  String filename;
  int linenr;

  /** cstr
   * @param name   message (usually filename)
   * @param nr     line number
   */
  public ParserException( String name, int nr ) 
  {
    filename = name;
    linenr   = nr;
  }

  /** @return string presentation
   */
  public String msg() { return filename + ":" + linenr; }

}
