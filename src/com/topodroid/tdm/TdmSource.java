/** @file TdmSource.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey source object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * source survey in TopoDroid database (essentially it is TdmInput)
 */
package com.topodroid.tdm;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

class TdmSource extends TdmFile
                implements View.OnClickListener
{
  boolean mChecked;

  public TdmSource( String surveyname )
  {
    super( null, surveyname );
    mChecked = false;
  }

  // void toggleChecekd() { mChecked = ! mChecked; }

  boolean isChecked() { return mChecked; }

  @Override
  public void onClick( View v ) 
  {
    mChecked = ! mChecked;
    ((CheckBox)v).setChecked( mChecked );
  }

}
