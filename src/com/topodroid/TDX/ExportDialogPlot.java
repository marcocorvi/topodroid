/* @file ExportDialogPlot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey sketch export dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;

import java.util.Locale;

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

public class ExportDialogPlot extends MyDialog
                   implements AdapterView.OnItemSelectedListener
                   , View.OnClickListener
{
  private final static int PARENT_DRAWING  = 0; // parent types
  private final static int PARENT_OVERVIEW = 1;

  private Button   mBtnOk;
  // private Button   mBtnBack;

  private final IExporter mParent;
  private String[]  mTypes;
  private String    mSelected;
  private final int mTitle;
  private int mSelectedPos;
  private int mParentType; // parent type
  private String mPlotName1;
  private String mPlotName2 = null;
  private boolean mBothViews = false; // whether to export both views (local - independently set for each export) 

  private LinearLayout mLayoutTherion;
  private LinearLayout mLayoutCSurvey;
  private LinearLayout mLayoutDxf;
  private LinearLayout mLayoutSvg;
  private LinearLayout mLayoutShp;
  // private LinearLayout mLayoutPng; // NO_PNG
  private LinearLayout mLayoutPdf;
  private LinearLayout mLayoutXvi;

  /** cstr
   * @param context     context
   * @param parent      parent window
   * @param types       export types, for the options
   * @param title       dialog title (resource)
   * @param plotname1   name of the primary plot
   * @param plotname2   name of the secondary plot
   *
   * @note parent_type 0: drawing, 1: overview
   */
  public ExportDialogPlot( Context context, IExporter parent, String[] types, int title, String plotname1, String plotname2 )
  {
    super( context, null, R.string.ExportDialog ); // null app
    mParent = parent;
    mTypes  = types;
    mSelected = null;
    mTitle = title;
    mParentType = ( parent instanceof OverviewWindow ) ? PARENT_OVERVIEW : PARENT_DRAWING;
    mPlotName1  = plotname1;
    mPlotName2  = plotname2;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.export_dialog_plot, mTitle );

    Spinner spin = (Spinner)findViewById( R.id.spin );
    spin.setOnItemSelectedListener( this );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mTypes );
    spin.setAdapter( adapter );

    mLayoutTherion = (LinearLayout) findViewById( R.id.layout_therion );
    mLayoutCSurvey = (LinearLayout) findViewById( R.id.layout_csurvey );
    mLayoutDxf     = (LinearLayout) findViewById( R.id.layout_dxf );
    mLayoutSvg     = (LinearLayout) findViewById( R.id.layout_svg );
    mLayoutShp     = (LinearLayout) findViewById( R.id.layout_shp );
    // mLayoutPng     = (LinearLayout) findViewById( R.id.layout_png ); // NO_PNG
    mLayoutPdf     = (LinearLayout) findViewById( R.id.layout_pdf );
    mLayoutXvi     = (LinearLayout) findViewById( R.id.layout_xvi );

    if ( ! TDLevel.overAdvanced ) {
      ((CheckBox) findViewById( R.id.therion_xvi )).setVisibility( View.GONE );
      ((LinearLayout) findViewById( R.id.layout_therion_scale )).setVisibility( View.GONE );
      ((CheckBox) findViewById( R.id.svg_grid )).setVisibility( View.GONE );
      ((CheckBox) findViewById( R.id.svg_linedir )).setVisibility( View.GONE );
      if ( ! TDLevel.overExpert ) {
        ((CheckBox) findViewById( R.id.svg_roundtrip )).setVisibility( View.GONE );
      }
      ((CheckBox) findViewById( R.id.therion_bothviews )).setVisibility( View.GONE );
    }
    // FIXME hide all except therion_bothviews
    ((CheckBox) findViewById( R.id.svg_bothviews )).setVisibility( View.GONE );
    ((CheckBox) findViewById( R.id.dxf_bothviews )).setVisibility( View.GONE );
    ((CheckBox) findViewById( R.id.shp_bothviews )).setVisibility( View.GONE );
    ((CheckBox) findViewById( R.id.pdf_bothviews )).setVisibility( View.GONE );

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
    if ( TDSetting.mExportPlotFormat >= 0 ) {
      for ( int k = 0; k < TDConst.mPlotExportIndex.length; ++ k ) {
        if ( TDConst.mPlotExportIndex[k] == TDSetting.mExportPlotFormat ) {
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
    // TDLog.v("Export plot: " + mPlotName1 + " selected " + mSelected + " pos " + mSelectedPos  );
    Button b = (Button)v;
    if ( b == mBtnOk && mSelected != null ) {
      setOptions();
      if ( mParentType == PARENT_DRAWING ) { // plot
        mParent.doExport( mSelected, TDConst.getPlotFilename( mSelectedPos, mPlotName1 ), null, -1L, false );  // null prefix, -1=first
        if ( mBothViews && mPlotName2 != null ) {
          mParent.doExport( mSelected, TDConst.getPlotFilename( mSelectedPos, mPlotName2 ), null, -1L, true );  // null prefix, -1=first
        }
      } else { // overview
        mParent.doExport( mSelected, TDConst.getOverviewFilename( mSelectedPos, mPlotName1 ), null, -1L, false ); // null prefix, -1=first
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
    mLayoutTherion.setVisibility( View.GONE );
    mLayoutCSurvey.setVisibility( View.GONE );
    mLayoutDxf.setVisibility( View.GONE );
    mLayoutSvg.setVisibility( View.GONE );
    mLayoutShp.setVisibility( View.GONE );
    // mLayoutPng.setVisibility( View.GONE ); // NO_PNG
    mLayoutPdf.setVisibility( View.GONE );
    mLayoutXvi.setVisibility( View.GONE );
    if ( mParentType == PARENT_DRAWING ) { // SketchWindow
      switch ( mSelectedPos ) {
        case TDConst.PLOT_POS_THERION:   mLayoutTherion.setVisibility( View.VISIBLE ); break;
        case TDConst.PLOT_POS_CSURVEY:   mLayoutCSurvey.setVisibility( View.VISIBLE ); break;
        case TDConst.PLOT_POS_DXF:       mLayoutDxf.setVisibility( View.VISIBLE ); break;
        case TDConst.PLOT_POS_SVG:       mLayoutSvg.setVisibility( View.VISIBLE ); break;
        case TDConst.PLOT_POS_SHAPEFILE: if ( TDLevel.overExpert ) mLayoutShp.setVisibility( View.VISIBLE ); break;
        // case TDConst.PLOT_POS_PNG: mLayoutPng.setVisibility( View.VISIBLE ); break; // NO_PNG
        case TDConst.PLOT_POS_PDF: mLayoutPdf.setVisibility( View.VISIBLE ); break;
        case TDConst.PLOT_POS_XVI: mLayoutXvi.setVisibility( View.VISIBLE ); break;
        // case TDConst.PLOT_POS_TUNNEL: break;
      }
    } else { // mParentType == PARENT_OVERVIEW // OverviewWindow
      switch ( mSelectedPos ) {
        case TDConst.OVERVIEW_POS_THERION:    mLayoutTherion.setVisibility( View.VISIBLE ); break;
        // case TDConst.OVERVIEW_POS_CSURVEY: mLayoutCSurvey.setVisibility( View.VISIBLE ); break;
        case TDConst.OVERVIEW_POS_DXF:        mLayoutDxf.setVisibility( View.VISIBLE ); break;
        case TDConst.OVERVIEW_POS_SVG:        mLayoutSvg.setVisibility( View.VISIBLE ); break;
        case TDConst.OVERVIEW_POS_SHAPEFILE:  if ( TDLevel.overExpert ) mLayoutShp.setVisibility( View.VISIBLE ); break;
        // case TDConst.OVERVIEW_POS_PNG: mLayoutPng.setVisibility( View.VISIBLE ); break;
        case TDConst.OVERVIEW_POS_PDF:        mLayoutPdf.setVisibility( View.VISIBLE ); break;
        case TDConst.OVERVIEW_POS_XVI:        mLayoutXvi.setVisibility( View.VISIBLE ); break;
      }
    }
  }

  /** set the options for the selected export
   * indices:    0   1   2   3   4   5   6   7   8
   * DRAWING:   th2 csx dxf svg shp pdf xvi tnl c3d
   * OVERVIEW:  th2     dxf svg shp pdf xvi
   */
  private void setOptions()
  {
    int selected = mSelectedPos;
    if ( mParentType == PARENT_OVERVIEW ) {
      if ( selected > 0 ) ++selected;  // shift indices dxf .. xvi up - skip csx
    } 
    if ( selected > 6 ) return; // no options for tnl c3d
    switch ( selected ) {
      case 0: // Therion
        {
          TDSetting.mTherionSplays = ((CheckBox) findViewById( R.id.therion_splays )).isChecked();
          TDSetting.mTherionXvi = ((CheckBox) findViewById( R.id.therion_xvi )).isChecked();
          if ( mParentType == PARENT_DRAWING ) {
            mBothViews = ((CheckBox) findViewById( R.id.therion_bothviews )).isChecked();
          }
          try { 
            TDSetting.setExportScale( Integer.parseInt( ((EditText) findViewById( R.id.therion_scale )).getText().toString() ) );
          } catch ( NumberFormatException e ) {
            TDLog.Error("export scale: not integer");
          }
          try { 
            TDSetting.setBezierStep( Float.parseFloat( ((EditText) findViewById( R.id.therion_spacing )).getText().toString() ) );
          } catch ( NumberFormatException e ) {
            TDLog.Error("export spacing: bad value");
          }
        }
        break;
      case 1: // CSurvey
        {
          TDSetting.mExportStationsPrefix = ((CheckBox) findViewById( R.id.csurvey_prefix )).isChecked();
        }
        break;
      case 2: // DXF
        {
          // TDSetting.mDxfBlocks = ((CheckBox) findViewById( R.id.dxf_blocks )).isChecked();
          TDSetting.mAutoXSections = ((CheckBox) findViewById( R.id.dxf_xsections )).isChecked();
          TDSetting.mDxfReference  = ((CheckBox) findViewById( R.id.dxf_reference )).isChecked();
          TDSetting.mAcadLayer     = ((CheckBox) findViewById( R.id.dxf_layers )).isChecked();
          if ( mParentType == PARENT_DRAWING ) {
            mBothViews = ((CheckBox) findViewById( R.id.dxf_bothviews )).isChecked();
          }
          // TDSetting.mAcadVersion
          if ( ((RadioButton) findViewById( R.id.acad_12 )).isChecked() ) {
            TDSetting.mAcadVersion = 13;
          } else if ( ((RadioButton) findViewById( R.id.acad_16 )).isChecked() ) {
            TDSetting.mAcadVersion = 16;
          } else {
            TDSetting.mAcadVersion =  9;
          }
        }
        break;
      case 3: // SVG
        {
          TDSetting.mSvgRoundTrip  = ((CheckBox) findViewById( R.id.svg_roundtrip )).isChecked();
          TDSetting.mSvgGrid       = ((CheckBox) findViewById( R.id.svg_grid )).isChecked();
          TDSetting.mSvgLineDirection   = ((CheckBox) findViewById( R.id.svg_linedir )).isChecked();
          TDSetting.mSvgSplays     = ((CheckBox) findViewById( R.id.svg_splays )).isChecked();
          TDSetting.mAutoXSections = ((CheckBox) findViewById( R.id.svg_xsections )).isChecked();
          if ( mParentType == PARENT_DRAWING ) {
            mBothViews = ((CheckBox) findViewById( R.id.svg_bothviews )).isChecked();
          }
          try { 
            TDSetting.setExportScale( Integer.parseInt( ((EditText) findViewById( R.id.svg_scale )).getText().toString() ) );
          } catch ( NumberFormatException e ) {
            TDLog.Error("Not integer export scale");
          }
        }
        break;
      case 4: // Shapefile
        {
          TDSetting.mShpGeoref = ((CheckBox) findViewById( R.id.shp_georeference )).isChecked();
          if ( mParentType == PARENT_DRAWING ) {
            mBothViews = ((CheckBox) findViewById( R.id.shp_bothviews )).isChecked();
          }
          // TDLog.v( "shapefile set georef " + TDSetting.mShpGeoref );
        }
        break;
      // case 5: // NO_PNG
      //   {
      //     TDSetting.mTherionSplays = ((CheckBox) findViewById( R.id.png_splays )).isChecked();
      //     TDSetting.mSvgGrid   = ((CheckBox) findViewById( R.id.png_grid )).isChecked();
      //     if ( ((CheckBox) findViewById( R.id.png_bgcolor )).isChecked() ) TDSetting.mBitmapBgcolor = 0xffffffff;
      //     try { 
      //       float sc = Float.parseFloat( ((EditText) findViewById( R.id.png_scale )).getText().toString() );
      //       if ( sc > 0 ) TDSetting.mBitmapScale = sc;
      //     } catch ( NumberFormatException e ) {
      //       TDLog.Error("Non-number PNG scale");
      //     }
      //   }
      //   break;
      case 5: // PDF
        {
          // if ( ((CheckBox) findViewById( R.id.pdf_bgcolor )).isChecked() ) TDSetting.mPdfBgcolor = 0;
          // if ( ((CheckBox) findViewById( R.id.pdf_bgcolor )).isChecked() ) TDSetting.mBitmapBgcolor = 0xffffffff;
          // TDSetting.mTherionSplays = ((CheckBox) findViewById( R.id.pdf_splays )).isChecked();
          if ( mParentType == PARENT_DRAWING ) {
            mBothViews = ((CheckBox) findViewById( R.id.pdf_bothviews )).isChecked();
          }
          try { 
            TDSetting.setExportScale( Integer.parseInt( ((EditText) findViewById( R.id.pdf_scale )).getText().toString() ) );
          } catch ( NumberFormatException e ) {
            TDLog.Error("Not integer export scale");
          }
        }
        break;
      case 6: // XVI
        TDSetting.mSvgSplays = ((CheckBox) findViewById( R.id.xvi_splays )).isChecked();
        // TDLog.v("SVG splays: " + TDSetting.mSvgSplays );
        break;
    }
  }

  /** initialize the options widgets
   * @note the bothviews checkboxes are all unchecked
   */
  private void initOptions()
  {
    ((CheckBox) findViewById( R.id.therion_splays )).setChecked( TDSetting.mTherionSplays );
    ((CheckBox) findViewById( R.id.therion_xvi )).setChecked( TDSetting.mTherionXvi );
    ((EditText) findViewById( R.id.therion_scale )).setText( String.format( Locale.US, "%d", TDSetting.mTherionScale ) );
    ((EditText) findViewById( R.id.therion_spacing )).setText( String.format( Locale.US, "%.2f", TDSetting.mBezierStep ) );

    ((CheckBox) findViewById( R.id.csurvey_prefix )).setChecked( TDSetting.mExportStationsPrefix );
    
    // ((CheckBox) findViewById( R.id.dxf_blocks )).setChecked( TDSetting.mDxfBlocks );
    ((CheckBox) findViewById( R.id.dxf_xsections )).setChecked( TDSetting.mAutoXSections );
    ((CheckBox) findViewById( R.id.dxf_reference )).setChecked( TDSetting.mDxfReference );
    ((CheckBox) findViewById( R.id.dxf_layers    )).setChecked( TDSetting.mAcadLayer );
    // TDSetting.mAcadVersion
    
    ((CheckBox) findViewById( R.id.svg_roundtrip )).setChecked( TDSetting.mSvgRoundTrip );
    ((CheckBox) findViewById( R.id.svg_grid )).setChecked( TDSetting.mSvgGrid );
    ((CheckBox) findViewById( R.id.svg_linedir )).setChecked( TDSetting.mSvgLineDirection );
    ((CheckBox) findViewById( R.id.svg_splays )).setChecked( TDSetting.mSvgSplays );
    ((CheckBox) findViewById( R.id.svg_xsections )).setChecked( TDSetting.mAutoXSections );
    ((EditText) findViewById( R.id.svg_scale )).setText( String.format( Locale.US, "%d", TDSetting.mTherionScale ) );
    
    ((CheckBox) findViewById( R.id.shp_georeference )).setChecked( TDSetting.mShpGeoref );

    // ((CheckBox) findViewById( R.id.png_splays )).setChecked( TDSetting.mTherionSplays ); // NO_PNG
    // ((CheckBox) findViewById( R.id.png_grid )).setChecked( TDSetting.mSvgGrid ); // NO_PNG
    // ((CheckBox) findViewById( R.id.png_bgcolor )).setChecked( TDSetting.mBitmapBgcolor == 0xffffffff ); // NO_PNG
    // ((EditText) findViewById( R.id.png_scale )).setText( Float.toString( TDSetting.mBitmapScale ) ); // NO_PNG

    // ((CheckBox) findViewById( R.id.pdf_bgcolor )).setChecked( TDSetting.mBitmapBgcolor == 0xffffffff );
    // ((CheckBox) findViewById( R.id.pdf_splays )).setChecked( TDSetting.mTherionSplays );
    ((EditText) findViewById( R.id.pdf_scale )).setText( String.format( Locale.US, "%d", TDSetting.mTherionScale ) );

    ((CheckBox) findViewById( R.id.xvi_splays )).setChecked( TDSetting.mSvgSplays );
  }
}


