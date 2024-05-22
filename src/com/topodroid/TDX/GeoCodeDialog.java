/* @file GeoCodeDialog.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid geocodes dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;


import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

// import android.graphics.*;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
// import android.widget.CheckBox;
import android.widget.ListView;


class GeoCodeDialog extends MyDialog
                    implements View.OnClickListener
{

  private final IGeoCoder mGeoCoder;

  private GeoCodeAdapter mAdapter = null;
  private GeoCodes mGeoCodes;

  /** cstr
   * @param context  context
   * @param coder    geomorphology coder
   * @param geocode     current geocode (in the coder)
   */
  GeoCodeDialog( Context context, IGeoCoder geocoder, String geocode )
  {
    super( context, null, R.string.GeoCodeDialog ); // null app
    mGeoCoder = geocoder;
    mGeoCodes = TopoDroidApp.getGeoCodes();
    mGeoCodes.resetSelected();
    if ( geocode != null && geocode.length() > 0 ) {
      String [] geocodes = geocode.split(" ");
      for ( String cd : geocodes ) {
        mGeoCodes.setSelected( cd );
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.geocode_dialog, R.string.geocode_title );

    ((Button) findViewById(R.id.code_ok)).setOnClickListener( this );
    ((Button) findViewById(R.id.code_cancel)).setOnClickListener( this );

    ListView list = (ListView)findViewById(R.id.code_list );
    list.setDividerHeight( 2 );

    if ( mGeoCodes.size() > 0 ) {
      mAdapter = new GeoCodeAdapter( mContext, R.layout.geocode_row, mGeoCodes.getGeoCodes() );
      list.setAdapter( mAdapter );
    }
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Photo Dialog onClick() " + view.toString() );
    if (view.getId() == R.id.code_ok ) {
      String geocode = (mAdapter == null)? "" : mAdapter.makeCode();
      TDLog.v("GeoCode <" + geocode + ">" );
      mGeoCoder.setGeoCode( geocode );
    // } else if ( view.getId() == R.id.photo_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}
        

