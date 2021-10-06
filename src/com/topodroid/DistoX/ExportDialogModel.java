/* @file ExportDialogModel.java
 *
 * @author marco corvi
 * @date oct 2021
 *
 * @brief TopoDroid 3d model export dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.c3out.ExportData;

// import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

// import android.content.Intent;

import android.content.Context;

import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;

public class ExportDialogModel extends MyDialog
                   implements AdapterView.OnItemSelectedListener
                   , View.OnClickListener
{
  private Button   mBtnOk;
  // private Button   mBtnBack;

  // private final IExporter mParent;
  private TopoGL    mParent;
  private TglParser mParser;
  private String[]  mTypes;
  private String    mSelected;
  private final int mTitle;
  private int mSelectedPos;

  // private LinearLayout mLayoutGltf;

  public ExportDialogModel( Context context, TopoGL parent, TglParser parser, String[] types, int title )
  {
    super( context, R.string.ExportDialog );
    mParent = parent;
    mParser = parser;
    mTypes  = types;
    mTitle  = title;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.export_dialog_model, mTitle );

    Spinner spin = (Spinner)findViewById( R.id.spin );
    spin.setOnItemSelectedListener( this );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mTypes );
    spin.setAdapter( adapter );

    // mLayoutGltf = (LinearLayout) findViewById( R.id.layout_gltf );

    mBtnOk   = (Button) findViewById(R.id.button_ok );
    mBtnOk.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back );
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_back ) ).setOnClickListener( this );

    mSelectedPos = 0;
    mSelected = mTypes[ mSelectedPos ];
    initOptions();
    // updateLayouts(); // not necessary
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mSelected = mTypes[ pos ];
    mSelectedPos = pos;
    // updateLayouts(); // not neceessary
  }

  @Override
  public void onNothingSelected( AdapterView av ) 
  { 
    mSelected = null;
    mSelectedPos = -1;
    // updateLayouts(); // not neceessary
  }

  @Override
  public void onClick(View v) 
  {
    // TDLog.v("C3D selected " + mSelected );
    Button b = (Button)v;
    if ( b == mBtnOk && mSelected != null ) {
      // setOptions(); // not necessary
      
      // mParent.doExport( mSelected );
      ExportData export = new ExportData( mParser.getName(), 
        ((CheckBox) findViewById( R.id.model_stations )).isChecked( ),
        ((CheckBox) findViewById( R.id.model_splays )).isChecked( ),
        ((CheckBox) findViewById( R.id.model_walls )).isChecked( ),
        ((CheckBox) findViewById( R.id.model_surface )).isChecked( ),
        true ); // overwrite
      mParent.selectExportFile( export );
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  // private void updateLayouts()
  // {
  //   // mLayoutGltf.setVisibility( View.GONE );
  // }

  // private void setOptions()
  // {
  //   int selected = mSelectedPos;
  //   switch ( selected ) {
  //     case 0: // GLTF
  //       break;
  //     case 1: // STL-bin
  //       break;
  //     case 2: // STL
  //       break;
  //     case 3: // CGAL
  //       break;
  //     case 4: // LAS-bin
  //       break;
  //     case 5: // DXF
  //       break;
  //     case 6: // KML
  //       break;
  //     case 6: // Shapefile
  //       break;
  //   }
  // }

  private void initOptions()
  {
    ((CheckBox) findViewById( R.id.model_stations )).setChecked( true );
    ((CheckBox) findViewById( R.id.model_splays )).setChecked( true );
    ((CheckBox) findViewById( R.id.model_walls )).setChecked( false );
    ((CheckBox) findViewById( R.id.model_surface )).setChecked( false );
    if ( ! mParser.hasWall() ) {
      ((CheckBox) findViewById( R.id.model_walls )).setVisibility( View.GONE );
    }
    if ( ! mParser.hasSurface() ) {
      ((CheckBox) findViewById( R.id.model_surface )).setVisibility( View.GONE );
    }
  }
}


