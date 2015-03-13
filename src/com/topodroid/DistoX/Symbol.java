/** @file Symbol.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing symbol: 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.LinearLayout;

class Symbol // implements View.OnClickListener
{
  boolean mEnabled;  //!< whether the symbol is enabled in the library
  String  mThName;   // therion name
  int     mCsxLayer;    // cSurvey layer
  int     mCsxType;
  int     mCsxCategory;
  int     mCsxPen;      // pen (for lines)
  int     mCsxBrush;    // brush type (for areas)
  String  mCsx; // clipart path 

  /** default cstr
   */
  Symbol()
  {
    mEnabled = true;
    mThName  = null;
    mCsxLayer = -1;
    mCsxType  = -1;
    mCsxCategory = -1;
    mCsxPen   = 1;     // default pen is "1"
    mCsxBrush = 1;     // default brush is "1"
    mCsx = null;
  }

  Symbol( String th_name ) 
  { 
    mEnabled  = true;
    mThName   = th_name;
    mCsxLayer = -1;
    mCsxType  = -1;
    mCsxCategory = -1;
    mCsxPen   = 1;
    mCsxBrush = 1; 
    mCsx = null;
  }

  /** cstr 
   * @param enabled  whether the symbol is enabled
   */
  Symbol(boolean enabled ) 
  { 
    mEnabled  = enabled; 
    mThName   = null;
    mCsxLayer = -1;
    mCsxType  = -1;
    mCsxCategory = -1;
    mCsxPen   = 1;
    mCsxBrush = 1; 
    mCsx = null;
  }

  boolean isEnabled() { return mEnabled; }

  void setEnabled( boolean enabled ) { mEnabled = enabled; }

}
