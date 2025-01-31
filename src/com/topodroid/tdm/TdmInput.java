/** @file TdmInput.h
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager survey input objects
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import android.view.View;
// import android.view.View.OnClickListener;
import android.widget.CheckBox;

class TdmInput extends TdmSurvey
               // implements View.OnClickListener
{
  boolean mChecked;
  // boolean m3dView;

  /** cstr
   * @param name   db survey name
   */
  TdmInput( String name )
  {
    super( name );
    mChecked = false;
    // m3dView  = true;
  }

  /** cstr
   * @param name  db survey name
   * @param color input color
   */
  TdmInput( String name, int color )
  {
    super( name, color );
    mChecked = false;
    // m3dView  = true;
  }

  /** @return the survey name
   */
  String getSurveyName() { return getName(); }

  // void toggleChecked() { mChecked = ! mChecked; }

  /** @return true if the input is selected
   */
  boolean isChecked() { return mChecked; }

  // boolean is3dView() { return m3dView; }

  /** switch the value of "checked" flag
   * @return the new value of the flag
   */
  boolean switchChecked() 
  { 
    mChecked = ! mChecked;
    return mChecked;
  }

  // boolean switch3dView() 
  // {
  //   m3dView = ! m3dView;
  //   return m3dView;
  // }

  // @Override
  // public void onClick( View v ) 
  // {
  //   mChecked = ! mChecked;
  //   ((CheckBox)v).setChecked( mChecked );
  // }

}

