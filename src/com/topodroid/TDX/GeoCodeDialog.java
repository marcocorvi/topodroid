/* @file GeoCodeDialog.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid geo codes dialog
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

  private final IGeoCoder mCoder;

  private GeoCodeAdapter mAdapter = null;
  private GeoCodes mCodes;

  /** cstr
   * @param context  context
   * @param coder    geomorphology coder
   * @param code     current code (in the coder)
   */
  GeoCodeDialog( Context context, IGeoCoder coder, String code )
  {
    super( context, null, R.string.GeoCodeDialog ); // null app
    mCoder = coder;
    mCodes = TopoDroidApp.getGeoCodes();
    mCodes.resetSelected();
    if ( code != null && code.length() > 0 ) {
      String [] codes = code.split(" ");
      for ( String cd : codes ) {
        mCodes.setSelected( cd );
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

    if ( mCodes.size() > 0 ) {
      mAdapter = new GeoCodeAdapter( mContext, R.layout.geocode_row, mCodes.getCodes() );
      list.setAdapter( mAdapter );
    }
  }

  @Override
  public void onClick(View view)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Photo Dialog onClick() " + view.toString() );
    if (view.getId() == R.id.code_ok ) {
      String code = (mAdapter == null)? "" : mAdapter.makeCode();
      TDLog.v("GeoCode <" + code + ">" );
      mCoder.setGeoCode( code );
    // } else if ( view.getId() == R.id.photo_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}
        

