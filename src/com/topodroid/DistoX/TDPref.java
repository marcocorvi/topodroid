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

// import android.util.Log;

import android.content.Context;
// import android.content.res.Resources;

import android.widget.TextView;
import android.widget.EditText;
// import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
// import android.widget.LinearLayout;
import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

// import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
// import android.text.method.KeyListener;

class TDPref implements AdapterView.OnItemSelectedListener
                      , View.OnClickListener
		      , View.OnFocusChangeListener
		      , TextWatcher
		      , OnKeyListener
{
  TDPrefHelper helper = null;
  Context context;

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
  String def_value = null;
  boolean bvalue = false;
  int     ivalue = 0;
  float   fvalue = 0;
  int     category;
  boolean commit = false; // whether need to commit value to DB

  String[] options;
  String[] values;

  View mView = null;
  EditText mEdittext = null;

  private TDPref( int cat, String nm, int wt, int tit, int sum, int lvl, int pt, String val, String def_val, Context ctx, TDPrefHelper hlp )
  {
    name  = nm;
    wtype = wt;
    context = ctx;
    title   = ctx.getResources().getString( tit );
    summary = (sum >= 0)? ctx.getResources().getString( sum ) : null;
    level = lvl;
    ptype = pt;
    value = val;
    def_value = def_val;
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
	    mEdittext.setInputType( TDConst.TEXT );
            break;
        }
        mEdittext.addTextChangedListener( this );
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
  @Override
  public void afterTextChanged( Editable e ) { commit = true; }

  @Override
  public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

  @Override
  public void onTextChanged( CharSequence cs, int start, int before, int cnt ) { }
  
  void commitValueString()
  {
    // Log.v( "DistoXPref", "[*] " + name + " value " + value + " commit " + commit );
    if ( commit && mEdittext != null ) {
      String val = mEdittext.getText().toString();
      if ( ! value.equals( val ) ) {
        setValue( val );
        String text = TDSetting.updatePreference( helper, category, name, value );
        // Log.v( "DistoXPref", "[commitValueStrring] " + name + " value " + val + " text " + text );
	if ( text != null ) {
	  // Log.v("DistoX", "commit value <" + text + ">" );
	  mEdittext.setText( text );
	}
      }
      commit = false;
    }
  }

  @Override
  public void onFocusChange( View v, boolean has_focus )
  {
    // Log.v("DistoXPref", "focus change " + name + " focus " + has_focus + " commit " + commit );
    if ( (! has_focus) /* && ( v == mEdittext ) */ ) commitValueString();
  }

  @Override
  public void onClick(View v) 
  {
    switch ( v.getId() ) {
      case R.id.checkbox: // click always switches the checkbox
	bvalue = ((CheckBox)v).isChecked();
	value  = ( bvalue? "true" : "false" );
        // Log.v("DistoXPref", "[onClick] checkbox: " + name + " val " + value );
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
    if ( ivalue != pos ) {
      // Log.v("DistoXPref", "[onItemSelected]: " + name + " index " + ivalue + "->" + pos + " val " + values[pos] );
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
      // commit = true;
      // Log.v("DistoXPref", "on key " + keyCode + " commit " + commit );
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
  
  static private TDPref makeFwd( int cat, String nm, int tit, int lvl, Context ctx, TDPrefHelper hlp )
  { 
    return new TDPref( cat, nm, FORWARD, tit, -1, lvl, PREF, null, null, ctx, hlp );
  }

  static private TDPref makeBtn( int cat, String nm, int tit, int sum, int lvl, String def_val, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val );
    return new TDPref( cat, nm, BUTTON, tit, sum, lvl, PREF, val, def_val, ctx, hlp );
  }

  static private TDPref makeCbx( int cat, String nm, int tit, int sum, int lvl, boolean def_val, Context ctx, TDPrefHelper hlp )
  { 
    boolean val = hlp.getBoolean( nm, def_val );
    TDPref ret = new TDPref( cat, nm, CHECKBOX, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), (def_val? "true" : "false"), ctx, hlp );
    ret.bvalue = val;
    return ret;
  }

  static private TDPref makeCbx( int cat, String nm, int tit, int sum, int lvl, String def_str, Context ctx, TDPrefHelper hlp )
  { 
    boolean val = hlp.getBoolean( nm, def_str.startsWith("t") );
    TDPref ret = new TDPref( cat, nm, CHECKBOX, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), def_str, ctx, hlp );
    ret.bvalue = val;
    return ret;
  }

  static private TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, String def_val, int pt, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val );
    TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, def_val, ctx, hlp );
    try {
      if ( pt == INTEGER ) {
        ret.ivalue = Integer.parseInt( ret.value );
      } else if ( pt == FLOAT ) {
        ret.fvalue = Float.parseFloat( ret.value );
      }
    } catch ( NumberFormatException e ) { }
    return ret;
  }

  // static private TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, int idef, int pt, Context ctx, TDPrefHelper hlp)
  // { 
  //   String val = hlp.getString( nm, ctx.getString(idef) );
  //   TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, ctx, hlp );
  //   // Log.v("DistoX", "EditText value " + ret.value );
  //   if ( pt == INTEGER ) {
  //     ret.ivalue = Integer.parseInt( ret.value );
  //   } else if ( pt == FLOAT ) {
  //     ret.fvalue = Float.parseFloat( ret.value );
  //   }
  //   return ret;
  // }

  static private TDPref makeLst( int cat, String nm, int tit, int sum, int lvl, String def_val, int opts, int vals, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val ); // options stoctx the selected value
    String[] options = ctx.getResources().getStringArray( opts );
    String[] values  = ctx.getResources().getStringArray( vals );
    // String opt = getOptionFromValue( val, options, values );
    TDPref ret = new TDPref( cat, nm, LIST, tit, sum, lvl, OPTIONS, val, def_val, ctx, hlp );
    ret.options = options;
    ret.values  = values;
    int idx = ret.makeLstIndex( );
    // Log.v("DistoXPref", "make list [1] " + nm + " val <" + val + "> index " + idx );
    return ret;
  }

  static private TDPref makeLst( int cat, String nm, int tit, int sum, int lvl, int idef, int opts, int vals, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, ctx.getResources().getString(idef) ); // options stores the selected value
    String[] options = ctx.getResources().getStringArray( opts );
    String[] values  = ctx.getResources().getStringArray( vals );
    // String opt = getOptionFromValue( val, options, values );
    TDPref ret = new TDPref( cat, nm, LIST, tit, sum, lvl, OPTIONS, val, values[0], ctx, hlp );
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
    } else {
      try {
	if ( ptype == INTEGER ) {
          ivalue = Integer.parseInt( value );
        } else if ( ptype == FLOAT ) {
          fvalue = Float.parseFloat( value );
        } else if ( ptype == BOOLEAN ) {
          bvalue = Boolean.parseBoolean( value );
        }
      } catch ( NumberFormatException e ) {
	TDLog.Error("FIXME number format exception " + e.getMessage() );
      }
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
  boolean booleanValue() { return ( ptype == BOOLEAN )&& bvalue; }

  // -----------------------------------------------
  static final int B = 0;
  static final int N = 1;
  static final int A = 2;
  static final int E = 3;
  static final int T = 4;
  static final int D = 5;

  static TDPref[] makeMainPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_ALL;
    String[] key = TDPrefKey.MAIN;
    int[] tit = TDPrefKey.MAINtitle;
    int[] dsc = TDPrefKey.MAINdesc;
    String[] def = TDPrefKey.MAINdef;
    return new TDPref[ ] {
      makeBtn( cat, key[ 0], tit[ 0], dsc[ 0], N, def[ 0], ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], INTEGER, ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2], R.array.sizeButtons, R.array.sizeButtonsValue, ctx, hlp ),
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], B, def[ 3], R.array.extraButtons, R.array.extraButtonsValue, ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], B, def[ 4], ctx, hlp ),
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], T, def[ 5], ctx, hlp ),
      makeLst( cat, key[ 6], tit[ 6], dsc[ 6], A, def[ 6], R.array.localUserMan, R.array.localUserManValue, ctx, hlp ),
      makeLst( cat, key[ 7], tit[ 7], dsc[ 7], N, def[ 7], R.array.locale, R.array.localeValue, ctx, hlp ),
      makeLst( cat, key[ 8], tit[ 8], dsc[ 8], T, def[ 8], R.array.orientation, R.array.orientationValue, ctx, hlp ),
      // makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], D, def[ 8], ctx, hlp ), // IF_COSURVEY
      makeFwd( cat, key[ 9], tit[ 9],          B,          ctx, hlp ),
      makeFwd( cat, key[10], tit[10],          B,          ctx, hlp ),
      makeFwd( cat, key[11], tit[11],          B,          ctx, hlp ),
      makeFwd( cat, key[12], tit[12],          B,          ctx, hlp ),
      makeFwd( cat, key[13], tit[13],          T,          ctx, hlp ),    // GEEK
      makeFwd( cat, key[14], tit[14],          E,          ctx, hlp ),    // EXPORT SETTINGS
    };
  }

  static TDPref[] makeSurveyPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SURVEY;
    String[] key = TDPrefKey.SURVEY;
    int[] tit = TDPrefKey.SURVEYtitle;
    int[] dsc = TDPrefKey.SURVEYdesc;
    String[] def = TDPrefKey.SURVEYdef;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[0], B, def[0], STRING, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[1], B, def[1], R.array.surveyStations, R.array.surveyStationsValue, ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[2], B, def[2], R.array.stationNames, R.array.stationNamesValue, ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[3], B, def[3], STRING, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[4], A, def[4], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 5], tit[ 5], dsc[5], B, def[5], ctx, hlp ),
      makeCbx( cat, key[ 6], tit[ 6], dsc[6], B, def[6], ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 7], dsc[7], B, def[7], ctx, hlp ),
      makeFwd( cat, key[ 8], tit[ 8],         B,         ctx, hlp ),
      makeFwd( cat, key[ 9], tit[ 9],         B,         ctx, hlp ),
      makeFwd( cat, key[10], tit[10],         N,         ctx, hlp ),
      makeFwd( cat, key[11], tit[11],         A,         ctx, hlp )
    };
  }

  static TDPref[] makePlotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_PLOT;
    String[] key = TDPrefKey.PLOT;
    int[] tit = TDPrefKey.PLOTtitle;
    int[] dsc = TDPrefKey.PLOTdesc;
    String[] def = TDPrefKey.PLOTdef;
    return new TDPref[ ] {
      makeLst( cat, key[ 0], tit[ 0], dsc[0], B, def[0], R.array.pickerType, R.array.pickerTypeValue, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[1], N, def[1], R.array.recentNr, R.array.recentNr, ctx, hlp ),
      makeCbx( cat, key[ 2], tit[ 2], dsc[2], B, def[2], ctx, hlp ),
      makeLst( cat, key[ 3], tit[ 3], dsc[3], B, def[3], R.array.zoomCtrl, R.array.zoomCtrlValue, ctx, hlp ),
      // makeLst( cat, key[  ], tit[  ], dsc[ ], X, def[ ], R.array.sectionStations, R.array.sectionStationsValue, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[4], T, def[4], FLOAT,   ctx, hlp ), // X-SECTION H-THRESHOLD
      makeCbx( cat, key[ 5], tit[ 5], dsc[5], A, def[5], ctx, hlp ), // CHECK-MIDLINE
      makeCbx( cat, key[ 6], tit[ 6], dsc[6], A, def[6], ctx, hlp ), // CHECK-EXTEND
      makeFwd( cat, key[ 7], tit[ 7],         B,         ctx, hlp ),
      makeFwd( cat, key[ 8], tit[ 8],         N,         ctx, hlp ),
      makeFwd( cat, key[ 9], tit[ 9],         B,         ctx, hlp )
      // makeFwd( cat, key[11], tit[11],         T,         ctx, hlp ), // PLOT_WALLS
    };
  }

  static TDPref[] makeCalibPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_CALIB;
    String[] key = TDPrefKey.CALIB;
    int[] tit = TDPrefKey.CALIBtitle;
    int[] dsc = TDPrefKey.CALIBdesc;
    String[] def = TDPrefKey.CALIBdef;
    return new TDPref[ ] {
      makeLst( cat, key[ 0], tit[ 0], dsc[ 0], A, def[ 0], R.array.groupBy, R.array.groupByValue, ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], A, def[ 1], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], B, def[ 3], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], A, def[ 4], ctx, hlp ),
    // r[ ] = makeCbx( cat, key[  ], tit[  ], dsc[  ], X, def[  ], ctx, hlp );
      makeLst( cat, key[ 5], tit[ 5], dsc[ 5], A, def[ 5], R.array.rawCData, R.array.rawCDataValue, ctx, hlp ),
      makeLst( cat, key[ 6], tit[ 6], dsc[ 6], E, def[ 6], R.array.calibAlgo, R.array.calibAlgoValue, ctx, hlp ),
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], D, def[ 7], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], D, def[ 8], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], D, def[ 9], FLOAT, ctx, hlp ),
      makeEdt( cat, key[10], tit[10], dsc[10], D, def[10], FLOAT, ctx, hlp )
    };
  }

  static TDPref[] makeDevicePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_DEVICE;
    String[] key = TDPrefKey.DEVICE;
    int[] tit = TDPrefKey.DEVICEtitle;
    int[] dsc = TDPrefKey.DEVICEdesc;
    String[] def = TDPrefKey.DEVICEdef;
    return new TDPref[ ] {
      // makeEdt( cat, key[  ], tit[  ], dsc[  ], X, def[  ], STRING,  ctx, hlp ),
      // makeLst( cat, key[  ], tit[  ], dsc[  ], X, def[  ], R.array.deviceType, R.array.deviceTypeValue, ctx, hlp ),
      makeLst( cat, key[ 0], tit[ 0], dsc[ 0], N, def[ 0], R.array.deviceBT, R.array.deviceBTValue, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], R.array.connMode, R.array.connModeValue, ctx, hlp ),
      makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2],          ctx, hlp ),
      makeCbx( cat, key[ 3], tit[ 3], dsc[ 3], B, def[ 3],          ctx, hlp ),
      makeLst( cat, key[ 4], tit[ 4], dsc[ 4], B, def[ 4], R.array.sockType, R.array.sockTypeValue, ctx, hlp ),
      // makeEdt( cat, key[  ], tit[  ], dsc[  ], X, def[  ], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], N, def[ 5],          ctx, hlp ),
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], A, def[ 6],          ctx, hlp ),
      makeLst( cat, key[ 7], tit[ 7], dsc[ 7], E, def[ 7], R.array.feedbackMode, R.array.feedbackModeValue, ctx, hlp ),
      // makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], E, def[ 7], INTEGER, ctx, hlp ),
      // makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], A, def[ 8], INTEGER, ctx, hlp ),
      // makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9], INTEGER, ctx, hlp ),
      // makeEdt( cat, key[10], tit[10], dsc[10], A, def[10], INTEGER, ctx, hlp ),
      // makeEdt( cat, key[11], tit[11], dsc[11], A, def[11], INTEGER, ctx, hlp ),
      makeFwd( cat, key[ 8], tit[ 8],          B,                   ctx, hlp )
    };
  }

  static TDPref[] makeExportPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_EXPORT;
    String[] key = TDPrefKey.EXPORT;
    int[] tit = TDPrefKey.EXPORTtitle;
    int[] dsc = TDPrefKey.EXPORTdesc;
    String[] def = TDPrefKey.EXPORTdef;
    return new TDPref[ ] {
      makeLst( cat, key[ 0], tit[ 0], dsc[ 0], B, def[ 0], R.array.exportShots, R.array.exportShotsValue, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], R.array.exportPlot, R.array.exportPlotValue, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], A, def[2], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], A, def[3], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[4], tit[4], dsc[4], A, def[4], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[5], tit[5], dsc[5], E, def[5], FLOAT,  ctx, hlp ),
      makeFwd( cat, key[ 6], tit[ 6],          N,                   ctx, hlp ),
      makeFwd( cat, key[ 7], tit[ 7],          N,                   ctx, hlp ),
      makeFwd( cat, key[ 8], tit[ 8],          N,                   ctx, hlp ),
      makeFwd( cat, key[ 9], tit[ 9],          N,                   ctx, hlp ),
      makeFwd( cat, key[10], tit[10],          N,                   ctx, hlp ),
      makeFwd( cat, key[11], tit[11],          N,                   ctx, hlp ),
      makeFwd( cat, key[12], tit[12],          N,                   ctx, hlp ),
      makeFwd( cat, key[13], tit[13],          N,                   ctx, hlp ),
      makeFwd( cat, key[14], tit[14],          T,                   ctx, hlp ),
      makeFwd( cat, key[15], tit[15],          T,                   ctx, hlp )
    };
  }

  static TDPref[] makeImportPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_IMPORT;
    String[] key = TDPrefKey.EXPORT_import;
    int[] tit = TDPrefKey.EXPORT_importtitle;
    int[] dsc = TDPrefKey.EXPORT_importdesc;
    String[] def = TDPrefKey.EXPORT_importdef;
    return new TDPref[ ] {
      makeBtn( cat, key[ 0], tit[ 0], dsc[ 0], B, def[ 0],         ctx, hlp ),
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], A, def[ 1],         ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[ 2], T, def[ 2], R.array.importDatamode, R.array.importDatamodeValue, ctx, hlp )
    };
  }

  static TDPref[] makeSvxPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SVX;
    String[] key = TDPrefKey.EXPORT_SVX;
    int[] tit = TDPrefKey.EXPORT_SVXtitle;
    int[] dsc = TDPrefKey.EXPORT_SVXdesc;
    String[] def = TDPrefKey.EXPORT_SVXdef;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], N, def[0], R.array.survexEol, R.array.survexEolValue, ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], A, def[1],         ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], A, def[2],         ctx, hlp )
    };
  }

  static TDPref[] makeThPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_TH;
    String[] key = TDPrefKey.EXPORT_TH;
    int[] tit = TDPrefKey.EXPORT_THtitle;
    int[] dsc = TDPrefKey.EXPORT_THdesc;
    String[] def = TDPrefKey.EXPORT_THdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], A, def[0], ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], N, def[1], ctx, hlp ),
      // makeCbx( cat, key[ ], tit[ ], dsc[ ], X, def[ ], ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], A, def[2], ctx, hlp ),
      makeCbx( cat, key[3], tit[3], dsc[3], A, def[3], ctx, hlp ),
      // makeCbx( cat, key[4], tit[4], dsc[4], A, def[4], ctx, hlp ),
      makeEdt( cat, key[4], tit[4], dsc[4], E, def[4], INTEGER,  ctx, hlp ),
      makeCbx( cat, key[5], tit[5], dsc[5], E, def[5], ctx, hlp )
    };
  }

  static TDPref[] makeDatPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_DAT;
    String[] key = TDPrefKey.EXPORT_DAT;
    int[] tit = TDPrefKey.EXPORT_DATtitle;
    int[] dsc = TDPrefKey.EXPORT_DATdesc;
    String[] def = TDPrefKey.EXPORT_DATdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], B, def[0], ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], A, def[1], ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], N, def[2], ctx, hlp )
    };
  }

  static TDPref[] makeTroPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_TRO;
    String[] key = TDPrefKey.EXPORT_TRO;
    int[] tit = TDPrefKey.EXPORT_TROtitle;
    int[] dsc = TDPrefKey.EXPORT_TROdesc;
    String[] def = TDPrefKey.EXPORT_TROdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], A, def[0], ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], N, def[1], ctx, hlp )
    };
  }

  static TDPref[] makeSvgPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SVG;
    String[] key = TDPrefKey.EXPORT_SVG;
    int[] tit = TDPrefKey.EXPORT_SVGtitle;
    int[] dsc = TDPrefKey.EXPORT_SVGdesc;
    String[] def = TDPrefKey.EXPORT_SVGdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], N, def[0],         ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], E, def[1],         ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], E, def[2],         ctx, hlp ),
      // makeCbx( cat, key[ ], tit[ ], dsc[ ], X, def[ ],         ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], A, def[3], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[4], tit[4], dsc[4], A, def[4], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[5], tit[5], dsc[5], A, def[5], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[6], tit[6], dsc[6], A, def[6], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[7], tit[7], dsc[7], A, def[7], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[8], tit[8], dsc[8], A, def[8], FLOAT,  ctx, hlp )
    };
  }

  static TDPref[] makeDxfPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_DXF;
    String[] key = TDPrefKey.EXPORT_DXF;
    int[] tit = TDPrefKey.EXPORT_DXFtitle;
    int[] dsc = TDPrefKey.EXPORT_DXFdesc;
    String[] def = TDPrefKey.EXPORT_DXFdef;
    return new TDPref[ ] {
      // makeEdt( cat, key[ ]  tit[ ], dsc[ ], X, def[ ], FLOAT,  ctx, hlp ),
      makeCbx( cat, key[0], tit[0], dsc[0], N, def[0],         ctx, hlp ),
      makeLst( cat, key[1], tit[1], dsc[1], E, def[1], R.array.acadVersion, R.array.acadVersionValue, ctx, hlp )
    };
  }

  static TDPref[] makePngPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_PNG;
    String[] key = TDPrefKey.EXPORT_PNG;
    int[] tit = TDPrefKey.EXPORT_PNGtitle;
    int[] dsc = TDPrefKey.EXPORT_PNGdesc;
    String[] def = TDPrefKey.EXPORT_PNGdef;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], N, def[0], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], N, def[1], STRING, ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], N, def[2],         ctx, hlp ),  // DISTOX_SVG_GRID
      makeCbx( cat, key[3], tit[3], dsc[3], A, def[3],         ctx, hlp ),  // DISTOX_THEREION_SPLAYS
      makeCbx( cat, key[4], tit[4], dsc[4], N, def[4],         ctx, hlp )   // DISTOX_AUTO_STATIONS
    };
  }

  static TDPref[] makeKmlPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_KML;
    String[] key = TDPrefKey.EXPORT_KML;
    int[] tit = TDPrefKey.EXPORT_KMLtitle;
    int[] dsc = TDPrefKey.EXPORT_KMLdesc;
    String[] def = TDPrefKey.EXPORT_KMLdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], N, def[0],         ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], E, def[1],         ctx, hlp )
    };
  }

  static TDPref[] makeCsvPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_CSV;
    String[] key = TDPrefKey.EXPORT_CSV;
    int[] tit = TDPrefKey.EXPORT_CSVtitle;
    int[] dsc = TDPrefKey.EXPORT_CSVdesc;
    String[] def = TDPrefKey.EXPORT_CSVdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], N, def[0],         ctx, hlp ), // DISTOX_CSV_RAW
      makeLst( cat, key[1], tit[1], dsc[1], A, def[1], R.array.csvSeparator, R.array.csvSeparatorValue, ctx, hlp ), // DISTOX_CSV_SEP
      makeLst( cat, key[2], tit[2], dsc[2], N, def[2], R.array.survexEol, R.array.survexEolValue, ctx, hlp ) // DISTOC_SURVEX_EOL
    };
  }


  static TDPref[] makeShotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_SHOT_DATA;
    String[] key = TDPrefKey.DATA;
    int[] tit = TDPrefKey.DATAtitle;
    int[] dsc = TDPrefKey.DATAdesc;
    String[] def = TDPrefKey.DATAdef;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], B, def[ 0], FLOAT,   ctx, hlp ), // CLOSE_DISTANCE
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], FLOAT,   ctx, hlp ), // MAX_SHOT_LENGTH
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2], FLOAT,   ctx, hlp ), // MIN_LEG_LENGTH
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], A, def[ 3], R.array.legShots, R.array.legShotsValue, ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], T, def[ 4],          ctx, hlp ), // BACKSHOT DistoX
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], N, def[ 5], FLOAT,   ctx, hlp ), // EXTEND_THRS
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], N, def[ 6], FLOAT,   ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], A, def[ 7],          ctx, hlp ), // AZIMUTH_MANUAL
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], A, def[ 8],          ctx, hlp ), // PREV_NEXT
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9],          ctx, hlp ), // BACKSIGHT
      makeLst( cat, key[10], tit[10], dsc[10], N, def[10], R.array.feedbackMode, R.array.feedbackModeValue, ctx, hlp ) // DISTOX_LEG_FEEDBACK
      // makeEdt( cat, key[10], tit[10], dsc[10], T, def[10], INTEGER, ctx, hlp ), // TIMER
      // makeEdt( cat, key[11], tit[11], dsc[11], T, def[11], INTEGER, ctx, hlp ), // VOLUME
    };
  }

  static TDPref[] makeUnitsPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_SHOT_UNITS;
    String[] key = TDPrefKey.UNITS;
    int[] tit = TDPrefKey.UNITStitle;
    int[] dsc = TDPrefKey.UNITSdesc;
    String[] def = TDPrefKey.UNITSdef;
    int[] arr = TDPrefKey.UNITSarr;
    int[] val = TDPrefKey.UNITSval;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], B, def[0], arr[0], val[0], ctx, hlp ), // LENGTH
      makeLst( cat, key[1], tit[1], dsc[1], B, def[1], arr[1], val[1], ctx, hlp ), // ANGLE
      makeLst( cat, key[2], tit[2], dsc[2], B, def[2], arr[2], val[2], ctx, hlp )  // GRID
    };
  }

  static TDPref[] makeAccuracyPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_ACCURACY;
    String[] key = TDPrefKey.ACCURACY;
    int[] tit = TDPrefKey.ACCURACYtitle;
    int[] dsc = TDPrefKey.ACCURACYdesc;
    String[] def = TDPrefKey.ACCURACYdef;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], A, def[0], FLOAT, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], A, def[1], FLOAT, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], A, def[2], FLOAT, ctx, hlp )
    };
  }

  static TDPref[] makeLocationPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_LOCATION;
    String[] key = TDPrefKey.LOCATION;
    int[] tit = TDPrefKey.LOCATIONtitle;
    int[] dsc = TDPrefKey.LOCATIONdesc;
    String[] def = TDPrefKey.LOCATIONdef;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], N, def[0], R.array.unitLocation, R.array.unitLocationValue, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], A, def[1], STRING, ctx, hlp )
    };
  }

  static TDPref[] makeScreenPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_SCREEN;
    String[] key = TDPrefKey.SCREEN;
    int[] tit = TDPrefKey.SCREENtitle;
    int[] dsc = TDPrefKey.SCREENdesc;
    String[] def = TDPrefKey.SCREENdef;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], B, def[ 0], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], N, def[ 2], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], B, def[ 3], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], B, def[ 4], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], E, def[ 5], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], E, def[ 6], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], T, def[ 7], INTEGER, ctx, hlp )  // SPLAY ALPHA
    };
  }

  static TDPref[] makeLinePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_TOOL_LINE;
    String[] key = TDPrefKey.LINE;
    int[] tit    = TDPrefKey.LINEtitle;
    int[] dsc    = TDPrefKey.LINEdesc;
    String[] def = TDPrefKey.LINEdef;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], N, def[ 0], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], N, def[ 1], FLOAT,   ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[ 2], N, def[ 2], R.array.lineStyle, R.array.lineStyleValue, ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], N, def[ 3], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], A, def[ 4], FLOAT,   ctx, hlp ),
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], A, def[ 5],          ctx, hlp ),
      makeLst( cat, key[ 6], tit[ 6], dsc[ 6], E, def[ 6], R.array.lineContinue, R.array.lineContinueValue, ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], N, def[ 7],          ctx, hlp ),
    };
  }

  static TDPref[] makePointPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_TOOL_POINT;
    String[] key = TDPrefKey.POINT;
    int[] tit    = TDPrefKey.POINTtitle;
    int[] dsc    = TDPrefKey.POINTdesc;
    String[] def = TDPrefKey.POINTdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], N, def[0],        ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], B, def[1], FLOAT, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], B, def[2], FLOAT, ctx, hlp ) 
    };
  }

  static TDPref[] makeWallsPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_WALLS;
    String[] key = TDPrefKey.WALLS;
    int[] tit    = TDPrefKey.WALLStitle;
    int[] dsc    = TDPrefKey.WALLSdesc;
    String[] def = TDPrefKey.WALLSdef;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0],  dsc[0], T, def[0], R.array.wallsType, R.array.wallsTypeValue, ctx, hlp ),
      makeEdt( cat, key[1], tit[1],  dsc[1], T, def[1], INTEGER, ctx, hlp ),
      makeEdt( cat, key[2], tit[2],  dsc[2], T, def[2], INTEGER, ctx, hlp ),
      makeEdt( cat, key[3], tit[3],  dsc[3], T, def[3], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[4], tit[4],  dsc[4], T, def[4], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[5], tit[5],  dsc[5], T, def[5], FLOAT,   ctx, hlp )
    };
  }

  static TDPref[] makeDrawPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_DRAW;
    String[] key = TDPrefKey.DRAW;
    int[] tit    = TDPrefKey.DRAWtitle;
    int[] dsc    = TDPrefKey.DRAWdesc;
    String[] def = TDPrefKey.DRAWdef;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], N, def[ 0],          ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], B, def[ 1], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], B, def[ 2], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], N, def[ 3], FLOAT,   ctx, hlp ),
      makeLst( cat, key[ 4], tit[ 4], dsc[ 4], N, def[ 4], R.array.lineStyle, R.array.lineStyleValue, ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], N, def[ 5], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], A, def[ 6], FLOAT,   ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], A, def[ 7],          ctx, hlp ),
      makeLst( cat, key[ 8], tit[ 8], dsc[ 8], E, def[ 8], R.array.lineContinue, R.array.lineContinueValue, ctx, hlp ),
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], N, def[ 9],          ctx, hlp ),
      // makeEdt( cat, key[10], tit[10], dsc[10], A, def[10], FLOAT,   ctx, hlp ),
      // makeEdt( cat, key[11], tit[11], dsc[11], N, def[11], FLOAT,   ctx, hlp ),
      // makeEdt( cat, key[12], tit[12], dsc[12], N, def[12], FLOAT,   ctx, hlp ) 
    };
  }

  static TDPref[] makeErasePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_ERASE;
    String[] key = TDPrefKey.ERASE;
    int[] tit    = TDPrefKey.ERASEtitle;
    int[] dsc    = TDPrefKey.ERASEdesc;
    String[] def = TDPrefKey.ERASEdef;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], B, def[0], INTEGER, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], B, def[1], INTEGER, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], E, def[2], INTEGER, ctx, hlp )
    };
  }

  static TDPref[] makeEditPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_PLOT_EDIT;
    String[] key = TDPrefKey.EDIT;
    int[] tit    = TDPrefKey.EDITtitle;
    int[] dsc    = TDPrefKey.EDITdesc;
    String[] def = TDPrefKey.EDITdef;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], N, def[0], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], B, def[1], INTEGER, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], E, def[2], INTEGER, ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], E, def[3], INTEGER, ctx, hlp )
    };
  }


  static TDPref[] makeGeekDevicePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_GEEK_DEVICE;
    String[] key = TDPrefKey.GEEKDEVICE;
    int[] tit    = TDPrefKey.GEEKDEVICEtitle;
    int[] dsc    = TDPrefKey.GEEKDEVICEdesc;
    String[] def = TDPrefKey.GEEKDEVICEdef;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], E, def[0], INTEGER, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], A, def[1], INTEGER, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], A, def[2], INTEGER, ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], A, def[3], INTEGER, ctx, hlp ),
      makeEdt( cat, key[4], tit[4], dsc[4], A, def[4], INTEGER, ctx, hlp )
    };
  }

  static TDPref[] makeGeekLinePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_GEEK_LINE;
    String[] key = TDPrefKey.GEEKLINE;
    int[] tit    = TDPrefKey.GEEKLINEtitle;
    int[] dsc    = TDPrefKey.GEEKLINEdesc;
    String[] def = TDPrefKey.GEEKLINEdef;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], T, def[ 0], FLOAT,   ctx, hlp ), // REDUCE ANGLE
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], T, def[ 1], FLOAT,   ctx, hlp ), // BEZIER ACCUR.
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], T, def[ 2], FLOAT,   ctx, hlp ), //        CORNER
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], T, def[ 3], FLOAT,   ctx, hlp ), // WEED DISTANCE
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], T, def[ 4], FLOAT,   ctx, hlp ), //      LENGTH
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], T, def[ 5], FLOAT,   ctx, hlp ), //      BUFFER
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], T, def[ 6],          ctx, hlp ), // SNAP
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], T, def[ 7],          ctx, hlp ), // CURVE
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], T, def[ 8],          ctx, hlp ), // STRAIGHT
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], T, def[ 9],          ctx, hlp ), // PATH MULTISELECT
      makeCbx( cat, key[10], tit[10], dsc[10], T, def[10],          ctx, hlp )  // COMPOSITE ACTIONS
    };
  }


  static TDPref[] makeGeekShotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_GEEK_SHOT;
    String[] key = TDPrefKey.GEEKSHOT;
    int[] tit    = TDPrefKey.GEEKSHOTtitle;
    int[] dsc    = TDPrefKey.GEEKSHOTdesc;
    String[] def = TDPrefKey.GEEKSHOTdef;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], T, def[ 0],          ctx, hlp ), // DIVING_MODE
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], T, def[ 1],          ctx, hlp ), // RECENT_SHOT
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], T, def[ 2], INTEGER, ctx, hlp ), // RECENT TIMEOUT
      makeCbx( cat, key[ 3], tit[ 3], dsc[ 3], T, def[ 3],          ctx, hlp ), // SPLAY CLASSES
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], T, def[ 4],          ctx, hlp ), // SPLAY COLOR
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], T, def[ 5],          ctx, hlp ), // EXTEND FRAC
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], T, def[ 6],          ctx, hlp ), // BEDDING PLANE
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], A, def[ 7],          ctx, hlp ), // WITH SENSORS
      makeLst( cat, key[ 8], tit[ 8], dsc[ 8], E, def[ 8], R.array.loopClosure, R.array.loopClosureValue, ctx, hlp ),
      // makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], T, def[ 9], FLOAT,   ctx, hlp ), // DIST/ANGLE TOLERANCE
      // makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9],          ctx, hlp )  // SPLAYS AT ACTIVE STATION
      // makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9],          ctx, hlp )  // WITH RENAME
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], A, def[ 9],          ctx, hlp ),// WITH ANDROID AZIMUTH
      makeEdt( cat, key[10], tit[10], dsc[10], E, def[10], INTEGER, ctx, hlp ), // TIMER
      makeEdt( cat, key[11], tit[11], dsc[11], E, def[11], INTEGER, ctx, hlp )  // VOLUME
    };
  }

  static TDPref[] makeGeekPlotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_GEEK_PLOT;
    String[] key = TDPrefKey.GEEKPLOT;
    int[] tit    = TDPrefKey.GEEKPLOTtitle;
    int[] dsc    = TDPrefKey.GEEKPLOTdesc;
    String[] def = TDPrefKey.GEEKPLOTdef;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], T, def[ 0],          ctx, hlp ), // PLOT SHIFT
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], T, def[ 1],          ctx, hlp ), // PLOT SPLIT MERGE
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], A, def[ 2], INTEGER, ctx, hlp ), // MAX CLINO SPLAY-PLAN
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], T, def[ 3], R.array.splayDash, R.array.splayDashValue,       ctx, hlp ), // DASH COHERENCE
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], T, def[ 4], FLOAT,   ctx, hlp ), // DASH PLAN
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], T, def[ 5], FLOAT,   ctx, hlp ), // DASH PROFILE
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], T, def[ 6], FLOAT,   ctx, hlp ), // DASH X-SCETION
      makeLst( cat, key[ 7], tit[ 7], dsc[ 7], A, def[ 7], R.array.backupNumber, R.array.backupNumberValue, ctx, hlp ),
      makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], A, def[ 8], INTEGER, ctx, hlp ), // BACKUP INTERVAL
      // makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], T, def[ 9],          ctx, hlp ), // BACKUPS CLEAR
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], T, def[ 9],          ctx, hlp ), // AUTO XSECTIONS on export/save
      makeCbx( cat, key[10], tit[10], dsc[10], T, def[10],          ctx, hlp ), // SAVED STATIONS
      makeLst( cat, key[11], tit[11], dsc[11], T, def[11], R.array.canvasLevels, R.array.canvasLevelsValue, ctx, hlp )  // WITH LEVELS
    };
  }
  static TDPref[] makeGeekPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_GEEK;
    String[] key = TDPrefKey.GEEK;
    int[] tit    = TDPrefKey.GEEKtitle;
    int[] dsc    = TDPrefKey.GEEKdesc;
    String[] def = TDPrefKey.GEEKdef;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0],  T, def[0],  ctx, hlp ), // PALETTES
      makeCbx( cat, key[1], tit[1], dsc[1],  T, def[1],  ctx, hlp ), // BACKUP CLEAR
      makeCbx( cat, key[2], tit[2], dsc[2],  T, def[2],  ctx, hlp ), // PACKET LOGGER
      makeFwd( cat, key[3], tit[3],          T,          ctx, hlp ), // GEEK_SHOT
      makeFwd( cat, key[4], tit[4],          T,          ctx, hlp ), // GEEK_PLOT
      makeFwd( cat, key[5], tit[5],          T,          ctx, hlp ), // GEEK_LINE
      makeFwd( cat, key[6], tit[6],          T,          ctx, hlp ), // PLOT_WALLS
      makeFwd( cat, key[7], tit[7],          T,          ctx, hlp )  // GEEK_DEVICE
      // makeFwd( cat, key[8], tit[8],          D,          ctx, hlp )  // SKETCH // FIXME_SKETCH_3D
    };
  }

  /* FIXME_SKETCH_3D *
  static TDPref[] makeSketchPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_SKETCH;
    String[] key = TDPrefKey.SKETCH;
    int[] tit    = TDPrefKey.SKETCHtitle;
    int[] dsc    = TDPrefKey.SKETCHdesc;
    String[] def = TDPrefKey.SKETCHdef;
    return new TDPref[ ] {
      // makeCbx( cat, key[ ], tit[ ], dsc[ ], X, def[ ],          ctx, hlp ),
      makeLst( cat, key[0], tit[0], dsc[0], D, def[0], R.array.modelType, R.array.modelTypeValue, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], D, def[1], FLOAT,   ctx, hlp ),
      // makeEdt( cat, key[ ], tit[ ], dsc[ ], X, def[ ], FLOAT,   ctx, hlp ),
      // makeEdt( cat, key[ ], tit[ ], dsc[ ], X, def[ ], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], D, def[2], FLOAT,   ctx, hlp )
      // makeEdt( cat, key[ ], tit[ ], dsc[ ], X, def[ ], INTEGER, ctx, hlp ) 
    };
  }
  * END_SKETCH_3D */

  static TDPref[] makeLogPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefActivity.PREF_CATEGORY_LOG;
    String[] key = TDPrefKey.LOG;
    int[] tit    = TDPrefKey.LOGtitle;
    return new TDPref[ ] {
      makeLst( cat, key[0], R.string.pref_log_stream_title, R.string.pref_log_stream_summary, T, "0", R.array.logStream, R.array.logStreamValue, ctx, hlp ),
      makeCbx( cat, key[ 1], tit[ 0], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 2], tit[ 1], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 3], tit[ 2], -1, E, true,  ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 3], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 5], tit[ 4], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 6], tit[ 5], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 6], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 8], tit[ 7], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[ 9], tit[ 8], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[10], tit[ 9], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[11], tit[10], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[12], tit[11], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[13], tit[12], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[14], tit[13], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[15], tit[14], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[16], tit[15], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[17], tit[16], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[18], tit[17], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[19], tit[18], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[20], tit[19], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[21], tit[20], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[22], tit[21], -1, T, false, ctx, hlp ),
      makeCbx( cat, key[23], tit[22], -1, T, false, ctx, hlp ),
      makeCbx( cat, key[24], tit[23], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[25], tit[24], -1, T, false, ctx, hlp ),
      makeCbx( cat, key[26], tit[25], -1, E, false, ctx, hlp ),
      makeCbx( cat, key[27], tit[26], -1, T, false, ctx, hlp ),
      makeCbx( cat, key[28], tit[27], -1, T, false, ctx, hlp ),
      makeCbx( cat, key[29], tit[28], -1, E, false, ctx, hlp ) 
      // makeCbx( cat, key[30], tit[29], -1, E, false, ctx, hlp )  // DISTOX_LOG_SYNC
    };
  }

}
