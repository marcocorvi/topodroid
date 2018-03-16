/** @file ExportDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey export dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;

import android.util.Log;

class ExportDialog extends MyDialog
                          implements AdapterView.OnItemSelectedListener, View.OnClickListener
{
  private Button   mBtnOk;
  private Button   mBtnBack;

  private IExporter mParent;
  private String[]  mTypes;
  private String    mSelected;
  private int mTitle;

  ExportDialog( Context context, IExporter parent, String[] types, int title )
  {
    super( context, R.string.ExportDialog );
    mParent = parent;
    mTypes  = types;
    mSelected = null;
    mTitle = title;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.export_dialog, mTitle );

    Spinner spin = (Spinner)findViewById( R.id.spin );
    spin.setOnItemSelectedListener( this );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mTypes );
    spin.setAdapter( adapter );

    mBtnOk = (Button) findViewById(R.id.button_ok );
    mBtnBack = (Button) findViewById(R.id.button_back );
    mBtnOk.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) { mSelected = mTypes[ pos ]; }

  @Override
  public void onNothingSelected( AdapterView av ) { mSelected = null; }

  @Override
  public void onClick(View v) 
  {
    // Log.v("DistoX", "Selected " + mSelected );
    Button b = (Button)v;
    if ( b == mBtnOk && mSelected != null ) {
      mParent.doExport( mSelected );
    } else if ( b == mBtnBack ) {
      /* nothing */
    }
    dismiss();
  }

}


