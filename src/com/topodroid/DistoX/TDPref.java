/* @file TDPref.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid option wrapper
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.content.res.Resources;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.method.KeyListener; 

import android.util.Log;

class TDPref implements AdapterView.OnItemSelectedListener
                      , View.OnClickListener
		      , View.OnFocusChangeListener
		      // , TextWatcher
		      , OnKeyListener
{
  TDPrefHelper helper = null;

  static final int FORWARD  = 0;
  static final int BUTTON   = 1;
  static final int CHECKBOX = 2;
  static final int EDITTEXT = 3;
  static final int LIST     = 4;

  static final int PREF     = 0;
  static final int INTEGER  = 1;
  static final int FLOAT    = 2;
  static final int STRING   = 3;
  static final int OPTIONS  = 4;
  static final int BOOLEAN  = 5;

  String name;
  int wtype;   // widget type
  String title;
  String summary; // -1 if no summary
  int level;
  int ptype;   // preference type
  String value = null;
  boolean bvalue = false;
  int     ivalue = 0;
  float   fvalue = 0;
  int     category;
  boolean commit = false; // whether need to commit value to DB

  String[] options;
  String[] values;

  View mView = null;
  EditText mEdittext = null;

  private TDPref( int cat, String nm, int wt, int tit, int sum, int lvl, int pt, String val, Resources res, TDPrefHelper hlp )
  {
    name  = nm;
    wtype = wt;
    title   = res.getString( tit );
    summary = (sum >= 0)? res.getString( sum ) : null;
    level = lvl;
    ptype = pt;
    value = val;
    options = null;
    values  = null;
    helper  = hlp;
    category = cat;
    commit   = false;
  }

  View getView( Context context, LayoutInflater li, ViewGroup parent )
  {
    View v = null;
    Spinner spinner;
    CheckBox checkbox;
    TextView textview;
    switch ( wtype ) {
      case FORWARD:
        v = li.inflate( R.layout.pref_forward, parent, false );
        break;
      case BUTTON:
        v = li.inflate( R.layout.pref_button, parent, false );
        ( (TextView) v.findViewById( R.id.value ) ).setText( stringValue() );
        break;
      case CHECKBOX:
        v = li.inflate( R.layout.pref_checkbox, parent, false );
	checkbox = (CheckBox) v.findViewById( R.id.checkbox );
	checkbox.setChecked( booleanValue() );
	checkbox.setOnClickListener( this );
        break;
      case EDITTEXT:
        v = li.inflate( R.layout.pref_edittext, parent, false );
	mEdittext = (EditText) v.findViewById( R.id.edittext );
	mEdittext.setText( stringValue() );
        switch ( ptype ) {
	  case INTEGER:
	    mEdittext.setInputType( TDConst.NUMBER );
            break;
	  case FLOAT:
	    mEdittext.setInputType( TDConst.NUMBER_DECIMAL );
            break;
	  // case STRING:
	  default:
            break;
        }
        // mEdittext.addTextChangedListener( this );
        mEdittext.setOnKeyListener(this);
        mEdittext.setOnFocusChangeListener(this);

        break;
      case LIST:
        v = li.inflate( R.layout.pref_spinner, parent, false );
	spinner = (Spinner) v.findViewById( R.id.spinner );
        spinner.setAdapter( new ArrayAdapter<>( context, R.layout.menu, options ) );
        spinner.setSelection( intValue() );
        spinner.setOnItemSelectedListener( this );
        break;
    }
    textview = (TextView) v.findViewById( R.id.title );
    textview.setMaxWidth( (int)(0.70f * TopoDroidApp.mDisplayWidth) );
    textview.setText( title );
    if ( summary == null ) {
      ((TextView) v.findViewById( R.id.summary )).setVisibility( View.GONE );
    } else {
      ((TextView) v.findViewById( R.id.summary )).setText( summary );
    }
    mView = v;
    return v;
  }

  View getView() { return mView; }

  // -------------------------------------------------------------------------
  // @Override
  // public void afterTextChanged( Editable e )
  // {
  //   setValue( e.toString() );
  //   // Log.v("DistoXPref", "TODO edit after text changed: " + value );
  //   // TDSetting.update( name, value );
  // }

  // @Override
  // public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) {  Log.v("DistoXPref", "edit before text changed"); }

  // @Override
  // public void onTextChanged( CharSequence cs, int start, int before, int cnt ) {  Log.v("DistoXPref", "edit on text changed"); }
  
  void commitValueString()
  {
    if ( commit && mEdittext != null ) {
      String val = mEdittext.getText().toString();
      if ( ! value.equals( val ) ) {
        setValue( val );
        // Log.v( "DistoXPref", "[*] " + name + " Keycode " + keyCode + " value " + value );
        TDSetting.updatePreference( helper, category, name, value );
      }
      commit = false;
    }
  }

  @Override
  public void onFocusChange( View v, boolean has_focus )
  {
    if ( (! has_focus) && ( v == mEdittext ) ) commitValueString();
  }

  @Override
  public void onClick(View v) 
  {
    switch ( v.getId() ) {
      case R.id.checkbox: // click always switches the checkbox
	bvalue = ((CheckBox)v).isChecked();
	value  = ( bvalue? "true" : "false" );
        // Log.v("DistoXPref", "TODO checkbox click: " + name + " val " + value );
	TDSetting.updatePreference( helper, category, name, value );
        break;
      case R.id.title:
        // Log.v("DistoXPref", "TODO title click tell TDSetting " + title );
        break;
    }
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id )
  {
    value  = options[pos];
    // Log.v("DistoXPref", "TODO item Selected: " + name + " index " + ivalue + " -> " + pos + " val " + value );
    if ( ivalue != pos ) {
      ivalue = pos;
      TDSetting.updatePreference( helper, category, name, values[ ivalue ] ); // options store the selected value
    }
  }

  @Override
  public void onNothingSelected( AdapterView av )
  {
    ivalue = Integer.parseInt( value );
    value  = options[ivalue];
    // Log.v("DistoXPref", "TODO nothing Selected: " + name + " index " + ivalue + " val " + value );
  }

  @Override
  public boolean onKey( View view, int keyCode, KeyEvent event)
  {
    if ( view == mEdittext ) {
      commit = true;
      if ( keyCode == 66 ) commitValueString();
    }
    return false;
  }

  // @Override
  // public void clearMetaKeyState( View v,  Editable e, int i ) { }

  // @Override
  // public boolean onKeyOther( View v, Editable e, KeyEvent evt ) { return false; }

  // @Override
  // public boolean onKeyUp( View v, Editable e, int code, KeyEvent evt ) { return false; }

  // @Override
  // public boolean onKeyDown( View v, Editable e, int code, KeyEvent evt ) { return false; }

  // @Override
  // public int getInputType() 
  // {
  //   switch ( ptype ) {
  //     case INTEGER: return TDConst.NUMBER;
  //     case FLOAT:   return TDConst.NUMBER_DECIMAL;
  //   }
  //   return InputType.TYPE_NULL;
  // }

  // -------------------------------------------------------------------------
  // creators
  
  static private TDPref makeForward( int cat, String nm, int tit, int lvl, Resources res, TDPrefHelper hlp )
  { 
    return new TDPref( cat, nm, FORWARD, tit, -1, lvl, PREF, null, res, hlp );
  }

  static private TDPref makeBtn( int cat, String nm, int tit, int sum, int lvl, String def_val, Resources res, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val );
    return new TDPref( cat, nm, BUTTON, tit, sum, lvl, PREF, val, res, hlp );
  }

  static private TDPref makeCbx( int cat, String nm, int tit, int sum, int lvl, boolean def_val, Resources res, TDPrefHelper hlp )
  { 
    boolean val = hlp.getBoolean( nm, def_val );
    TDPref ret = new TDPref( cat, nm, CHECKBOX, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), res, hlp );
    ret.bvalue = val;
    return ret;
  }

  static private TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, int pt, String def_val, Resources res, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val );
    TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, res, hlp );
    if ( pt == INTEGER ) {
      ret.ivalue = Integer.parseInt( ret.value );
    } else if ( pt == FLOAT ) {
      ret.fvalue = Float.parseFloat( ret.value );
    }
    return ret;
  }

  static private TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, int pt, int idef, Resources res, TDPrefHelper hlp)
  { 
    String val = hlp.getString( nm, res.getString(idef) );
    TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, res, hlp );
    // Log.v("DistoX", "EditText value " + ret.value );
    if ( pt == INTEGER ) {
      ret.ivalue = Integer.parseInt( ret.value );
    } else if ( pt == FLOAT ) {
      ret.fvalue = Float.parseFloat( ret.value );
    }
    return ret;
  }

  static private TDPref makeLst( int cat, String nm, int tit, int sum, int lvl, String def_val, int opts, int vals, Resources res, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val ); // options stores the selected value
    String[] options = res.getStringArray( opts );
    String[] values  = res.getStringArray( vals );
    // String opt = getOptionFromValue( val, options, values );
    TDPref ret = new TDPref( cat, nm, LIST, tit, sum, lvl, OPTIONS, val, res, hlp );
    ret.options = options;
    ret.values  = values;
    int idx = ret.makeLstIndex( );
    // Log.v("DistoXPref", "make list [1] " + nm + " val <" + val + "> index " + idx );
    return ret;
  }

  static private TDPref makeLst( int cat, String nm, int tit, int sum, int lvl, int idef, int opts, int vals, Resources res, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, res.getString(idef) ); // options stores the selected value
    String[] options = res.getStringArray( opts );
    String[] values  = res.getStringArray( vals );
    // String opt = getOptionFromValue( val, options, values );
    TDPref ret = new TDPref( cat, nm, LIST, tit, sum, lvl, OPTIONS, val, res, hlp );
    ret.options = options;
    ret.values  = values;
    int idx = ret.makeLstIndex( );
    // Log.v("DistoXPref", "make list [2] " + nm + " val <" + val + "> index " + idx );
    return ret;
  }

  // -----------------------------------------------------------------------

  private int makeLstIndex( )
  {
    // Log.v("DistoXPref", "make list index: val <" + value + "> opts size " + options.length );
    if ( value == null || value.length() == 0 ) {
      for ( int k=0; k< values.length; ++k ) { 
        if ( values[k].length() == 0 ) {
          ivalue = k;
          return k;
        }
      }
    } else {
      for ( int k=0; k< values.length; ++k ) { 
        if ( value.equals( values[k] ) ) {
          ivalue = k;
          return k;
        }
      }
    }
    return -1;
  }

  // private static String getOptionFromValue( String val, String[] opts, String[] vals )
  // {
  //   for ( int k=0; k< vals.length; ++k ) { 
  //     if ( val.equals( vals[k] ) ) {
  //       return opts[k];
  //     }
  //   }
  //   return null;
  // }

  // private String getOptionFromValue( String val )
  // {
  //   for ( int k=0; k< values.length; ++k ) { 
  //     if ( val.equals( values[k] ) ) {
  //       return options[k];
  //     }
  //   }
  //   return null;
  // }

  void setValue( String val )
  {
    value = val;
    if ( ptype == OPTIONS ) {
      makeLstIndex();
    } else if ( ptype == INTEGER ) {
      ivalue = Integer.parseInt( value );
    } else if ( ptype == FLOAT ) {
      fvalue = Float.parseFloat( value );
    } else if ( ptype == BOOLEAN ) {
      bvalue = Boolean.parseBoolean( value );
    }
  }

  void setButtonValue( String val )
  {
    if ( mView != null ) {
      TextView tv = (TextView) mView.findViewById( R.id.value );
      if ( tv != null ) tv.setText( val );
    }
    value = val;
  }

  int intValue()         { return ( ptype == INTEGER || ptype == OPTIONS )? ivalue : 0; }
  float floatValue()     { return ( ptype == FLOAT )? fvalue : 0; }
  String stringValue()   { return value; }
  boolean booleanValue() { return ( ptype == BOOLEAN )? bvalue : false; }

  // -----------------------------------------------
  static final int B = 0;
  static final int N = 1;
  static final int A = 2;
  static final int E = 3;
  static final int T = 4;
  static final int D = 5;

  static TDPref[] makeMainPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_ALL;
    String[] key = TDPrefKey.MAIN;
    TDPref[] ret = new TDPref[ 14 ];
    ret[ 0] = makeBtn( cat, key[0], R.string.pref_cwd_title, -1, N, "TopoDroid", res, hlp );
    ret[ 1] = makeEdt( cat, key[1], R.string.pref_text_size_title, R.string.pref_text_size_summary, B, INTEGER, R.string.default_textsize, res, hlp );
    ret[ 2] = makeLst( cat, key[2], R.string.pref_size_buttons_title, R.string.pref_size_buttons_summary, B, R.string.default_buttonsize, R.array.sizeButtons, R.array.sizeButtonsValue, res, hlp );
    ret[ 3] = makeLst( cat, key[3], R.string.pref_extra_buttons_summary, R.string.pref_extra_buttons_title, B, "1", R.array.extraButtons, R.array.extraButtonsValue, res, hlp );
    ret[ 4] = makeCbx( cat, key[4], R.string.pref_mkeyboard_title, R.string.pref_mkeyboard_summary, B, true, res, hlp );
    ret[ 5] = makeCbx( cat, key[5], R.string.pref_no_cursor_title, R.string.pref_no_cursor_summary, T, false, res, hlp );
    ret[ 6] = makeLst( cat, key[6], R.string.pref_local_help_title, R.string.pref_local_help_summary, A, "0", R.array.localUserMan, R.array.localUserManValue, res, hlp );
    ret[ 7] = makeCbx( cat, key[7], R.string.pref_cosurvey_title, R.string.pref_cosurvey_summary, D, false, res, hlp );
    ret[ 8] = makeLst( cat, key[8], R.string.pref_locale_title, R.string.pref_locale_summary, N, "", R.array.locale, R.array.localeValue, res, hlp );
    ret[ 9] = makeForward( cat, key[ 9], R.string.pref_cat_import_export, B, res, hlp );
    ret[10] = makeForward( cat, key[10], R.string.pref_cat_survey, B, res, hlp );
    ret[11] = makeForward( cat, key[11],   R.string.pref_cat_drawing, B, res, hlp );
    ret[12] = makeForward( cat, key[12], R.string.pref_cat_sketch, D, res, hlp );
    ret[13] = makeForward( cat, key[13], R.string.pref_cat_device, B, res, hlp );
    return ret;
  }

  static TDPref[] makeSurveyPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SURVEY;
    String[] key = TDPrefKey.SURVEY;
    TDPref[] ret = new TDPref[ 11 ];
    ret[ 0] = makeEdt( cat, key[0], R.string.pref_team_title, R.string.pref_team_summary, B, STRING, "", res, hlp );
    ret[ 1] = makeLst( cat, key[1], R.string.pref_survey_stations_title, R.string.pref_survey_stations_summary, B, "1", R.array.surveyStations, R.array.surveyStationsValue, res, hlp );
    ret[ 2] = makeLst( cat, key[2], R.string.pref_station_names_title, R.string.pref_station_names_summary, B, "alpha", R.array.stationNames, R.array.stationNamesValue, res, hlp );
    ret[ 3] = makeEdt( cat, key[3], R.string.pref_init_station_title,     R.string.pref_init_station_summary, B, STRING, "0", res, hlp );
    ret[ 4] = makeEdt( cat, key[4], R.string.pref_thumbnail_title,        R.string.pref_thumbnail_summary, A, INTEGER, "200", res, hlp );
    ret[ 5] = makeCbx( cat, key[5], R.string.pref_data_backup_title,      R.string.pref_data_backup_summary, B, false, res, hlp );
    ret[ 6] = makeCbx( cat, key[6], R.string.pref_shared_xsections_title, R.string.pref_shared_xsections_summary, B, false, res, hlp );
    ret[ 7] = makeForward( cat, key[ 7], R.string.pref_shot_units_title, B, res, hlp );
    ret[ 8] = makeForward( cat, key[ 8], R.string.pref_shot_data_title,  B, res, hlp );
    ret[ 9] = makeForward( cat, key[ 9], R.string.pref_location_title,   N, res, hlp );
    ret[10] = makeForward( cat, key[10], R.string.pref_accuracy_title,   A, res, hlp );
    return ret;
  }

  static TDPref[] makePlotPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_PLOT;
    String[] key = TDPrefKey.PLOT;
    TDPref[] ret = new TDPref[ 12 ];
    ret[0] = makeLst( cat, key[0], R.string.pref_picker_type_title, R.string.pref_picker_type_summary, B, "0", R.array.pickerType, R.array.pickerTypeValue, res, hlp );
    ret[1] = makeLst( cat, key[1], R.string.pref_recent_nr_title,   R.string.pref_recent_nr_summary, N, "4", R.array.recentNr, R.array.recentNr, res, hlp );
    ret[2] = makeCbx( cat, key[2], R.string.pref_side_drag_title,   R.string.pref_side_drag_summary, B, false, res, hlp );
    ret[3] = makeLst( cat, key[3], R.string.pref_zoom_controls_title, R.string.pref_zoom_controls_summary, B, "1", R.array.zoomCtrl, R.array.zoomCtrlValue, res, hlp );
    // ret[] = makeLst( cat, key[], R.string.pref_section_stations_title, R.string.pref_section_stations_summary, X, "3", R.array.sectionStations, R.array.sectionStationsValue, res, hlp );
    ret[ 4] = makeCbx( cat, key[4], R.string.pref_checkAttached_title, R.string.pref_checkAttached_summary, A, false, res, hlp );
    ret[ 5] = makeCbx( cat, key[5], R.string.pref_checkExtend_title,   R.string.pref_checkExtend_summary, A, true, res, hlp );
    ret[ 6] = makeLst( cat, key[6], R.string.pref_backup_number_title, R.string.pref_backup_number_summary, A, "5", R.array.backupNumber, R.array.backupNumberValue, res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], R.string.pref_backup_interval_title, R.string.pref_backup_interval_summary, A, INTEGER, "60", res, hlp );
    ret[ 8] = makeForward( cat, key[ 8], R.string.pref_tool_point_title,  B, res, hlp );
    ret[ 9] = makeForward( cat, key[ 9], R.string.pref_tool_line_title,   N, res, hlp );
    ret[10] = makeForward( cat, key[10], R.string.pref_plot_screen_title, B, res, hlp );
    ret[11] = makeForward( cat, key[11], R.string.pref_plot_walls_title,  T, res, hlp );
    return ret;
  }

  static TDPref[] makeCalibPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_CALIB;
    String[] key = TDPrefKey.CALIB;
    TDPref[] ret = new TDPref[ 11 ];
    ret[ 0] = makeLst( cat, key[ 0], R.string.pref_group_by_title, R.string.pref_group_by_summary, A, "1", R.array.groupBy, R.array.groupByValue, res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], R.string.pref_group_title, R.string.pref_group_summary, A, FLOAT, "40", res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], R.string.pref_error_title, R.string.pref_error_summary, B, FLOAT, "0.000001", res, hlp );
    ret[ 3] = makeEdt( cat, key[ 3], R.string.pref_iter_title, R.string.pref_iter_summary, B, INTEGER, "200", res, hlp );
    ret[ 4] = makeCbx( cat, key[ 4], R.string.pref_calib_shot_download_title, R.string.pref_calib_shot_download_summary, A, true, res, hlp );
    // ret[] = makeCbx( cat, key[], R.string.pref_raw_data_title, R.string.pref_raw_data_summary, X, false, res, hlp );
    ret[ 5] = makeLst( cat, key[ 5], R.string.pref_raw_data_title, R.string.pref_raw_data_summary, A, "0", R.array.rawCData, R.array.rawCDataValue, res, hlp );
    ret[ 6] = makeLst( cat, key[ 6], R.string.pref_calib_algo_title, R.string.pref_calib_algo_summary, E, "0", R.array.calibAlgo, R.array.calibAlgoValue, res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], R.string.pref_algo_min_alpha_title, R.string.pref_algo_min_alpha_summary, D, FLOAT, "0.1", res, hlp );
    ret[ 8] = makeEdt( cat, key[ 8], R.string.pref_algo_min_beta_title,  R.string.pref_algo_min_beta_summary,  D, FLOAT, "4.0", res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], R.string.pref_algo_min_gamma_title, R.string.pref_algo_min_gamma_summary, D, FLOAT, "1.0", res, hlp );
    ret[10] = makeEdt( cat, key[10], R.string.pref_algo_min_delta_title, R.string.pref_algo_min_delta_summary, D, FLOAT, "1.0", res, hlp );
    return ret;
  }

  static TDPref[] makeDevicePrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_DEVICE;
    String[] key = TDPrefKey.DEVICE;
    TDPref[] ret = new TDPref[ 13 ];
    // ret[] = makeEdt( cat, key[], R.string.pref_device_title, R.string.pref_device_summary, X, STRING, "", res, hlp );
    // ret[] = makeLst( cat, key[], R.string.pref_device_type_title, R.string.pref_device_type_summary, X, "1", R.array.deviceType, R.array.deviceTypeValue, res, hlp );
    ret[ 0] = makeLst( cat, key[ 0], R.string.pref_checkBT_title, R.string.pref_checkBT_summary, N, "1", R.array.deviceBT, R.array.deviceBTValue, res, hlp );
    ret[ 1] = makeLst( cat, key[ 1], R.string.pref_conn_mode_title, R.string.pref_conn_mode_summary, B, "0", R.array.connMode, R.array.connModeValue, res, hlp );
    ret[ 2] = makeCbx( cat, key[ 2], R.string.pref_auto_reconnect_title, R.string.pref_auto_reconnect_summary, B, false, res, hlp );
    ret[ 3] = makeCbx( cat, key[ 3], R.string.pref_head_tail_title, R.string.pref_head_tail_summary, B, false, res, hlp );
    ret[ 4] = makeLst( cat, key[ 4], R.string.pref_sock_type_title, R.string.pref_sock_type_summary, B, "0", R.array.sockType, R.array.sockTypeValue, res, hlp );
    // ret[ 5] = makeEdt( cat, key[ 5], R.string.pref_comm_retry_title, R.string.pref_comm_retry_summary, X, INTEGER, "1", res, hlp );
    ret[ 5] = makeCbx( cat, key[ 5], R.string.pref_z6_workaround_title, R.string.pref_z6_workaround_summary, N, true, res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], R.string.pref_socket_delay_title, R.string.pref_socket_delay_summary, E, INTEGER, "0", res, hlp );
    ret[ 7] = makeCbx( cat, key[ 7], R.string.pref_auto_pair_title, R.string.pref_auto_pair_summary, A, true, res, hlp );
    ret[ 8] = makeEdt( cat, key[ 8], R.string.pref_wait_data_title, R.string.pref_wait_data_summary, A, INTEGER, "250", res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], R.string.pref_wait_conn_title, R.string.pref_wait_conn_summary, A, INTEGER, "500", res, hlp );
    ret[10] = makeEdt( cat, key[10], R.string.pref_wait_laser_title, R.string.pref_wait_laser_summary, A, INTEGER, "1000", res, hlp );
    ret[11] = makeEdt( cat, key[11], R.string.pref_wait_shot_title, R.string.pref_wait_shot_summary, A, INTEGER, "4000", res, hlp );
    ret[12] = makeForward( cat, key[12], R.string.pref_cat_calib, B, res, hlp );
    return ret;
  }

  static TDPref[] makeExportPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_EXPORT;
    String[] key = TDPrefKey.EXPORT;
    TDPref[] ret = new TDPref[ 31 ];
    ret[ 0] = makeBtn( cat, key[ 0], R.string.pref_pt_color_map_title, R.string.pref_pt_color_map_summary, B, "", res, hlp );
    ret[ 1] = makeCbx( cat, key[ 1], R.string.pref_LRExtend_title, R.string.pref_LRExtend_summary, A, true, res, hlp );
    ret[ 2] = makeLst( cat, key[ 2], R.string.pref_export_shots_title, R.string.pref_export_shots_summary, B, "none", R.array.exportShots, R.array.exportShotsValue, res, hlp );
    ret[ 3] = makeLst( cat, key[ 3], R.string.pref_export_plot_title, R.string.pref_export_plot_summary, B, "none", R.array.exportPlot, R.array.exportPlotValue, res, hlp );
    ret[ 4] = makeCbx( cat, key[ 4], R.string.pref_therion_maps_title, R.string.pref_therion_maps_summary, A, false, res, hlp );
    ret[ 5] = makeCbx( cat, key[ 5], R.string.pref_autoStations_title, R.string.pref_autoStations_summary, N, true, res, hlp );
    // ret[] = makeCbx( cat, key[], R.string.pref_xtherion_areas_title, R.string.pref_xtherion_areas_summary, X, false, res, hlp );
    ret[ 6] = makeCbx( cat, key[ 6], R.string.pref_therion_splays_title, R.string.pref_therion_splays_summary, A, false, res, hlp );
    ret[ 7] = makeCbx( cat, key[ 7], R.string.pref_station_prefix_title, R.string.pref_station_prefix_summary, B, false, res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], R.string.pref_compass_splays_title, R.string.pref_compass_splays_summary, A, true, res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], R.string.pref_ortho_lrud_title, R.string.pref_ortho_lrud_summary, A, FLOAT, "0", res, hlp );
    ret[10] = makeEdt( cat, key[10], R.string.pref_lrud_vertical_title, R.string.pref_lrud_vertical_summary, A, FLOAT, "0", res, hlp );
    ret[11] = makeEdt( cat, key[11], R.string.pref_lrud_horizontal_title, R.string.pref_lrud_horizontal_summary, A, FLOAT, "90", res, hlp );
    ret[12] = makeCbx( cat, key[12], R.string.pref_swapLR_title, R.string.pref_swapLR_summary, N, false, res, hlp );
    ret[13] = makeLst( cat, key[13], R.string.pref_survex_eol_title, R.string.pref_survex_eol_summary, N, "lf", R.array.survexEol, R.array.survexEolValue, res, hlp );
    ret[14] = makeCbx( cat, key[14], R.string.pref_survex_splay_title, R.string.pref_survex_splay_summary, A, false, res, hlp );
    ret[15] = makeCbx( cat, key[15], R.string.pref_survex_lrud_title, R.string.pref_survex_lrud_summary, A, false, res, hlp );
    ret[16] = makeEdt( cat, key[16], R.string.pref_bezier_step_title, R.string.pref_bezier_step_summary, E, FLOAT, "0.2", res, hlp );
    ret[17] = makeCbx( cat, key[17], R.string.pref_svg_grid_title, R.string.pref_svg_grid_summary, N, false, res, hlp );
    ret[18] = makeCbx( cat, key[18], R.string.pref_svg_line_dir_title, R.string.pref_svg_line_dir_summary, E, false, res, hlp );
    // ret[] = makeCbx( cat,key[], R.string.pref_svg_in_html_title, R.string.pref_svg_in_html_summary, X, false, res, hlp );
    ret[19] = makeEdt( cat, key[19], R.string.pref_svg_pointstroke_title, R.string.pref_svg_pointstroke_summary, A, FLOAT, "0.1", res, hlp );
    ret[20] = makeEdt( cat, key[20], R.string.pref_svg_labelstroke_title, R.string.pref_svg_labelstroke_summary, A, FLOAT, "0.3", res, hlp );
    ret[21] = makeEdt( cat, key[21], R.string.pref_svg_linestroke_title, R.string.pref_svg_linestroke_summary, A, FLOAT, "0.5", res, hlp );
    ret[22] = makeEdt( cat, key[22], R.string.pref_svg_gridstroke_title, R.string.pref_svg_gridstroke_summary, A, FLOAT, "0.5", res, hlp );
    ret[23] = makeEdt( cat, key[23], R.string.pref_svg_shotstroke_title, R.string.pref_svg_shotstroke_summary, A, FLOAT, "0.5", res, hlp );
    ret[24] = makeEdt( cat, key[24], R.string.pref_svg_linedirstroke_title, R.string.pref_svg_linedirstroke_summary, A, FLOAT, "2.0", res, hlp );
    ret[25] = makeCbx( cat, key[25], R.string.pref_kml_stations_title, R.string.pref_kml_stations_summary, N, true, res, hlp );
    ret[26] = makeCbx( cat, key[26], R.string.pref_kml_splays_title, R.string.pref_kml_splays_summary, N, false, res, hlp );
    ret[27] = makeEdt( cat, key[27], R.string.pref_bitmap_scale_title, R.string.pref_bitmap_scale_summary, N, FLOAT, "1.5", res, hlp );
    ret[28] = makeEdt( cat, key[28], R.string.pref_bitmap_bgcolor_title, R.string.pref_bitmap_bgcolor_summary, N, STRING, "0 0 0", res, hlp );
    // ret[] = makeEdt( cat, key[], R.string.pref_dxf_scale_title, R.string.pref_dxf_scale_summary, X, FLOAT, "1.0", res, hlp );
    ret[29] = makeCbx( cat, key[29], R.string.pref_dxf_blocks_title, R.string.pref_dxf_blocks_summary, N, true, res, hlp );
    ret[30] = makeLst( cat, key[30], R.string.pref_acad_version_title, R.string.pref_acad_version_summary, E, "9", R.array.acadVersion, R.array.acadVersionValue, res, hlp );
    return ret;
  }

  static TDPref[] makeShotPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_SHOT_DATA;
    String[] key = TDPrefKey.DATA;
    TDPref[] ret = new TDPref[ 14 ];
    ret[ 0] = makeEdt( cat, key[ 0], R.string.pref_leg_title, R.string.pref_leg_summary, B, FLOAT, "0.05", res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], R.string.pref_max_shot_title, R.string.pref_max_shot_summary, B, FLOAT, "50", res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], R.string.pref_min_leg_title, R.string.pref_min_leg_summary, B, FLOAT, "0.5", res, hlp );
    ret[ 3] = makeLst( cat, key[ 3], R.string.pref_leg_shots_title, R.string.pref_leg_shots_summary, A, "3", R.array.legShots, R.array.legShotsValue, res, hlp );
    ret[ 4] = makeEdt( cat, key[ 4], R.string.pref_recent_timeout_title, R.string.pref_recent_timeout_summary, E, INTEGER, "30", res, hlp );
    ret[ 5] = makeCbx( cat, key[ 5], R.string.pref_backshot_title, R.string.pref_backshot_summary, E, false, res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], R.string.pref_ethr_title, R.string.pref_ethr_summary, N, FLOAT, "10", res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], R.string.pref_vthr_title, R.string.pref_vthr_summary, N, FLOAT, "80", res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], R.string.pref_azimuth_manual_title, R.string.pref_azimuth_manual_summary, N, false, res, hlp );
    ret[ 9] = makeLst( cat, key[ 9], R.string.pref_loopClosure_title, R.string.pref_loopClosure_summary, E, "0", R.array.loopClosure, R.array.loopClosureValue, res, hlp );
    ret[10] = makeCbx( cat, key[10], R.string.pref_prev_next_title, R.string.pref_prev_next_summary, A, true, res, hlp );
    ret[11] = makeCbx( cat, key[11], R.string.pref_backsight_title, R.string.pref_backsight_summary, A, false, res, hlp );
    // ret[] = makeCbx( cat, key[], R.string.pref_mag_anomaly_title, R.string.pref_mag_anomaly_summary, X, false, res, hlp );
    ret[12] = makeEdt( cat, key[12], R.string.pref_shot_timer_title, R.string.pref_shot_timer_summary, A, INTEGER, "10", res, hlp );
    ret[13] = makeEdt( cat, key[13], R.string.pref_beep_volume_title, R.string.pref_beep_volume_summary, A, INTEGER, "50", res, hlp );
    return ret;
  }

  static TDPref[] makeUnitsPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_SHOT_UNITS;
    String[] key = TDPrefKey.UNITS;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeLst( cat, key[0], R.string.pref_unit_length_title, R.string.pref_unit_length_summary, B, "meters", R.array.unitLength, R.array.unitLengthValue, res, hlp );
    ret[1] = makeLst( cat, key[1], R.string.pref_unit_angle_title,  R.string.pref_unit_angle_summary,  B, "degrees", R.array.unitAngle, R.array.unitAngleValue, res, hlp );
    ret[2] = makeLst( cat, key[2], R.string.pref_unit_grid_title,   R.string.pref_unit_grid_summary,   B, "1.0", R.array.unitGrid, R.array.unitGridValue, res, hlp );
    return ret;
  }

  static TDPref[] makeAccuracyPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_ACCURACY;
    String[] key = TDPrefKey.ACCURACY;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeEdt( cat, key[0], R.string.pref_accel_thr_title, R.string.pref_accel_thr_summary, A, FLOAT, "1.0", res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_mag_thr_title,   R.string.pref_mag_thr_summary,   A, FLOAT, "1.0", res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_dip_thr_title,   R.string.pref_dip_thr_summary,   A, FLOAT, "2.0", res, hlp );
    return ret;
  }

  static TDPref[] makeLocationPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_LOCATION;
    String[] key = TDPrefKey.LOCATION;
    TDPref[] ret = new TDPref[ 2 ];
    ret[0] = makeLst( cat, key[0], R.string.pref_unit_location_title, R.string.pref_unit_location_summary, N, "ddmmss", R.array.unitLocation, R.array.unitLocationValue, res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_crs_title, R.string.pref_crs_summary, A, STRING, "Long-Lat", res, hlp );
    return ret;
  }

  static TDPref[] makeScreenPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_SCREEN;
    String[] key = TDPrefKey.SCREEN;
    TDPref[] ret = new TDPref[ 13 ];
    ret[ 0] = makeEdt( cat, key[ 0], R.string.pref_fixed_thickness_title, R.string.pref_fixed_thickness_summary, B, FLOAT, "1", res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], R.string.pref_station_size_title, R.string.pref_station_size_summary, B, FLOAT, "20", res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], R.string.pref_dot_radius_title, R.string.pref_dot_radius_message, N, FLOAT, "5", res, hlp );
    ret[ 3] = makeEdt( cat, key[ 3], R.string.pref_closeness_title, R.string.pref_closeness_message, B, INTEGER, "24", res, hlp );
    ret[ 4] = makeEdt( cat, key[ 4], R.string.pref_eraseness_title, R.string.pref_eraseness_message, B, INTEGER, "36", res, hlp );
    ret[ 5] = makeEdt( cat, key[ 5], R.string.pref_min_shift_title, R.string.pref_min_shift_message, E, INTEGER, "60", res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], R.string.pref_pointing_title, R.string.pref_pointing_message, E, INTEGER, "24", res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], R.string.pref_vthr_title, R.string.pref_vthr_summary, A, INTEGER, "80", res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], R.string.pref_dash_splay_title, R.string.pref_dash_splay_message, A, true, res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], R.string.pref_vert_splay_title, R.string.pref_vert_splay_message, A, FLOAT, "50", res, hlp );
    ret[10] = makeEdt( cat, key[10], R.string.pref_horiz_splay_title, R.string.pref_horiz_splay_message, A, FLOAT, "60", res, hlp );
    ret[11] = makeEdt( cat, key[11], R.string.pref_section_splay_title, R.string.pref_section_splay_message, A, FLOAT, "60", res, hlp );
    ret[12] = makeEdt( cat, key[12], R.string.pref_hthr_title, R.string.pref_hthr_summary, A, FLOAT, "70", res, hlp );
    return ret;
  }

  static TDPref[] makeLinePrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_TOOL_LINE;
    String[] key = TDPrefKey.LINE;
    TDPref[] ret = new TDPref[ 10 ];
    ret[0] = makeEdt( cat, key[0], R.string.pref_line_thickness_title, R.string.pref_line_thickness_summary, N, FLOAT, "1", res, hlp );
    ret[1] = makeLst( cat, key[1], R.string.pref_linestyle_title, R.string.pref_linestyle_summary, N, "2", R.array.lineStyle, R.array.lineStyleValue, res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_segment_title, R.string.pref_segment_message, N, INTEGER, "10", res, hlp );
    ret[3] = makeEdt( cat, key[3], R.string.pref_arrow_length_title, R.string.pref_arrow_length_message, A, FLOAT, "8", res, hlp );
    ret[4] = makeEdt( cat, key[4], R.string.pref_reduce_angle_title, R.string.pref_reduce_angle_summary, A, FLOAT, "45", res, hlp );
    ret[5] = makeCbx( cat, key[5], R.string.pref_auto_section_pt_title, R.string.pref_auto_section_pt_summary, A, false, res, hlp );
    ret[6] = makeLst( cat, key[6], R.string.pref_linecontinue_title, R.string.pref_linecontinue_summary, E, "0", R.array.lineContinue, R.array.lineContinueValue, res, hlp );
    ret[7] = makeCbx( cat, key[7], R.string.pref_area_border_title, R.string.pref_area_border_summary, N, true, res, hlp );
    ret[8] = makeEdt( cat, key[8], R.string.pref_lineacc_title, R.string.pref_lineacc_summary, N, FLOAT, "1.0", res, hlp );
    ret[9] = makeEdt( cat, key[9], R.string.pref_linecorner_title, R.string.pref_linecorner_summary, N, FLOAT, "20.0", res, hlp );
    return ret;
  }

  static TDPref[] makePointPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_TOOL_POINT;
    String[] key = TDPrefKey.POINT;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeCbx( cat, key[0], R.string.pref_unscaled_points_title, R.string.pref_unscaled_points_summary, N, false, res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_drawing_unit_title, R.string.pref_drawing_unit_summary, B, FLOAT, "1.2", res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_label_size_title, R.string.pref_label_size_summary, B, INTEGER, "24", res, hlp );
    return ret;
  }

  static TDPref[] makeWallsPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_WALLS;
    String[] key = TDPrefKey.WALLS;
    TDPref[] ret = new TDPref[ 6 ];
    ret[0] = makeLst( cat, key[0], R.string.pref_walls_type_title, R.string.pref_walls_type_summary, T, "0", R.array.wallsType, R.array.wallsTypeValue, res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_walls_plan_thr_title, R.string.pref_walls_plan_thr_summary, T, INTEGER, "70", res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_walls_extended_thr_title, R.string.pref_walls_extended_thr_summary, T, INTEGER, "45", res, hlp );
    ret[3] = makeEdt( cat, key[3], R.string.pref_walls_xclose_title, R.string.pref_walls_xclose_summary, T, FLOAT, "0.1", res, hlp );
    ret[4] = makeEdt( cat, key[4], R.string.pref_walls_concave_title, R.string.pref_walls_concave_summary, T, FLOAT, "0.1", res, hlp );
    ret[5] = makeEdt( cat, key[5], R.string.pref_walls_xstep_title, R.string.pref_walls_xstep_summary, T, FLOAT, "1.0", res, hlp );
    return ret;
  }

  static TDPref[] makeDrawPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_DRAW;
    String[] key = TDPrefKey.DRAW;
    TDPref[] ret = new TDPref[ 13 ];
    ret[ 0] = makeCbx( cat, key[ 0], R.string.pref_unscaled_points_title, R.string.pref_unscaled_points_summary, N, false, res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], R.string.pref_drawing_unit_title, R.string.pref_drawing_unit_summary, B, FLOAT, "1.2", res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], R.string.pref_label_size_title, R.string.pref_label_size_summary, B, INTEGER, "24", res, hlp );
    ret[ 3] = makeEdt( cat, key[ 3], R.string.pref_line_thickness_title, R.string.pref_line_thickness_summary, N, FLOAT, "1", res, hlp );
    ret[ 4] = makeLst( cat, key[ 4], R.string.pref_linestyle_title, R.string.pref_linestyle_summary, N, "2", R.array.lineStyle, R.array.lineStyleValue, res, hlp );
    ret[ 5] = makeEdt( cat, key[ 5], R.string.pref_segment_title, R.string.pref_segment_message, N, INTEGER, "10", res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], R.string.pref_arrow_length_title, R.string.pref_arrow_length_message, A, FLOAT, "8", res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], R.string.pref_reduce_angle_title, R.string.pref_reduce_angle_summary, A, FLOAT, "45", res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], R.string.pref_auto_section_pt_title, R.string.pref_auto_section_pt_summary, A, false, res, hlp );
    ret[ 9] = makeLst( cat, key[ 9], R.string.pref_linecontinue_title, R.string.pref_linecontinue_summary, E, "0", R.array.lineContinue, R.array.lineContinueValue, res, hlp );
    ret[10] = makeCbx( cat, key[10], R.string.pref_area_border_title, R.string.pref_area_border_summary, N, true, res, hlp );
    ret[11] = makeEdt( cat, key[11], R.string.pref_lineacc_title, R.string.pref_lineacc_summary, N, FLOAT, "1.0", res, hlp );
    ret[12] = makeEdt( cat, key[12], R.string.pref_linecorner_title, R.string.pref_linecorner_summary, N, FLOAT, "20.0", res, hlp );
    return ret;
  }

  static TDPref[] makeErasePrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_ERASE;
    String[] key = TDPrefKey.ERASE;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeEdt( cat, key[0], R.string.pref_closeness_title, R.string.pref_closeness_message, B, INTEGER, "24", res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_eraseness_title, R.string.pref_eraseness_message, B, INTEGER, "36", res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_pointing_title,  R.string.pref_pointing_message,  E, INTEGER, "24", res, hlp );
    return ret;
  }

  static TDPref[] makeEditPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_EDIT;
    String[] key = TDPrefKey.EDIT;
    TDPref[] ret = new TDPref[ 4 ];
    ret[0] = makeEdt( cat, key[0], R.string.pref_dot_radius_title, R.string.pref_dot_radius_message, N, FLOAT,   "5",  res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_closeness_title,  R.string.pref_closeness_message,  B, INTEGER, "24", res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_min_shift_title,  R.string.pref_min_shift_message,  E, INTEGER, "60", res, hlp );
    ret[3] = makeEdt( cat, key[3], R.string.pref_pointing_title,   R.string.pref_pointing_message,   E, INTEGER, "24", res, hlp );
    return ret;
  }

  static TDPref[] makeSketchPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SKETCH;
    String[] key = TDPrefKey.SKETCH;
    TDPref[] ret = new TDPref[ 3 ];
    // ret[] = makeCbx( cat, key[], R.string.pref_sketchUsesSplays_title, R.string.pref_sketchUsesSplays_summary, X, false, res, hlp );
    ret[0] = makeLst( cat, key[0], R.string.pref_sketchModelType_title, R.string.pref_sketchModelType_summary, D, "0", R.array.modelType, R.array.modelTypeValue, res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_sketchLineStep_title, R.string.pref_sketchLineStep_summary, D, FLOAT, "0.5", res, hlp );
    // ret[] = makeEdt( cat, key[], R.string.pref_sketchBorderStep_title, R.string.pref_sketchBorderStep_summary, X, FLOAT, "0.2", res, hlp );
    // ret[] = makeEdt( cat, key[], R.string.pref_sketchSectionStep_title, R.string.pref_sketchSectionStep_summary, X, FLOAT, "0.5", res, hlp );
    ret[2] = makeEdt( cat, key[2], R.string.pref_sketchDeltaExtrude_title, R.string.pref_sketchDeltaExtrude_summary, D, FLOAT, "50", res, hlp );
    // ret[3] = makeEdt( cat, key[3], R.string.pref_sketchCompassReadings, R.string.pref_sketchCompassReadings, X, INTEGER, "4", res, hlp );
    return ret;
  }

  static TDPref[] makeLogPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_LOG;
    String[] key = TDPrefKey.LOG;
    TDPref[] ret = new TDPref[ 31 ];
    ret[0] = makeLst( cat, key[0],
               R.string.pref_log_stream_title, R.string.pref_log_stream_summary, T, "0", R.array.logStream, R.array.logStreamValue, res, hlp );
    ret[1]  = makeCbx( cat, key[ 1], R.string.pref_log_append,  -1, T, false, res, hlp );
    ret[2]  = makeCbx( cat, key[ 2], R.string.pref_log_debug,   -1, T, false, res, hlp );
    ret[3]  = makeCbx( cat, key[ 3],   R.string.pref_log_err,   -1, T, true,  res, hlp );
    ret[4]  = makeCbx( cat, key[ 4],  R.string.pref_log_perm,   -1, T, false, res, hlp );
    ret[5]  = makeCbx( cat, key[ 5], R.string.pref_log_input,   -1, T, false, res, hlp );
    ret[6]  = makeCbx( cat, key[ 6],  R.string.pref_log_path,   -1, T, false, res, hlp );
    ret[7]  = makeCbx( cat, key[ 7],    R.string.pref_log_io,   -1, T, false, res, hlp );
    ret[8]  = makeCbx( cat, key[ 8],    R.string.pref_log_bt,   -1, T, false, res, hlp );
    ret[9]  = makeCbx( cat, key[ 9],  R.string.pref_log_comm,   -1, T, false, res, hlp );
    ret[10] = makeCbx( cat, key[10], R.string.pref_log_distox,  -1, T, false, res, hlp );
    ret[11] = makeCbx( cat, key[11],  R.string.pref_log_proto,  -1, T, false, res, hlp );
    ret[12] = makeCbx( cat, key[12], R.string.pref_log_device,  -1, T, false, res, hlp );
    ret[13] = makeCbx( cat, key[13],  R.string.pref_log_calib,  -1, T, false, res, hlp );
    ret[14] = makeCbx( cat, key[14],     R.string.pref_log_db,  -1, T, false, res, hlp );
    ret[15] = makeCbx( cat, key[15],  R.string.pref_log_units,  -1, T, false, res, hlp );
    ret[16] = makeCbx( cat, key[16],   R.string.pref_log_data,  -1, T, false, res, hlp );
    ret[17] = makeCbx( cat, key[17],   R.string.pref_log_shot,  -1, T, false, res, hlp );
    ret[18] = makeCbx( cat, key[18], R.string.pref_log_survey,  -1, T, false, res, hlp );
    ret[19] = makeCbx( cat, key[19],    R.string.pref_log_num,  -1, T, false, res, hlp );
    ret[20] = makeCbx( cat, key[20],  R.string.pref_log_fixed,  -1, T, false, res, hlp );
    ret[21] = makeCbx( cat, key[21],    R.string.pref_log_loc,  -1, T, false, res, hlp );
    ret[22] = makeCbx( cat, key[22],  R.string.pref_log_photo,  -1, T, false, res, hlp );
    ret[23] = makeCbx( cat, key[23], R.string.pref_log_sensor,  -1, T, false, res, hlp );
    ret[24] = makeCbx( cat, key[24],   R.string.pref_log_plot,  -1, T, false, res, hlp );
    ret[25] = makeCbx( cat, key[25], R.string.pref_log_bezier,  -1, T, false, res, hlp );
    ret[26] = makeCbx( cat, key[26], R.string.pref_log_therion, -1, T, false, res, hlp );
    ret[27] = makeCbx( cat, key[27], R.string.pref_log_csurvey, -1, T, false, res, hlp );
    ret[28] = makeCbx( cat, key[28],   R.string.pref_log_ptopo, -1, T, false, res, hlp );
    ret[29] = makeCbx( cat, key[29],     R.string.pref_log_zip, -1, T, false, res, hlp );
    ret[30] = makeCbx( cat, key[30],    R.string.pref_log_sync, -1, T, false, res, hlp );

    return ret;
  }

}
