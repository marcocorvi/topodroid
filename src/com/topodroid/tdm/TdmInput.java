/** @file TdmInput.h
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager survey input objetcs
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import android.util.Log;

class TdmInput extends TdmSurvey
              implements View.OnClickListener
{
  boolean mChecked;

  // @param name   db survey name
  TdmInput( String name )
  {
    super( name );
    mChecked = false;
  }

  String getSurveyName() { return getName(); }

  // void toggleChecekd() { mChecked = ! mChecked; }

  boolean isChecked() { return mChecked; }

  @Override
  public void onClick( View v ) 
  {
    mChecked = ! mChecked;
    ((CheckBox)v).setChecked( mChecked );
  }

}

