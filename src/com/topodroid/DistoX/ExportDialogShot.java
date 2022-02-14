/* @file ExportDialogShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey export dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

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

import android.text.Editable;

import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;

public class ExportDialogShot extends MyDialog
                   implements AdapterView.OnItemSelectedListener
                   , View.OnClickListener
{
  private Button   mBtnOk;
  // private Button   mBtnBack;

  private final IExporter mParent;
  private String[]  mTypes;
  private String    mSelected;
  private final int mTitle;
  private int mSelectedPos;
  private String    mSurvey;
  private String    mExportPrefix;

  private LinearLayout mLayoutCompass;
  private LinearLayout mLayoutCSurvey;
  private LinearLayout mLayoutSurvex;
  private LinearLayout mLayoutTherion;
  private LinearLayout mLayoutVTopo;
  private LinearLayout mLayoutCsv;
  private LinearLayout mLayoutDxf;
  private LinearLayout mLayoutKml;
  private LinearLayout mLayoutShp;
  private LinearLayout mLayoutWinkarst;

  /** cstr
   * @param context     context
   * @param parent      parent window
   * @param types       export types, for the options
   * @param title       dialog title (resource)
   * @param survey      survey name
   */
  public ExportDialogShot( Context context, IExporter parent, String[] types, int title, String survey )
  {
    super( context, R.string.ExportDialog );
    mParent = parent;
    mTypes  = types;
    mSelected = null;
    mTitle = title;
    mSurvey = survey;
    mExportPrefix = null;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.export_dialog_shot, mTitle );

    Spinner spin = (Spinner)findViewById( R.id.spin );
    spin.setOnItemSelectedListener( this );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mTypes );
    spin.setAdapter( adapter );

    mLayoutCompass  = (LinearLayout) findViewById( R.id.layout_compass );
    mLayoutCSurvey  = (LinearLayout) findViewById( R.id.layout_csurvey );
    mLayoutSurvex   = (LinearLayout) findViewById( R.id.layout_survex );
    mLayoutTherion  = (LinearLayout) findViewById( R.id.layout_therion );
    mLayoutVTopo    = (LinearLayout) findViewById( R.id.layout_vtopo );
    mLayoutCsv      = (LinearLayout) findViewById( R.id.layout_csv );
    mLayoutDxf      = (LinearLayout) findViewById( R.id.layout_dxf );
    mLayoutKml      = (LinearLayout) findViewById( R.id.layout_kml );
    mLayoutShp      = (LinearLayout) findViewById( R.id.layout_shp );
    mLayoutWinkarst = (LinearLayout) findViewById( R.id.layout_winkarst );

    mBtnOk   = (Button) findViewById(R.id.button_ok );
    mBtnOk.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back );
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_back ) ).setOnClickListener( this );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

    // mSelectedPos = 0;
    // mSelected = mTypes[ mSelectedPos ];
    setSelected( spin );

    initOptions();
    updateLayouts();
  }

  /** set the initial selection according to the default export format
   * @param spin   drop-down list
   */
  private void setSelected( Spinner spin )
  {
    int pos = 0;
    if ( TDSetting.mExportShotsFormat >= 0 ) {
      for ( int k = 0; k < TDConst.mSurveyExportIndex.length; ++ k ) {
        if ( TDConst.mSurveyExportIndex[k] == TDSetting.mExportShotsFormat ) {
          pos = k;
          break;
        }
      }
    }
    mSelected = mTypes[ pos ];
    mSelectedPos = pos;
    spin.setSelection( pos );
  }

  /** react to an item selection
   * @param av    item adapter
   * @param v     item view
   * @param pos   item position
   * @param id    ?
   */
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mSelected = mTypes[ pos ];
    mSelectedPos = pos;
    updateLayouts();
  }

  /** react to a deselection
   * @param av    item adapter
   */
  @Override
  public void onNothingSelected( AdapterView av ) 
  { 
    mSelected = null;
    mSelectedPos = -1;
    updateLayouts();
  }

  /** react to a user tap
   * @param v  tapped view
   */
  @Override
  public void onClick(View v) 
  {
    // TDLog.v("C3D selected " + mSelected );
    Button b = (Button)v;
    if ( b == mBtnOk && mSelected != null ) {
      setOptions();
      int selected_pos = ( mSelectedPos == 11 && TDSetting.mVTopoTrox )? -mSelectedPos : mSelectedPos;
      mParent.doExport( mSelected, TDConst.getSurveyFilename( selected_pos, mSurvey ), mExportPrefix );
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  /** update the layouts
   */
  private void updateLayouts()
  {
    mLayoutCompass.setVisibility( View.GONE );
    mLayoutCSurvey.setVisibility( View.GONE );
    mLayoutSurvex.setVisibility( View.GONE );
    mLayoutTherion.setVisibility( View.GONE );
    mLayoutVTopo.setVisibility( View.GONE );
    mLayoutCsv.setVisibility( View.GONE );
    mLayoutDxf.setVisibility( View.GONE );
    mLayoutKml.setVisibility( View.GONE );
    mLayoutShp.setVisibility( View.GONE );
    mLayoutWinkarst.setVisibility( View.GONE );
    switch ( mSelectedPos ) { // indices in mSurveyExportTypes
      case 1: mLayoutCompass.setVisibility( View.VISIBLE ); break;
      case 2: mLayoutCSurvey.setVisibility( View.VISIBLE ); break;
      case 7: mLayoutSurvex.setVisibility( View.VISIBLE ); break;
      case 8: mLayoutTherion.setVisibility( View.VISIBLE ); break;
      case 11: mLayoutVTopo.setVisibility( View.VISIBLE ); break;
      case 13: mLayoutWinkarst.setVisibility( View.VISIBLE ); break;
      case 14: mLayoutCsv.setVisibility( View.VISIBLE ); break;
      case 15: mLayoutDxf.setVisibility( View.VISIBLE ); break;
      case 16: 
      case 18: mLayoutKml.setVisibility( View.VISIBLE ); break;
      case 19: if ( TDLevel.overExpert) mLayoutShp.setVisibility( View.VISIBLE ); break;
    }
  }

  /** set the export prefix
   * @param prefix   content of the prefix edit text-field
   */
  private void setExportPrefix( Editable prefix )
  {
    if ( prefix == null ) {
      mExportPrefix = null;
    } else {
      mExportPrefix = prefix.toString().trim();
      if ( mExportPrefix.length() == 0 ) mExportPrefix = null;
    }
  }

  /** set the options for the selected export
   */
  private void setOptions()
  {
    switch ( mSelectedPos ) {
      // case 0: // Zip GEEK
      //   {
      //     TDSetting.mZipWithSymbols = ((CheckBox) findViewById( R.id.zip_symbols )).isChecked();
      //   }
      //   break;
      case 1: // Compass
        {
          // TDSetting.mExportStationsPrefix = ((CheckBox) findViewById( R.id.compass_prefix )).isChecked();
          TDSetting.mCompassSplays = ((CheckBox) findViewById( R.id.compass_splays )).isChecked();
          TDSetting.mSwapLR = ((CheckBox) findViewById( R.id.compass_swap_lr )).isChecked();
          setExportPrefix( ((EditText) findViewById( R.id.compass_prefix )).getText() );
        }
        break;
      case 2: // CSurvey
        {
          TDSetting.mExportStationsPrefix = ((CheckBox) findViewById( R.id.csurvey_prefix )).isChecked();
        }
        break;
      case 7: // Survex
        {
          TDSetting.mSurvexSplay = ((CheckBox) findViewById( R.id.survex_splay )).isChecked();
          TDSetting.mSurvexLRUD  = ((CheckBox) findViewById( R.id.survex_lrud )).isChecked();
        }
        break;
      case 8: // Therion
        {
          TDSetting.mTherionConfig = ((CheckBox) findViewById( R.id.therion_config )).isChecked();
          TDSetting.mTherionMaps = ((CheckBox) findViewById( R.id.therion_maps )).isChecked();
          TDSetting.mSurvexLRUD  = ((CheckBox) findViewById( R.id.therion_lrud )).isChecked();
        }
        break;
      case 11: // VTopo
        {
          TDSetting.mVTopoTrox = ((CheckBox) findViewById( R.id.vtopo_trox )).isChecked();
          TDSetting.mVTopoSplays = ((CheckBox) findViewById( R.id.vtopo_splays )).isChecked();
          TDSetting.mVTopoLrudAtFrom = ((CheckBox) findViewById( R.id.vtopo_lrud )).isChecked();
          setExportPrefix( ((EditText) findViewById( R.id.vtopo_prefix )).getText() );
        }
        break;
      case 13: // Winkarst
        {
          setExportPrefix( ((EditText) findViewById( R.id.winkarst_prefix )).getText() );
        }
        break;
      case 14: //CSV
        {
          TDSetting.mCsvRaw = ((CheckBox) findViewById( R.id.csv_rawdata )).isChecked();
        }
        break;
      case 15: // DXF
        {
          TDSetting.mDxfBlocks = ((CheckBox) findViewById( R.id.dxf_blocks )).isChecked();
          // TDSetting.mAcadVersion
        }
        break;
      case 16: // KML
      case 18: // GeoJSON
        {
          TDSetting.mKmlSplays = ((CheckBox) findViewById( R.id.kml_splays )).isChecked();
          TDSetting.mKmlStations = ((CheckBox) findViewById( R.id.kml_stations )).isChecked();
        }
        break;
      case 19: // Shapefile
        {
          TDSetting.mKmlSplays = ((CheckBox) findViewById( R.id.shp_splays )).isChecked();
          TDSetting.mKmlStations = ((CheckBox) findViewById( R.id.shp_stations )).isChecked();
          // TDSetting.mShpGeoref = ((CheckBox) findViewById( R.id.shp_georeference )).isChecked();
        }
        break;
    }
  }

  /** initialize the options widgets
   */
  private void initOptions()
  {
    // ((CheckBox) findViewById( R.id.compass_prefix )).setChecked( TDSetting.mExportStationsPrefix );
    ((CheckBox) findViewById( R.id.compass_splays )).setChecked( TDSetting.mCompassSplays );
    ((CheckBox) findViewById( R.id.compass_swap_lr )).setChecked( TDSetting.mSwapLR );

    ((CheckBox) findViewById( R.id.csurvey_prefix )).setChecked( TDSetting.mExportStationsPrefix );

    ((CheckBox) findViewById( R.id.survex_splay )).setChecked( TDSetting.mSurvexSplay );
    ((CheckBox) findViewById( R.id.survex_lrud )).setChecked( TDSetting.mSurvexLRUD );

    ((CheckBox) findViewById( R.id.therion_config )).setChecked( TDSetting.mTherionConfig );
    ((CheckBox) findViewById( R.id.therion_maps )).setChecked( TDSetting.mTherionMaps );
    ((CheckBox) findViewById( R.id.therion_lrud )).setChecked( TDSetting.mSurvexLRUD );

    ((CheckBox) findViewById( R.id.vtopo_trox )).setChecked( TDSetting.mVTopoTrox );
    ((CheckBox) findViewById( R.id.vtopo_splays )).setChecked( TDSetting.mVTopoSplays );
    ((CheckBox) findViewById( R.id.vtopo_lrud )).setChecked( TDSetting.mVTopoLrudAtFrom );

    ((CheckBox) findViewById( R.id.csv_rawdata )).setChecked( TDSetting.mCsvRaw );

    ((CheckBox) findViewById( R.id.dxf_blocks )).setChecked( TDSetting.mDxfBlocks );

    ((CheckBox) findViewById( R.id.kml_splays )).setChecked( TDSetting.mKmlSplays );
    ((CheckBox) findViewById( R.id.kml_stations )).setChecked( TDSetting.mKmlStations );

    ((CheckBox) findViewById( R.id.shp_splays )).setChecked( TDSetting.mKmlSplays );
    ((CheckBox) findViewById( R.id.shp_stations )).setChecked( TDSetting.mKmlStations );
    // ((CheckBox) findViewById( R.id.shp_georeference )).setChecked( TDSetting.mShpGeoref );

  }


}


