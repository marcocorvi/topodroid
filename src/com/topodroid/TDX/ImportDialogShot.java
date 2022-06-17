/* @file ImportDialogShot.java
 *
 * @author marco corvi
 * @date jul 2021
 *
 * @brief TopoDroid survey import dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.inport.ImportData;
// import com.topodroid.TDX.TDLevel;

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
// import android.widget.EditText;

import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;

public class ImportDialogShot extends MyDialog
                   implements AdapterView.OnItemSelectedListener
                   , View.OnClickListener
{
  private Button   mBtnOk;
  // private Button   mBtnBack;

  private final MainWindow mParent;
  private String[]  mTypes;
  private String    mSelected;
  private final int mTitle;
  private int mSelectedPos;

  private LinearLayout mLayoutZip;
  private LinearLayout mLayoutCompass;
  private LinearLayout mLayoutCaveSniper;
  private LinearLayout mLayoutSurvex;
  private LinearLayout mLayoutTherion;
  private LinearLayout mLayoutVTopo;
  // private LinearLayout mLayoutVTopoX;
  private LinearLayout mLayoutPTopo;

  private CheckBox mCBtroxTro; // VTopo
  private CheckBox mCBlrudTro;
  private CheckBox mCBlegTro;
  private CheckBox mCBdivingDat; // Compass
  private CheckBox mCBlrudDat;
  private CheckBox mCBlegDat;

  private ImportData mImportData;

  public ImportDialogShot( Context context, MainWindow parent, String[] types, int title )
  {
    super( context, null, R.string.ImportDialogShot ); // null app
    mParent = parent;
    mTypes  = types;
    mSelected = null;
    mTitle = title;
    // mImportData = new ImportData();
    mImportData = mParent.getImportData();
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.import_dialog_shot, mTitle );

    Spinner spin = (Spinner)findViewById( R.id.spin );
    spin.setOnItemSelectedListener( this );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mTypes );
    spin.setAdapter( adapter );

    mLayoutZip     = (LinearLayout) findViewById( R.id.layout_zip );
    mLayoutCompass = (LinearLayout) findViewById( R.id.layout_compass );
    mLayoutCaveSniper = (LinearLayout) findViewById( R.id.layout_cavesniper );
    mLayoutSurvex  = (LinearLayout) findViewById( R.id.layout_survex );
    mLayoutTherion = (LinearLayout) findViewById( R.id.layout_therion );
    mLayoutVTopo   = (LinearLayout) findViewById( R.id.layout_vtopo );
    // mLayoutVTopoX  = (LinearLayout) findViewById( R.id.layout_vtopox );
    mLayoutPTopo   = (LinearLayout) findViewById( R.id.layout_pockettopo );

    mBtnOk   = (Button) findViewById(R.id.button_ok );
    mBtnOk.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_back );
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_back ) ).setOnClickListener( this );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );

    mSelectedPos = 0;
    mSelected = mTypes[ mSelectedPos ];
    initOptions();
    updateLayouts();
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mSelected = mTypes[ pos ];
    mSelectedPos = pos;
    updateLayouts();
  }

  @Override
  public void onNothingSelected( AdapterView av ) 
  { 
    mSelected = null;
    mSelectedPos = -1;
    updateLayouts();
  }

  @Override
  public void onClick(View v) 
  {
    // TDLog.v( "Selected " + mSelected + " pos " + mSelectedPos );
    Button b = (Button)v;
    if ( b == mBtnOk && mSelected != null ) {
      setOptions();
      mParent.doImport( mSelected, mImportData );
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

  private void updateLayouts()
  {
    // TDLog.v( "update layout: selected " + mSelectedPos );
    mLayoutZip.setVisibility( View.GONE );
    mLayoutCompass.setVisibility( View.GONE );
    mLayoutCaveSniper.setVisibility( View.GONE );
    mLayoutSurvex.setVisibility( View.GONE );
    mLayoutTherion.setVisibility( View.GONE );
    mLayoutVTopo.setVisibility( View.GONE );
    // mLayoutVTopoX.setVisibility( View.GONE );
    mLayoutPTopo.setVisibility( View.GONE );
    switch ( mSelectedPos ) {
      case 0: mLayoutZip.setVisibility( View.VISIBLE );        break;
      case 1: mLayoutCompass.setVisibility( View.VISIBLE );    break;
      case 2: mLayoutCaveSniper.setVisibility( View.VISIBLE ); break;
      case 3: mLayoutSurvex.setVisibility( View.VISIBLE );     break;
      case 4: mLayoutTherion.setVisibility( View.VISIBLE );    break;
      case 5: mLayoutVTopo.setVisibility( View.VISIBLE );      break;
      // case x: mLayoutVTopoX.setVisibility( View.VISIBLE );  break;
      case 6: mLayoutPTopo.setVisibility( View.VISIBLE );      break;
    }
  }

  private void setOptions()
  {
    // TDLog.v("set options - pos " + mSelectedPos );
    switch ( mSelectedPos ) {
      // case 0: // Zip GEEK
      //   {
      //     TDSetting.mZipWithSymbols = ((CheckBox) findViewById( R.id.zip_symbols )).isChecked();
      //   }
      //   break;
      case 1: // Compass
        {
          mImportData.mLrud   = mCBlrudDat.isChecked();
          mImportData.mLeg    = mCBlegDat.isChecked();
          if ( TDLevel.overExpert ) mImportData.setDiving( mCBdivingDat.isChecked() );
        }
        break;
      case 2: // CaveSniper
        break;
      case 3: // Survex
        break;
      case 4: // Therion
        break;
      case 5: // VTopo
        {
          // TDLog.v("set VTopo options");
          mImportData.mLrud = mCBlrudTro.isChecked();
          mImportData.mLeg  = mCBlegTro.isChecked();
          mImportData.mTrox = mCBtroxTro.isChecked();
        }
        break;
      case 6: // PTopo
        break;
      default:
        break;
    }
  }

  private void initOptions()
  {
    mCBlrudDat = (CheckBox) findViewById( R.id.dat_lrud );   // Compass
    mCBlegDat  = (CheckBox) findViewById( R.id.dat_leg_first );
    mCBdivingDat = (CheckBox) findViewById( R.id.dat_diving );
    mCBlrudDat.setChecked( mImportData.mLrud );
    mCBlegDat .setChecked( mImportData.mLeg );
    mCBdivingDat.setChecked( mImportData.getDiving() );
    if ( ! TDLevel.overExpert ) {
      mCBdivingDat.setVisibility( View.GONE );
    }

    mCBlrudTro = (CheckBox) findViewById( R.id.tro_lrud );   // VTopo
    mCBlegTro  = (CheckBox) findViewById( R.id.tro_leg_first );
    mCBtroxTro = (CheckBox) findViewById( R.id.tro_trox );
    mCBlrudTro.setChecked( mImportData.mLrud );
    mCBlegTro .setChecked( mImportData.mLeg );
    mCBtroxTro.setChecked( mImportData.mTrox );
  }

}


