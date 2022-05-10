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
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;
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
// import android.widget.LinearLayout;
import android.widget.CheckBox;
// import android.widget.EditText;
// import android.widget.RadioButton;

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
  private final int mTitle;
  private String    mSelected; // selected value
  private int mSelectedPos;    // selected position

  // private LinearLayout mLayoutGltf;

  /** dialog that selects the export type of the 3D model
   * @param context
   * @param parent     3D viewer acivity
   * @param parser     3D model parser
   * @param types      supported 3D export types
   * @param title      dialog title
   */
  public ExportDialogModel( Context context, TopoGL parent, TglParser parser, String[] types, int title )
  {
    super( context, R.string.ExportDialogModel );
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

  /** respond to a tap on an item view
   * @param av     items adapter
   * @param v      tapped item view
   * @param pos    item position in the adapter
   * @param id     ...
   */
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mSelected = mTypes[ pos ];
    mSelectedPos = pos;
    // updateLayouts(); // not neceessary
  }

  /** respond to a deselection: clear the selected view and position
   * @param av     items adapter
   */
  @Override
  public void onNothingSelected( AdapterView av ) 
  { 
    mSelected = null;
    mSelectedPos = -1;
    // updateLayouts(); // not neceessary
  }

  /** respond to a user tap
   * @param v   tapped view
   */
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
      switch ( mSelectedPos ) {
        case 0: export.mType = ModelType.GLTF; break;
        case 1: export.mType = ModelType.CGAL_ASCII; break;
        case 2: export.mType = ModelType.STL_ASCII; break;
        case 3: export.mType = ModelType.STL_BINARY; break;
        case 4: export.mType = ModelType.LAS_BINARY; break;
        case 5: export.mType = ModelType.DXF_ASCII; break;
        case 6: export.mType = ModelType.KML_ASCII; break;
        case 7: export.mType = ModelType.SHP_ASCII; break;
        case 8: export.mType = ModelType.SERIAL; break; // TODO 
      }
      if ( export.mType >= 0 ) mParent.selectExportFile( export );
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

  /** initialize the export options according to the state of the 3D model
   */
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


