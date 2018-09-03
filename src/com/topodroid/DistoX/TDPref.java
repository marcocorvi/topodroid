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

  static private TDPref makeCbx( int cat, String nm, int tit, int sum, int lvl, String def_str, Resources res, TDPrefHelper hlp )
  { 
    boolean val = hlp.getBoolean( nm, def_str.startsWith("t") );
    TDPref ret = new TDPref( cat, nm, CHECKBOX, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), res, hlp );
    ret.bvalue = val;
    return ret;
  }

  static private TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, String def_val, int pt, Resources res, TDPrefHelper hlp )
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

  // static private TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, int idef, int pt, Resources res, TDPrefHelper hlp)
  // { 
  //   String val = hlp.getString( nm, res.getString(idef) );
  //   TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, res, hlp );
  //   // Log.v("DistoX", "EditText value " + ret.value );
  //   if ( pt == INTEGER ) {
  //     ret.ivalue = Integer.parseInt( ret.value );
  //   } else if ( pt == FLOAT ) {
  //     ret.fvalue = Float.parseFloat( ret.value );
  //   }
  //   return ret;
  // }

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
    int[] tit = TDPrefKey.MAINtitle;
    int[] dsc = TDPrefKey.MAINdesc;
    String[] def = TDPrefKey.MAINdef;
    TDPref[] ret = new TDPref[ 14 ];
    ret[ 0] = makeBtn(     cat, key[0],  tit[0],   dsc[0],  N, def[0],  res, hlp );
    ret[ 1] = makeEdt(     cat, key[1],  tit[1],   dsc[1],  B, def[1],  INTEGER, res, hlp );
    ret[ 2] = makeLst(     cat, key[2],  tit[2],   dsc[2],  B, def[2],  R.array.sizeButtons, R.array.sizeButtonsValue, res, hlp );
    ret[ 3] = makeLst(     cat, key[3],  tit[3],   dsc[3],  B, def[3],  R.array.extraButtons, R.array.extraButtonsValue, res, hlp );
    ret[ 4] = makeCbx(     cat, key[4],  tit[4],   dsc[4],  B, def[4],  res, hlp );
    ret[ 5] = makeCbx(     cat, key[5],  tit[5],   dsc[5],  T, def[5],  res, hlp );
    ret[ 6] = makeLst(     cat, key[6],  tit[6],   dsc[6],  A, def[6],  R.array.localUserMan, R.array.localUserManValue, res, hlp );
    ret[ 7] = makeCbx(     cat, key[7],  tit[7],   dsc[7],  D, def[7],  res, hlp );
    ret[ 8] = makeLst(     cat, key[8],  tit[8],   dsc[8],  N, def[8],  R.array.locale, R.array.localeValue, res, hlp );
    ret[ 9] = makeForward( cat, key[ 9], tit[ 9],           B,         res, hlp );
    ret[10] = makeForward( cat, key[10], tit[10],           B,         res, hlp );
    ret[11] = makeForward( cat, key[11], tit[11],           B,         res, hlp );
    ret[12] = makeForward( cat, key[12], tit[12],           D,         res, hlp );
    ret[13] = makeForward( cat, key[13], tit[13],           B,         res, hlp );
    return ret;
  }

  static TDPref[] makeSurveyPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SURVEY;
    String[] key = TDPrefKey.SURVEY;
    int[] tit = TDPrefKey.SURVEYtitle;
    int[] dsc = TDPrefKey.SURVEYdesc;
    String[] def = TDPrefKey.SURVEYdef;
    TDPref[] ret = new TDPref[ 11 ];
    ret[ 0] = makeEdt(     cat, key[0],  tit[0],   dsc[0], B, def[0], STRING, res, hlp );
    ret[ 1] = makeLst(     cat, key[1],  tit[1],   dsc[1], B, def[1], R.array.surveyStations, R.array.surveyStationsValue, res, hlp );
    ret[ 2] = makeLst(     cat, key[2],  tit[2],   dsc[2], B, def[2], R.array.stationNames, R.array.stationNamesValue, res, hlp );
    ret[ 3] = makeEdt(     cat, key[3],  tit[3],   dsc[3], B, def[3], STRING, res, hlp );
    ret[ 4] = makeEdt(     cat, key[4],  tit[4],   dsc[4], A, def[4], INTEGER, res, hlp );
    ret[ 5] = makeCbx(     cat, key[5],  tit[5],   dsc[5], B, def[5], res, hlp );
    ret[ 6] = makeCbx(     cat, key[6],  tit[6],   dsc[6], B, def[6], res, hlp );
    ret[ 7] = makeForward( cat, key[ 7], tit[7],           B,         res, hlp );
    ret[ 8] = makeForward( cat, key[ 8], tit[8],           B,         res, hlp );
    ret[ 9] = makeForward( cat, key[ 9], tit[ 9],          N,         res, hlp );
    ret[10] = makeForward( cat, key[10], tit[10],          A,         res, hlp );
    return ret;
  }

  static TDPref[] makePlotPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_PLOT;
    String[] key = TDPrefKey.PLOT;
    int[] tit = TDPrefKey.PLOTtitle;
    int[] dsc = TDPrefKey.PLOTdesc;
    String[] def = TDPrefKey.PLOTdef;
    TDPref[] ret = new TDPref[ 12 ];
    ret[0] = makeLst(      cat, key[0],  tit[0],  dsc[0], B, def[0], R.array.pickerType, R.array.pickerTypeValue, res, hlp );
    ret[1] = makeLst(      cat, key[1],  tit[1],  dsc[1], N, def[1], R.array.recentNr, R.array.recentNr, res, hlp );
    ret[2] = makeCbx(      cat, key[2],  tit[2],  dsc[2], B, def[2], res, hlp );
    ret[3] = makeLst(      cat, key[3],  tit[3],  dsc[3], B, def[3], R.array.zoomCtrl, R.array.zoomCtrlValue, res, hlp );
    // ret[] = makeLst(    cat, key[],   tit[ ],  dsc[ ], X, def[ ], R.array.sectionStations, R.array.sectionStationsValue, res, hlp );
    ret[ 4] = makeCbx(     cat, key[4],  tit[4],  dsc[4], A, def[4], res, hlp );
    ret[ 5] = makeCbx(     cat, key[5],  tit[5],  dsc[5], A, def[5], res, hlp );
    ret[ 6] = makeLst(     cat, key[6],  tit[6],  dsc[6], A, def[6], R.array.backupNumber, R.array.backupNumberValue, res, hlp );
    ret[ 7] = makeEdt(     cat, key[ 7], tit[7],  dsc[7], A, def[7], INTEGER, res, hlp );
    ret[ 8] = makeForward( cat, key[ 8], tit[ 8],         B,         res, hlp );
    ret[ 9] = makeForward( cat, key[ 9], tit[ 9],         N,         res, hlp );
    ret[10] = makeForward( cat, key[10], tit[10],         B,         res, hlp );
    ret[11] = makeForward( cat, key[11], tit[11],         T,         res, hlp );
    return ret;
  }

  static TDPref[] makeCalibPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_CALIB;
    String[] key = TDPrefKey.CALIB;
    int[] tit = TDPrefKey.CALIBtitle;
    int[] dsc = TDPrefKey.CALIBdesc;
    String[] def = TDPrefKey.CALIBdef;
    TDPref[] ret = new TDPref[ 11 ];
    ret[ 0] = makeLst( cat, key[ 0], tit[0],  dsc[0],  A, def[0],  R.array.groupBy, R.array.groupByValue, res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], tit[1],  dsc[1],  A, def[1],  FLOAT, res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], tit[2],  dsc[2],  B, def[2],  FLOAT, res, hlp );
    ret[ 3] = makeEdt( cat, key[ 3], tit[3],  dsc[3],  B, def[3],  INTEGER, res, hlp );
    ret[ 4] = makeCbx( cat, key[ 4], tit[4],  dsc[4],  A, def[4],  res, hlp );
    // ret[] = makeCbx( cat, key[],  tit[ ],  dsc[ ],  X, def[ ],  res, hlp );
    ret[ 5] = makeLst( cat, key[ 5], tit[5],  dsc[5],  A, def[5],  R.array.rawCData, R.array.rawCDataValue, res, hlp );
    ret[ 6] = makeLst( cat, key[ 6], tit[6],  dsc[6],  E, def[6],  R.array.calibAlgo, R.array.calibAlgoValue, res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], tit[7],  dsc[7],  D, def[7],  FLOAT, res, hlp );
    ret[ 8] = makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], D, def[ 8], FLOAT, res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], D, def[ 9], FLOAT, res, hlp );
    ret[10] = makeEdt( cat, key[10], tit[10], dsc[10], D, def[10], FLOAT, res, hlp );
    return ret; 
  }

  static TDPref[] makeDevicePrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_DEVICE;
    String[] key = TDPrefKey.DEVICE;
    int[] tit = TDPrefKey.DEVICEtitle;
    int[] dsc = TDPrefKey.DEVICEdesc;
    String[] def = TDPrefKey.DEVICEdef;
    TDPref[] ret = new TDPref[ 13 ];
    // ret[] = makeEdt( cat, key[],  tit[  ], dsc[  ], X, def[  ], STRING, res, hlp );
    // ret[] = makeLst( cat, key[],  tit[  ], dsc[  ], X, def[  ], R.array.deviceType, R.array.deviceTypeValue, res, hlp );
    ret[ 0] = makeLst( cat, key[ 0], tit[0],  dsc[0],  N, def[0],  R.array.deviceBT, R.array.deviceBTValue, res, hlp );
    ret[ 1] = makeLst( cat, key[ 1], tit[1],  dsc[1],  B, def[1],  R.array.connMode, R.array.connModeValue, res, hlp );
    ret[ 2] = makeCbx( cat, key[ 2], tit[2],  dsc[2],  B, def[2],  res, hlp );
    ret[ 3] = makeCbx( cat, key[ 3], tit[3],  dsc[3],  B, def[3],  res, hlp );
    ret[ 4] = makeLst( cat, key[ 4], tit[4],  dsc[4],  B, def[4],  R.array.sockType, R.array.sockTypeValue, res, hlp );
    // ret[ 5] = makeEdt( cat, key[],tit[  ], dsc[  ], X, def[  ], INTEGER, res, hlp );
    ret[ 5] = makeCbx( cat, key[ 5], tit[5],  dsc[5],  N, def[5],  res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], tit[6],  dsc[6],  E, def[6],  INTEGER, res, hlp );
    ret[ 7] = makeCbx( cat, key[ 7], tit[7],  dsc[7],  A, def[7],  res, hlp );
    ret[ 8] = makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], A, def[ 8], INTEGER, res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9], INTEGER, res, hlp );
    ret[10] = makeEdt( cat, key[10], tit[10], dsc[10], A, def[10], INTEGER, res, hlp );
    ret[11] = makeEdt( cat, key[11], tit[11], dsc[11], A, def[11], INTEGER, res, hlp );
    ret[12] = makeForward( cat, key[12], tit[12],      B,         res, hlp );
    return ret;
  }

  static TDPref[] makeExportPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_EXPORT;
    String[] key = TDPrefKey.EXPORT;
    int[] tit = TDPrefKey.EXPORTtitle;
    int[] dsc = TDPrefKey.EXPORTdesc;
    String[] def = TDPrefKey.EXPORTdef;
    TDPref[] ret = new TDPref[ 31 ];
    ret[ 0] = makeBtn( cat, key[ 0], tit[0],  dsc[0],  B, def[0],          res, hlp );
    ret[ 1] = makeCbx( cat, key[ 1], tit[1],  dsc[1],  A, def[1],          res, hlp );
    ret[ 2] = makeLst( cat, key[ 2], tit[2],  dsc[2],  B, def[2],  R.array.exportShots, R.array.exportShotsValue, res, hlp );
    ret[ 3] = makeLst( cat, key[ 3], tit[3],  dsc[3],  B, def[3],  R.array.exportPlot, R.array.exportPlotValue, res, hlp );
    ret[ 4] = makeCbx( cat, key[ 4], tit[4],  dsc[4],  A, def[4],          res, hlp );
    ret[ 5] = makeCbx( cat, key[ 5], tit[5],  dsc[5],  N, def[5],          res, hlp );
    // ret[] = makeCbx( cat, key[],  tit[ ],  dsc[ ],  X, def[ ],          res, hlp );
    ret[ 6] = makeCbx( cat, key[ 6], tit[7],  dsc[6],  A, def[6],          res, hlp );
    ret[ 7] = makeCbx( cat, key[ 7], tit[7],  dsc[7],  B, def[7],          res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], tit[8],  dsc[8],  A, def[8],          res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9], FLOAT,  res, hlp );
    ret[10] = makeEdt( cat, key[10], tit[10], dsc[10], A, def[10], FLOAT,  res, hlp );
    ret[11] = makeEdt( cat, key[11], tit[11], dsc[11], A, def[11], FLOAT,  res, hlp );
    ret[12] = makeCbx( cat, key[12], tit[12], dsc[12], N, def[12],         res, hlp );
    ret[13] = makeLst( cat, key[13], tit[13], dsc[13], N, def[13], R.array.survexEol, R.array.survexEolValue, res, hlp );
    ret[14] = makeCbx( cat, key[14], tit[14], dsc[14], A, def[14],         res, hlp );
    ret[15] = makeCbx( cat, key[15], tit[15], dsc[15], A, def[15],         res, hlp );
    ret[16] = makeEdt( cat, key[16], tit[16], dsc[16], E, def[16], FLOAT,  res, hlp );
    ret[17] = makeCbx( cat, key[17], tit[17], dsc[17], N, def[17],         res, hlp );
    ret[18] = makeCbx( cat, key[18], tit[18], dsc[18], E, def[18],         res, hlp );
    // ret[] = makeCbx( cat,key[],   tit[  ], dsc[  ], X, def[  ],         res, hlp );
    ret[19] = makeEdt( cat, key[19], tit[19], dsc[19], A, def[19], FLOAT,  res, hlp );
    ret[20] = makeEdt( cat, key[20], tit[20], dsc[20], A, def[20], FLOAT,  res, hlp );
    ret[21] = makeEdt( cat, key[21], tit[21], dsc[21], A, def[21], FLOAT,  res, hlp );
    ret[22] = makeEdt( cat, key[22], tit[22], dsc[22], A, def[22], FLOAT,  res, hlp );
    ret[23] = makeEdt( cat, key[23], tit[23], dsc[23], A, def[23], FLOAT,  res, hlp );
    ret[24] = makeEdt( cat, key[24], tit[24], dsc[24], A, def[24], FLOAT,  res, hlp );
    ret[25] = makeCbx( cat, key[25], tit[25], dsc[25], N, def[25],         res, hlp );
    ret[26] = makeCbx( cat, key[26], tit[26], dsc[26], N, def[26],         res, hlp );
    ret[27] = makeEdt( cat, key[27], tit[27], dsc[27], N, def[27], FLOAT,  res, hlp );
    ret[28] = makeEdt( cat, key[28], tit[28], dsc[28], N, def[28], STRING, res, hlp );
    // ret[] = makeEdt( cat, key[],  tit[  ], dsc[  ], X, def[  ], FLOAT,  res, hlp );
    ret[29] = makeCbx( cat, key[29], tit[29], dsc[29], N, def[29],         res, hlp );
    ret[30] = makeLst( cat, key[30], tit[30], dsc[30], E, def[30], R.array.acadVersion, R.array.acadVersionValue, res, hlp );
    return ret;
  }

  static TDPref[] makeShotPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_SHOT_DATA;
    String[] key = TDPrefKey.DATA;
    int[] tit = TDPrefKey.DATAtitle;
    int[] dsc = TDPrefKey.DATAdesc;
    String[] def = TDPrefKey.DATAdef;
    TDPref[] ret = new TDPref[ 14 ];
    ret[ 0] = makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], B, def[ 0], FLOAT,   res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], FLOAT,   res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2], FLOAT,   res, hlp );
    ret[ 3] = makeLst( cat, key[ 3], tit[ 3], dsc[ 3], A, def[ 3], R.array.legShots, R.array.legShotsValue, res, hlp );
    ret[ 4] = makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], E, def[ 4], INTEGER, res, hlp );
    ret[ 5] = makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], E, def[ 5],          res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], N, def[ 6], FLOAT,   res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], N, def[ 7], FLOAT,   res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], N, def[ 8],          res, hlp );
    ret[ 9] = makeLst( cat, key[ 9], tit[ 9], dsc[ 9], E, def[ 9], R.array.loopClosure, R.array.loopClosureValue, res, hlp );
    ret[10] = makeCbx( cat, key[10], tit[10], dsc[10], A, def[10],          res, hlp );
    ret[11] = makeCbx( cat, key[11], tit[11], dsc[11], A, def[11],          res, hlp );
    // ret[] = makeCbx( cat, key[],  tit[  ], dsc[  ], X, def[  ],          res, hlp );
    ret[12] = makeEdt( cat, key[12], tit[12], dsc[12], A, def[12], INTEGER, res, hlp );
    ret[13] = makeEdt( cat, key[13], tit[13], dsc[13], A, def[13], INTEGER, res, hlp );
    return ret;
  }

  static TDPref[] makeUnitsPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_SHOT_UNITS;
    String[] key = TDPrefKey.UNITS;
    int[] tit = TDPrefKey.UNITStitle;
    int[] dsc = TDPrefKey.UNITSdesc;
    String[] def = TDPrefKey.UNITSdef;
    int[] arr = TDPrefKey.UNITSarr;
    int[] val = TDPrefKey.UNITSval;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeLst( cat, key[0], tit[0], dsc[0], B, def[0], arr[0], val[0], res, hlp );
    ret[1] = makeLst( cat, key[1], tit[1], dsc[1], B, def[1], arr[1], val[1], res, hlp );
    ret[2] = makeLst( cat, key[2], tit[2], dsc[2], B, def[2], arr[2], val[2], res, hlp );
    return ret;
  }

  static TDPref[] makeAccuracyPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_ACCURACY;
    String[] key = TDPrefKey.ACCURACY;
    int[] tit = TDPrefKey.ACCURACYtitle;
    int[] dsc = TDPrefKey.ACCURACYdesc;
    String[] def = TDPrefKey.ACCURACYdef;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeEdt( cat, key[0], tit[0], dsc[0], A, def[0], FLOAT, res, hlp );
    ret[1] = makeEdt( cat, key[1], tit[1], dsc[1], A, def[1], FLOAT, res, hlp );
    ret[2] = makeEdt( cat, key[2], tit[2], dsc[2], A, def[2], FLOAT, res, hlp );
    return ret;
  }

  static TDPref[] makeLocationPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_LOCATION;
    String[] key = TDPrefKey.LOCATION;
    int[] tit = TDPrefKey.LOCATIONtitle;
    int[] dsc = TDPrefKey.LOCATIONdesc;
    String[] def = TDPrefKey.LOCATIONdef;
    TDPref[] ret = new TDPref[ 2 ];
    ret[0] = makeLst( cat, key[0], R.string.pref_unit_location_title, R.string.pref_unit_location_summary, N, "ddmmss", R.array.unitLocation, R.array.unitLocationValue, res, hlp );
    ret[1] = makeEdt( cat, key[1], R.string.pref_crs_title, R.string.pref_crs_summary, A, "Long-Lat", STRING, res, hlp );
    return ret;
  }

  static TDPref[] makeScreenPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_SCREEN;
    String[] key = TDPrefKey.SCREEN;
    int[] tit = TDPrefKey.SCREENtitle;
    int[] dsc = TDPrefKey.SCREENdesc;
    String[] def = TDPrefKey.SCREENdef;
    TDPref[] ret = new TDPref[ 13 ];
    ret[ 0] = makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], B, def[ 0], FLOAT,   res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], FLOAT,   res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], N, def[ 2], FLOAT,   res, hlp );
    ret[ 3] = makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], B, def[ 3], INTEGER, res, hlp );
    ret[ 4] = makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], B, def[ 4], INTEGER, res, hlp );
    ret[ 5] = makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], E, def[ 5], INTEGER, res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], E, def[ 6], INTEGER, res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], A, def[ 7], INTEGER, res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], A, def[ 8],          res, hlp );
    ret[ 9] = makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9], FLOAT,   res, hlp );
    ret[10] = makeEdt( cat, key[10], tit[10], dsc[10], A, def[10], FLOAT,   res, hlp );
    ret[11] = makeEdt( cat, key[11], tit[11], dsc[11], A, def[11], FLOAT,   res, hlp );
    ret[12] = makeEdt( cat, key[12], tit[12], dsc[12], A, def[12], FLOAT,   res, hlp );
    return ret;
  }

  static TDPref[] makeLinePrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_TOOL_LINE;
    String[] key = TDPrefKey.LINE;
    int[] tit    = TDPrefKey.LINEtitle;
    int[] dsc    = TDPrefKey.LINEdesc;
    String[] def = TDPrefKey.LINEdef;
    TDPref[] ret = new TDPref[ 10 ];
    ret[0] = makeEdt( cat, key[0], tit[ 0], dsc[ 0], N, def[ 0], FLOAT,   res, hlp );
    ret[1] = makeLst( cat, key[1], tit[ 1], dsc[ 1], N, def[ 1], R.array.lineStyle, R.array.lineStyleValue, res, hlp );
    ret[2] = makeEdt( cat, key[2], tit[ 2], dsc[ 2], N, def[ 2], INTEGER, res, hlp );
    ret[3] = makeEdt( cat, key[3], tit[ 3], dsc[ 3], A, def[ 3], FLOAT,   res, hlp );
    ret[4] = makeEdt( cat, key[4], tit[ 4], dsc[ 4], A, def[ 4], FLOAT,   res, hlp );
    ret[5] = makeCbx( cat, key[5], tit[ 5], dsc[ 5], A, def[ 5],          res, hlp );
    ret[6] = makeLst( cat, key[6], tit[ 6], dsc[ 6], E, def[ 6], R.array.lineContinue, R.array.lineContinueValue, res, hlp );
    ret[7] = makeCbx( cat, key[7], tit[ 7], dsc[ 7], N, def[ 7],          res, hlp );
    ret[8] = makeEdt( cat, key[8], tit[ 8], dsc[ 8], N, def[ 8], FLOAT,   res, hlp );
    ret[9] = makeEdt( cat, key[9], tit[ 9], dsc[ 9], N, def[ 9], FLOAT,   res, hlp );
    return ret;
  }

  static TDPref[] makePointPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_TOOL_POINT;
    String[] key = TDPrefKey.POINT;
    int[] tit    = TDPrefKey.POINTtitle;
    int[] dsc    = TDPrefKey.POINTdesc;
    String[] def = TDPrefKey.POINTdef;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeCbx( cat, key[0], tit[0], dsc[0], N, def[0],          res, hlp );
    ret[1] = makeEdt( cat, key[1], tit[1], dsc[1], B, def[1], FLOAT,   res, hlp );
    ret[2] = makeEdt( cat, key[2], tit[2], dsc[2], B, def[2], INTEGER, res, hlp );
    return ret;
  }

  static TDPref[] makeWallsPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_WALLS;
    String[] key = TDPrefKey.WALLS;
    int[] tit    = TDPrefKey.WALLStitle;
    int[] dsc    = TDPrefKey.WALLSdesc;
    String[] def = TDPrefKey.WALLSdef;
    TDPref[] ret = new TDPref[ 6 ];
    ret[0] = makeLst( cat, key[0], tit[0],  dsc[0], T, def[0], R.array.wallsType, R.array.wallsTypeValue, res, hlp );
    ret[1] = makeEdt( cat, key[1], tit[1],  dsc[1], T, def[1], INTEGER, res, hlp );
    ret[2] = makeEdt( cat, key[2], tit[2],  dsc[2], T, def[2], INTEGER, res, hlp );
    ret[3] = makeEdt( cat, key[3], tit[3],  dsc[3], T, def[3], FLOAT,   res, hlp );
    ret[4] = makeEdt( cat, key[4], tit[4],  dsc[4], T, def[4], FLOAT,   res, hlp );
    ret[5] = makeEdt( cat, key[5], tit[5],  dsc[5], T, def[5], FLOAT,   res, hlp );
    return ret;
  }

  static TDPref[] makeDrawPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_DRAW;
    String[] key = TDPrefKey.DRAW;
    int[] tit    = TDPrefKey.DRAWtitle;
    int[] dsc    = TDPrefKey.DRAWdesc;
    String[] def = TDPrefKey.DRAWdef;
    TDPref[] ret = new TDPref[ 13 ];
    ret[ 0] = makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], N, def[ 0],          res, hlp );
    ret[ 1] = makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], FLOAT,   res, hlp );
    ret[ 2] = makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2], INTEGER, res, hlp );
    ret[ 3] = makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], N, def[ 3], FLOAT,   res, hlp );
    ret[ 4] = makeLst( cat, key[ 4], tit[ 4], dsc[ 4], N, def[ 4], R.array.lineStyle, R.array.lineStyleValue, res, hlp );
    ret[ 5] = makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], N, def[ 5], INTEGER, res, hlp );
    ret[ 6] = makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], A, def[ 6], FLOAT,   res, hlp );
    ret[ 7] = makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], A, def[ 7], FLOAT,   res, hlp );
    ret[ 8] = makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], A, def[ 8],          res, hlp );
    ret[ 9] = makeLst( cat, key[ 9], tit[ 9], dsc[ 9], E, def[ 9], R.array.lineContinue, R.array.lineContinueValue, res, hlp );
    ret[10] = makeCbx( cat, key[10], tit[10], dsc[10], N, def[10],          res, hlp );
    ret[11] = makeEdt( cat, key[11], tit[11], dsc[11], N, def[11], FLOAT,   res, hlp );
    ret[12] = makeEdt( cat, key[12], tit[12], dsc[12], N, def[12], FLOAT,   res, hlp );
    return ret;
  }

  static TDPref[] makeErasePrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_ERASE;
    String[] key = TDPrefKey.ERASE;
    int[] tit    = TDPrefKey.ERASEtitle;
    int[] dsc    = TDPrefKey.ERASEdesc;
    String[] def = TDPrefKey.ERASEdef;
    TDPref[] ret = new TDPref[ 3 ];
    ret[0] = makeEdt( cat, key[0], tit[0], dsc[0], B, def[0], INTEGER, res, hlp );
    ret[1] = makeEdt( cat, key[1], tit[1], dsc[1], B, def[1], INTEGER, res, hlp );
    ret[2] = makeEdt( cat, key[2], tit[2], dsc[2], E, def[2], INTEGER, res, hlp );
    return ret;
  }

  static TDPref[] makeEditPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_EDIT;
    String[] key = TDPrefKey.EDIT;
    int[] tit    = TDPrefKey.EDITtitle;
    int[] dsc    = TDPrefKey.EDITdesc;
    String[] def = TDPrefKey.EDITdef;
    TDPref[] ret = new TDPref[ 4 ];
    ret[0] = makeEdt( cat, key[0], tit[0], dsc[0], N, def[0], FLOAT,   res, hlp );
    ret[1] = makeEdt( cat, key[1], tit[1], dsc[1], B, def[1], INTEGER, res, hlp );
    ret[2] = makeEdt( cat, key[2], tit[2], dsc[2], E, def[2], INTEGER, res, hlp );
    ret[3] = makeEdt( cat, key[3], tit[3], dsc[3], E, def[3], INTEGER, res, hlp );
    return ret;
  }

  static TDPref[] makeSketchPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SKETCH;
    String[] key = TDPrefKey.SKETCH;
    int[] tit    = TDPrefKey.SKETCHtitle;
    int[] dsc    = TDPrefKey.SKETCHdesc;
    String[] def = TDPrefKey.SKETCHdef;
    TDPref[] ret = new TDPref[ 3 ];
    // ret[] = makeCbx( cat, key[],  tit[ ], dsc[ ], X, def[ ], res, hlp );
    ret[0] = makeLst( cat, key[0],   tit[0], dsc[0], D, def[0], R.array.modelType, R.array.modelTypeValue, res, hlp );
    ret[1] = makeEdt( cat, key[1],   tit[1], dsc[1], D, def[1], FLOAT, res, hlp );
    // ret[] = makeEdt( cat, key[],  tit[ ], dsc[ ], X, def[ ], FLOAT, res, hlp );
    // ret[] = makeEdt( cat, key[],  tit[ ], dsc[ ], X, def[ ], FLOAT, res, hlp );
    ret[2] = makeEdt( cat, key[2],   tit[2], dsc[2], D, def[2], FLOAT, res, hlp );
    // ret[3] = makeEdt( cat, key[], tit[ ], dsc[ ], X, def[ ], INTEGER, res, hlp );
    return ret;
  }

  static TDPref[] makeLogPrefs( Resources res, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_LOG;
    String[] key = TDPrefKey.LOG;
    int[] tit    = TDPrefKey.LOGtitle;
    TDPref[] ret = new TDPref[ 31 ];
    ret[0] = makeLst( cat, key[0],
               R.string.pref_log_stream_title, R.string.pref_log_stream_summary, T, "0", R.array.logStream, R.array.logStreamValue, res, hlp );
    ret[1]  = makeCbx( cat, key[ 1], tit[ 0], -1, T, false, res, hlp );
    ret[2]  = makeCbx( cat, key[ 2], tit[ 1], -1, T, false, res, hlp );
    ret[3]  = makeCbx( cat, key[ 3], tit[ 2], -1, T, true,  res, hlp );
    ret[4]  = makeCbx( cat, key[ 4], tit[ 3], -1, T, false, res, hlp );
    ret[5]  = makeCbx( cat, key[ 5], tit[ 4], -1, T, false, res, hlp );
    ret[6]  = makeCbx( cat, key[ 6], tit[ 5], -1, T, false, res, hlp );
    ret[7]  = makeCbx( cat, key[ 7], tit[ 6], -1, T, false, res, hlp );
    ret[8]  = makeCbx( cat, key[ 8], tit[ 7], -1, T, false, res, hlp );
    ret[9]  = makeCbx( cat, key[ 9], tit[ 8], -1, T, false, res, hlp );
    ret[10] = makeCbx( cat, key[10], tit[ 9], -1, T, false, res, hlp );
    ret[11] = makeCbx( cat, key[11], tit[10], -1, T, false, res, hlp );
    ret[12] = makeCbx( cat, key[12], tit[11], -1, T, false, res, hlp );
    ret[13] = makeCbx( cat, key[13], tit[12], -1, T, false, res, hlp );
    ret[14] = makeCbx( cat, key[14], tit[13], -1, T, false, res, hlp );
    ret[15] = makeCbx( cat, key[15], tit[14], -1, T, false, res, hlp );
    ret[16] = makeCbx( cat, key[16], tit[15], -1, T, false, res, hlp );
    ret[17] = makeCbx( cat, key[17], tit[16], -1, T, false, res, hlp );
    ret[18] = makeCbx( cat, key[18], tit[17], -1, T, false, res, hlp );
    ret[19] = makeCbx( cat, key[19], tit[18], -1, T, false, res, hlp );
    ret[20] = makeCbx( cat, key[20], tit[19], -1, T, false, res, hlp );
    ret[21] = makeCbx( cat, key[21], tit[20], -1, T, false, res, hlp );
    ret[22] = makeCbx( cat, key[22], tit[21], -1, T, false, res, hlp );
    ret[23] = makeCbx( cat, key[23], tit[22], -1, T, false, res, hlp );
    ret[24] = makeCbx( cat, key[24], tit[23], -1, T, false, res, hlp );
    ret[25] = makeCbx( cat, key[25], tit[24], -1, T, false, res, hlp );
    ret[26] = makeCbx( cat, key[26], tit[25], -1, T, false, res, hlp );
    ret[27] = makeCbx( cat, key[27], tit[26], -1, T, false, res, hlp );
    ret[28] = makeCbx( cat, key[28], tit[27], -1, T, false, res, hlp );
    ret[29] = makeCbx( cat, key[29], tit[28], -1, T, false, res, hlp );
    ret[30] = makeCbx( cat, key[30], tit[29], -1, T, false, res, hlp );

    return ret;
  }

}
