/* @file UndeleteItem.java
 *
 * @author marco corvi
 * @date july 2020
 *
 * @brief TopoDroid undelete items
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class UndeleteItem 
{
  // item types:
  final static int UNDELETE_PLOT = 0;
  final static int UNDELETE_SHOT = 1;
  final static int UNDELETE_OVERSHOOT = 2;
  final static int UNDELETE_CALIB_CHECK = 3;

  long id;
  boolean flag;
  String  text;
  int     type;

  /** cstr
   * @param _id     item ID
   * @param txt     display text
   * @param _type   item type
   */
  UndeleteItem( long _id, String txt, int _type )
  {
    id = _id;
    flag = false;
    text = txt;
    type = _type;
  }

  /** toggle the item flag
   */
  void flipFlag() { flag = ! flag; }

  /** @return the display text
   */
  String getText() 
  {
    return (flag? "[+] " : "[ ] ") + text;
  }
}
