/* @file ExpotInfo.java
 *
 * @author marco corvi
 * @date dec 2020
 *
 * @brief TopoDroid survey export info
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.common;

public class ExportInfo
{
  public int    index  = -1;
  public String prefix = null;
  public String name   = null;
  public long   first  = -1L;

  // /** default cstr
  //  */
  // public ExportInfo()
  // {
  //   // index  = -1;    // already in declaration
  //   // prefix = null;
  //   // name   = null;
  //   // first  = -1L;
  // }

  // /** cstr
  //  * @param idx   export type
  //  * @param pfx   station names prefix
  //  * @param nm    export file name
  //  */
  // public ExportInfo( int idx, String pfx, String nm ) 
  // {
  //   index  = idx;
  //   prefix = pfx;
  //   name   = nm;
  //   // first  = -1L;
  // }

  /** cstr
   * @param idx   export type
   * @param pfx   station names prefix
   * @param nm    export file name
   * @param fst   first shot-index to export
   */
  public ExportInfo( int idx, String pfx, String nm, long fst )
  {
    index  = idx;
    prefix = pfx;
    name   = nm;
    first  = fst;
  }


  /** set the index
   * @param idx   index (export type)
   */
  public void setIndex( int idx ) { index = idx; }

  /** set the prefix
   * @param pfx  station prefix
   */
  public void setPrefix( String pfx ) { prefix = pfx; }

  /** set the index
   * @param nm   export file name (with extension)
   */
  public void setName( String nm ) { name = nm; }

  /** set the first shot-index to export
   * @param fst   first
   */
  public void setFirst( int fst ) { first = fst; }

}
