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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
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
  private String[]  mTypes; // list of types in drop down 
  private boolean[] mEnable = TDConst.mSurveyExportEnable;
  private String    mSelected;
  private final int mTitle;
  private int mSelectedPos;
  private String    mSurvey;
  private String    mExportPrefix = null;
  private String    mExportName = null;
  private boolean   mDiving = false; // diving-mode survey

  private LinearLayout mLayoutZip;
  private LinearLayout mLayoutCompass;
  private LinearLayout mLayoutCSurvey;
  private LinearLayout mLayoutSurvex;
  private LinearLayout mLayoutTherion;
  private LinearLayout mLayoutWalls;
  private LinearLayout mLayoutTRobot;
  private LinearLayout mLayoutVTopo;
  private LinearLayout mLayoutCsv;
  private LinearLayout mLayoutDxf;
  private LinearLayout mLayoutKml;
  private LinearLayout mLayoutShp;
  // private LinearLayout mLayoutWinkarst;

  /** cstr
   * @param context     context
   * @param parent      parent window
   * @param types       export types, for the options
   * @param title       dialog title (resource)
   * @param survey      survey name
   * @param diving      whether survey is diving-mode
   */
  public ExportDialogShot( Context context, IExporter parent, String[] types, int title, String survey, boolean diving )
  {
    super( context, null, R.string.ExportDialog ); // null app
    mParent   = parent;
    mTypes    = types;
    mSelected = null;
    mTitle    = title;
    mSurvey   = survey;
    mDiving   = diving;
    mExportPrefix = null;
    mExportName   = null;
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

    mLayoutZip      = (LinearLayout) findViewById( R.id.layout_zip );
    mLayoutCompass  = (LinearLayout) findViewById( R.id.layout_compass );
    mLayoutCSurvey  = (LinearLayout) findViewById( R.id.layout_csurvey );
    mLayoutSurvex   = (LinearLayout) findViewById( R.id.layout_survex );
    mLayoutTherion  = (LinearLayout) findViewById( R.id.layout_therion );
    mLayoutWalls    = (LinearLayout) findViewById( R.id.layout_walls );
    mLayoutTRobot   = (LinearLayout) findViewById( R.id.layout_trobot );
    mLayoutVTopo    = (LinearLayout) findViewById( R.id.layout_vtopo );
    mLayoutCsv      = (LinearLayout) findViewById( R.id.layout_csv );
    mLayoutDxf      = (LinearLayout) findViewById( R.id.layout_dxf );
    mLayoutKml      = (LinearLayout) findViewById( R.id.layout_kml );
    mLayoutShp      = (LinearLayout) findViewById( R.id.layout_shp );
    // mLayoutWinkarst = (LinearLayout) findViewById( R.id.layout_winkarst );

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
    int ppos = TDConst.surveyIndex( pos );
    if ( ppos < 0 || ppos >= mTypes.length ) ppos = 0;
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
    int ppos = TDConst.surveyIndex( pos );
    TDLog.v( "EXPORT ppos " + ppos );
    if ( ppos < 0 ) return;
    mSelected = TDConst.mSurveyExportTypes[ ppos ];
    mSelectedPos = ppos;
    updateLayouts();
    TDLog.v( "EXPORT ppos " + ppos + " " + mSelected );
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
    Button b = (Button)v;
    if ( b == mBtnOk && mSelected != null ) {
      TDLog.v("Survey format selected " + mSelected + " " + TDConst.mSurveyExportIndex[ mSelectedPos ] );
      if ( ! setOptions() ) return;
      int selected_pos = ( mSelectedPos == TDConst.SURVEY_POS_VTOPO && TDSetting.mVTopoTrox )? -mSelectedPos : mSelectedPos;
      if ( mExportName != null ) {
        mParent.doExport( mSelected, mExportName, mExportPrefix, false ); // second = false
      } else {
        mParent.doExport( mSelected, TDConst.getSurveyFilename( selected_pos, mSurvey ), mExportPrefix, false ); // second = false
      }
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  /** update the layouts
   */
  private void updateLayouts()
  {
    mLayoutZip.setVisibility( View.GONE );
    mLayoutCompass.setVisibility( View.GONE );
    mLayoutCSurvey.setVisibility( View.GONE );
    mLayoutSurvex.setVisibility( View.GONE );
    mLayoutTherion.setVisibility( View.GONE );
    mLayoutWalls.setVisibility( View.GONE );
    mLayoutTRobot.setVisibility( View.GONE );
    mLayoutVTopo.setVisibility( View.GONE );
    mLayoutCsv.setVisibility( View.GONE );
    mLayoutDxf.setVisibility( View.GONE );
    mLayoutKml.setVisibility( View.GONE );
    mLayoutShp.setVisibility( View.GONE );
    // mLayoutWinkarst.setVisibility( View.GONE );
    switch ( mSelectedPos ) { // indices in mSurveyExportTypes
      case TDConst.SURVEY_POS_ZIP: mLayoutZip.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_COMPASS: mLayoutCompass.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_CSURVEY: mLayoutCSurvey.setVisibility( View.VISIBLE ); break;
      // case TDConst.SURVEY_POS_GHTOPO: // GHTopo
      // // case TDConst.SURVEY_POS_GROTTOLF: // Grottolf
      // // case TDConst.SURVEY_POS_PTOPO: // PocketTopo
      // case TDConst.SURVEY_POS_POLYGON: // Polygon
      case TDConst.SURVEY_POS_SURVEX:   mLayoutSurvex.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_THERION:  mLayoutTherion.setVisibility( View.VISIBLE ); break;
      // case TDConst.SURVEY_POS_TOPO:  // Topo
      case TDConst.SURVEY_POS_TOPOROBOT: mLayoutTRobot.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_VTOPO:    mLayoutVTopo.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_WALLS:    mLayoutWalls.setVisibility( View.VISIBLE ); break;
      // case TDConst.SURVEY_POS_WINKARST: mLayoutWinkarst.setVisibility( View.VISIBLE ); break; // WinKarst
      case TDConst.SURVEY_POS_CSV:      mLayoutCsv.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_DXF:      mLayoutDxf.setVisibility( View.VISIBLE ); break;
      case TDConst.SURVEY_POS_KML: // KML (same as GeoJson)
      // case TDConst.SURVEY_POS_GPX: // GPX
      // case TDConst.SURVEY_POS_GEOJSON: mLayoutKml.setVisibility( View.VISIBLE ); break; // GeoJson
      case TDConst.SURVEY_POS_SHAPEFILE: if ( TDLevel.overExpert) mLayoutShp.setVisibility( View.VISIBLE ); break;
    }
  }

  /** set the TRobot export filename
   * @param type   export type
   * @param view   the name edit text-field
   * @return false if name is illegal
   */
  private boolean setExportName( int type, EditText view )
  {
    Editable name = view.getText();
    if ( name == null ) {
      mExportName = null;
      return true;
    } else {
      String export_name =name.toString().trim();
      int len = export_name.length();
      if ( len == 0 ) {
        mExportName = null;
        return true;
      } else {
        boolean ok = true;
        int n_ch = 0;
        StringBuilder sb = new StringBuilder();
        int dots = 0;
        for ( int i = 0; i<len; ++i ) if ( export_name.charAt( i ) == '.' ) dots++;
        for ( int i = 0; i<len; ++i ) {
          char ch = export_name.charAt( i );
          if ( ch >= 'A' && ch <= 'Z' ) {
            if ( n_ch < 8 || dots == 1 ) {
              sb.append( ch );
              ++ n_ch;
            } else {
              ok = false;
            }
          } else if ( ch >= 'a' && ch <= 'z' ) { // a-z
            if ( n_ch < 8 || dots == 1 ) {
              sb.append( ch );
              ++ n_ch;
            } else {
              ok = false;
            }
          } else if ( ch >= '0' && ch <= '9' ) { // 0-9
            if ( n_ch < 8 || dots == 1 ) {
              sb.append( ch );
              ++ n_ch;
            } else {
              ok = false;
            }
          } else if ( ch == '.' ) {
            if ( dots == 1 ) {
              sb.append( ch );
            } else {
              ok = false;
              -- dots;
            }
          } else {
            ok = false;
          }
        }
        export_name = sb.toString();
        TDLog.v( "export name " + export_name + " ok " + ok );
        if ( ! ok ) {
          view.setError( mContext.getResources().getString( R.string.error_bad_name ) );
          view.setText( export_name );
        }
        len = export_name.length();
        if ( len == 0 ) {
          mExportName = null;
        } else {
          mExportName = export_name;
        }
        view.setText( export_name );
        return ok;
      }
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
   * @return true if options are OK, false otherwise
   */
  private boolean setOptions()
  {
    switch ( mSelectedPos ) {
      case TDConst.SURVEY_POS_ZIP: // Zip 
        {
          // TDSetting.mZipWithSymbols = ((CheckBox) findViewById( R.id.zip_symbols )).isChecked();
          TDSetting.mZipShare = ((CheckBox) findViewById( R.id.zip_share )).isChecked();
        }
        break;
      case TDConst.SURVEY_POS_COMPASS: // Compass
        {
          // TDSetting.mExportStationsPrefix = ((CheckBox) findViewById( R.id.compass_prefix )).isChecked();
          TDSetting.mCompassSplays = ((CheckBox) findViewById( R.id.compass_splays )).isChecked();
          TDSetting.mSwapLR = ((CheckBox) findViewById( R.id.compass_swap_lr )).isChecked();
          setExportPrefix( ((EditText) findViewById( R.id.compass_prefix )).getText() );
        }
        break;
      case TDConst.SURVEY_POS_CSURVEY: // CSurvey
        {
          TDSetting.mExportStationsPrefix = ((CheckBox) findViewById( R.id.csurvey_prefix )).isChecked();
        }
        break;
      // case TDConst.SURVEY_POS_GHTOPO: // GHTopo
      // case TDConst.SURVEY_POS_POLYGON: // Polygon
      case TDConst.SURVEY_POS_SURVEX: // Survex
        {
          TDSetting.mSurvexSplay = ((CheckBox) findViewById( R.id.survex_splay )).isChecked();
          TDSetting.mSurvexLRUD  = ((CheckBox) findViewById( R.id.survex_lrud )).isChecked();
        }
        break;
      case TDConst.SURVEY_POS_THERION: // Therion
        {
          TDSetting.mTherionWithConfig = ((CheckBox) findViewById( R.id.therion_config )).isChecked();
          TDSetting.mTherionMaps = ((CheckBox) findViewById( R.id.therion_maps )).isChecked();
          TDSetting.mSurvexLRUD  = ((CheckBox) findViewById( R.id.therion_lrud )).isChecked();
        }
        break;
      // case 7: // Topo
      // case 8: // TopoRobot
      case TDConst.SURVEY_POS_TOPOROBOT:
        {
          EditText name = (EditText) findViewById( R.id.trobot_name );
          if ( ! setExportName( TDConst.SURVEY_POS_TOPOROBOT, name ) ) return false;
        }
        break;
      case TDConst.SURVEY_POS_VTOPO: // VTopo
        {
          if ( mDiving ) {
            TDSetting.mVTopoTrox = true;
          } else {
            TDSetting.mVTopoTrox = ((CheckBox) findViewById( R.id.vtopo_trox )).isChecked();
          }
          TDSetting.mVTopoSplays = ((CheckBox) findViewById( R.id.vtopo_splays )).isChecked();
          TDSetting.mVTopoLrudAtFrom = ((CheckBox) findViewById( R.id.vtopo_lrud )).isChecked();
          setExportPrefix( ((EditText) findViewById( R.id.vtopo_suffix )).getText() );
        }
        break;
      case TDConst.SURVEY_POS_WALLS: // Walls
        {
          TDSetting.mWallsSplays = ((CheckBox) findViewById( R.id.walls_splays )).isChecked();
        }
        break;
      // case TDConst.SURVEY_POS_WINKARST: // Winkarst
      //   {
      //     setExportPrefix( ((EditText) findViewById( R.id.winkarst_prefix )).getText() );
      //   }
      //   break;
      case TDConst.SURVEY_POS_CSV: //CSV
        {
          TDSetting.mCsvRaw = ((CheckBox) findViewById( R.id.csv_rawdata )).isChecked();
        }
        break;
      case TDConst.SURVEY_POS_DXF: // DXF
        {
          TDSetting.mDxfBlocks = ((CheckBox) findViewById( R.id.dxf_blocks )).isChecked();
          // TDSetting.mAcadVersion
        }
        break;
      case TDConst.SURVEY_POS_KML: // KML
      // case TDConst.SURVEY_POS_GPX: // GPX
      // case TDConst.SURVEY_POS_GEOJSON: // GeoJSON
        {
          TDSetting.mKmlSplays = ((CheckBox) findViewById( R.id.kml_splays )).isChecked();
          TDSetting.mKmlStations = ((CheckBox) findViewById( R.id.kml_stations )).isChecked();
        }
        break;
      case TDConst.SURVEY_POS_SHAPEFILE: // Shapefile
        {
          TDSetting.mKmlSplays = ((CheckBox) findViewById( R.id.shp_splays )).isChecked();
          TDSetting.mKmlStations = ((CheckBox) findViewById( R.id.shp_stations )).isChecked();
          // TDSetting.mShpGeoref = ((CheckBox) findViewById( R.id.shp_georeference )).isChecked();
        }
        break;
    }
    return true;
  }

  /** initialize the options widgets
   */
  private void initOptions()
  {
    ((CheckBox) findViewById( R.id.zip_share )).setChecked( TDSetting.mZipShare );

    // ((CheckBox) findViewById( R.id.compass_prefix )).setChecked( TDSetting.mExportStationsPrefix );
    ((CheckBox) findViewById( R.id.compass_splays )).setChecked( TDSetting.mCompassSplays );
    ((CheckBox) findViewById( R.id.compass_swap_lr )).setChecked( TDSetting.mSwapLR );

    ((CheckBox) findViewById( R.id.csurvey_prefix )).setChecked( TDSetting.mExportStationsPrefix );

    ((CheckBox) findViewById( R.id.survex_splay )).setChecked( TDSetting.mSurvexSplay );
    ((CheckBox) findViewById( R.id.survex_lrud )).setChecked( TDSetting.mSurvexLRUD );

    ((CheckBox) findViewById( R.id.therion_config )).setChecked( TDSetting.mTherionWithConfig );
    ((CheckBox) findViewById( R.id.therion_maps )).setChecked( TDSetting.mTherionMaps );
    ((CheckBox) findViewById( R.id.therion_lrud )).setChecked( TDSetting.mSurvexLRUD );
    if ( mDiving ) {
      ((CheckBox) findViewById( R.id.vtopo_trox )).setVisibility( View.GONE );
    } else {
      ((CheckBox) findViewById( R.id.vtopo_trox )).setChecked( TDSetting.mVTopoTrox );
    }
    ((CheckBox) findViewById( R.id.vtopo_splays )).setChecked( TDSetting.mVTopoSplays );
    ((CheckBox) findViewById( R.id.vtopo_lrud )).setChecked( TDSetting.mVTopoLrudAtFrom );

    ((CheckBox) findViewById( R.id.walls_splays )).setChecked( TDSetting.mWallsSplays );

    ((CheckBox) findViewById( R.id.csv_rawdata )).setChecked( TDSetting.mCsvRaw );

    ((CheckBox) findViewById( R.id.dxf_blocks )).setChecked( TDSetting.mDxfBlocks );

    ((CheckBox) findViewById( R.id.kml_splays )).setChecked( TDSetting.mKmlSplays );
    ((CheckBox) findViewById( R.id.kml_stations )).setChecked( TDSetting.mKmlStations );

    ((CheckBox) findViewById( R.id.shp_splays )).setChecked( TDSetting.mKmlSplays );
    ((CheckBox) findViewById( R.id.shp_stations )).setChecked( TDSetting.mKmlStations );
    // ((CheckBox) findViewById( R.id.shp_georeference )).setChecked( TDSetting.mShpGeoref );

    ((EditText) findViewById( R.id.trobot_name )).setHint( mSurvey + ".trb");

  }


}


