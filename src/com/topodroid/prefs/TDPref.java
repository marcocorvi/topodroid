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
package com.topodroid.prefs;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyColorPicker;
import com.topodroid.TDX.TDLevel;
import com.topodroid.TDX.TDConst;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.R;

import android.content.Context;
// import android.content.res.Resources;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
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
                      , MyColorPicker.IColorChanged
{
  TDPrefHelper helper = null;
  Context context;

  static final int FORWARD  = 0;
  static final int BUTTON   = 1;
  static final int CHECKBOX = 2;
  static final int EDITTEXT = 3;
  static final int LIST     = 4;
  static final int COLORBOX = 5;
  static final int SPECIAL  = 6;

  static final int PREF     = 0;
  static final int INTEGER  = 1;
  static final int FLOAT    = 2;
  static final int STRING   = 3;
  static final int OPTIONS  = 4;
  static final int BOOLEAN  = 5;
  static final int COLOR    = 6;

  String name;
  int widget_type;   // widget type
  String title;
  String summary; // -1 if no summary
  int level;
  int pref_type;   // preference type
  String value = null;
  String def_value = null;
  boolean b_value = false;
  int     i_value = 0;
  float   f_value = 0;
  int     category;
  boolean commit = false; // whether need to commit value to DB

  String[] options;
  String[] values;

  View mView = null;
  EditText mEdittext = null;
  Button mButton = null;

  /** private cstr
   * @param cat     category
   * @param nm      name
   * @param wt      type
   * @param tit     display title
   * @param sum     display summary
   * @param lvl     activity level
   * @param pt      preference type
   * @param val     value
   * @param def_val default value
   * @param ctx     context
   * @param hlp     shared preference helper
   */
  private TDPref( int cat, String nm, int wt, int tit, int sum, int lvl, int pt, String val, String def_val, Context ctx, TDPrefHelper hlp )
  {
    name  = nm;
    widget_type = wt;
    context = ctx;
    title   = ctx.getResources().getString( tit );
    summary = (sum >= 0)? ctx.getResources().getString( sum ) : null;
    level = lvl;
    pref_type = pt;
    value = val;
    def_value = def_val;
    options = null;
    values  = null;
    helper  = hlp;
    category = cat;
    commit   = false;
  }

  final static int mDebugColor = 0xff3399cc;
  final static int mSpecialColor = 0xff6699ff; // pref_category

  /** @return the view that displays this preference
   * @param context   context
   * @param li        layout inflater
   * @param parent    parent view
   */
  View getView( Context context, LayoutInflater li, ViewGroup parent )
  {
    // TDLog.v("PREF get view " + title );
    View v = null;
    Spinner spinner;
    CheckBox checkbox;
    TextView textview;
    switch ( widget_type ) {
      case FORWARD:
        v = li.inflate( R.layout.pref_forward, parent, false );
        if ( level > TDLevel.TESTER ) {
          ((TextView)v.findViewById(R.id.title)).setTextColor( mDebugColor );
        }
        break;
      case BUTTON:
        v = li.inflate( R.layout.pref_button, parent, false );
        ( (TextView) v.findViewById( R.id.value ) ).setText( stringValue() );
        if ( level > TDLevel.TESTER ) {
          ((TextView)v.findViewById(R.id.title)).setTextColor( mDebugColor );
        }
        break;
      case CHECKBOX:
        v = li.inflate( R.layout.pref_checkbox, parent, false );
	checkbox = (CheckBox) v.findViewById( R.id.checkbox );
	checkbox.setChecked( booleanValue() );
	checkbox.setOnClickListener( this );
        if ( level > TDLevel.TESTER ) {
          ((TextView)v.findViewById(R.id.title)).setTextColor( mDebugColor );
        }
        break;
      case EDITTEXT:
        v = li.inflate( R.layout.pref_edittext, parent, false );
	mEdittext = (EditText) v.findViewById( R.id.edittext );
	mEdittext.setText( stringValue() );
        switch ( pref_type ) {
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
        if ( level > TDLevel.TESTER ) {
          ((TextView)v.findViewById(R.id.title)).setTextColor( mDebugColor );
        }
        break;
      case LIST:
        v = li.inflate( R.layout.pref_spinner, parent, false );
	spinner = (Spinner) v.findViewById( R.id.spinner );
        spinner.setAdapter( new ArrayAdapter<>( context, R.layout.menu, options ) );
        spinner.setSelection( intValue() );
        spinner.setOnItemSelectedListener( this );
        break;
      case COLORBOX:
        v = li.inflate( R.layout.pref_color, parent, false );
	mButton = (Button) v.findViewById( R.id.button );
	mButton.setOnClickListener( this );
        setColor( value );
        break;
      case SPECIAL: // checkbox
        v = li.inflate( R.layout.pref_checkbox, parent, false );
	checkbox = (CheckBox) v.findViewById( R.id.checkbox );
	checkbox.setChecked( booleanValue() );
	checkbox.setOnClickListener( this );
        // if ( level > TDLevel.TESTER ) {
          ((TextView)v.findViewById(R.id.title)).setTextColor( mSpecialColor );
        // } 

        break;
    }
    textview = (TextView) v.findViewById( R.id.title ); // FIXME may null pointer
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

  /** @return the view stored in this preference
   */
  View getView() { return mView; }

  /** set the button color
   * @param color color (string)
   */
  private void setColor( String color )
  {
    setColor( Integer.parseInt( color ) );
  }

  /** set the button color
   * @param color color (int 0xRRGGBB)
   */
  private void setColor( int color )
  {
    if ( mButton == null ) return;
    i_value = color & 0xffffff;
    mButton.setBackgroundColor( 0xff000000 | i_value );
    // TDLog.v( name + " set color " + i_value + " " + color );
  }

  // -------------------------------------------------------------------------
  /** react to a user finish text change
   * @param e    editable ???
   */
  @Override
  public void afterTextChanged( Editable e ) { commit = true; }

  /** react to a user init text change - it does nothing
   * @param cs     text
   * @param start  ...
   * @param cnt    ...
   * @param after  ...
   */
  @Override
  public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

  /** react to a user text change - it does nothing
   * @param cs     text
   * @param start  ...
   * @param before ...
   * @param cnt    ...
   */
  @Override
  public void onTextChanged( CharSequence cs, int start, int before, int cnt ) { }

  /** commit the preference (string) value
   */
  void commitValueString()
  {
    // if ( name.equals("DISTOX_TEAM_DIALOG") ) {
    //   TDLog.v( "Pref commit [*] <" + name + "> value " + value + " commit " + commit );
    // }
    if ( commit && mEdittext != null ) {
      String val = mEdittext.getText().toString();
      if ( ! value.equals( val ) ) {
        setValue( val /*, name.equals("DISTOX_TEAM_DIALOG") */ );
        String text = TDSetting.updatePreference( helper, category, name, value );
        // TDLog.v( "Pref [commitValueString] " + name + " value " + val + " text " + text );
	if ( text != null ) {
	  // TDLog.v("commit value <" + text + ">" );
	  mEdittext.setText( text );
	}
      }
      commit = false;
    }
  }

  /** react to a view focus change
   * @param v         affected view
   * @param has_focus ...
   */
  @Override
  public void onFocusChange( View v, boolean has_focus )
  {
    // TDLog.v( "Pref focus change " + name + " focus " + has_focus + " commit " + commit );
    if ( (! has_focus) /* && ( v == mEdittext ) */ ) commitValueString();
  }

  /** react to a user tap on a view
   * @param v         tapped view
   */
  @Override
  public void onClick(View v) 
  {
    int vid = v.getId();
    if ( vid == R.id.checkbox ) { // click always switches the checkbox
      b_value = ((CheckBox)v).isChecked();
      value  = ( b_value? "true" : "false" );
      // TDLog.v( "Pref [onClick] checkbox: " + name + " val " + value );
      TDSetting.updatePreference( helper, category, name, value );
    } else if ( vid == R.id.title ) {
      /* nothing */
      // TDLog.v( "Pref TODO title click tell TDSetting " + title );
    } else if ( vid == R.id.button ) {
      // TDLog.v(" start the color picker dialog ");
      int color = intValue();
      (new MyColorPicker( context, this, color )).show();
    }
  }

  // IColorChanged
  public void colorChanged( int color )
  {
    setColor( color );
    // TDLog.v(name + " update color " + i_value + " " + color );
    TDSetting.updatePreference( helper, category, name, Integer.toString( i_value ) );
  }

  /** react to a user item selection
   * @param av    parent adapter
   * @param v     tapped view
   * @param pos   item position 
   * @param id    ... (not used)
   */
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id )
  {
    value  = options[pos];
    if ( i_value != pos ) {
      // TDLog.v( "Pref [onItemSelected]: " + name + " index " + i_value + "->" + pos + " val " + values[pos] );
      i_value = pos;
      TDSetting.updatePreference( helper, category, name, values[ i_value ] ); // options store the selected value
    }
  }

  /** react to a user items deselection
   * @param av    parent adapter
   */
  @Override
  public void onNothingSelected( AdapterView av )
  {
    i_value = Integer.parseInt( value );
    value  = options[i_value];
    // TDLog.v( "Pref TODO nothing Selected: " + name + " index " + i_value + " val " + value );
  }

  /** react to a user key press
   * @param view       affected view
   * @param keyCode key code
   * @param event   key event
   * @return true if key-press has been handled
   */
  @Override
  public boolean onKey( View view, int keyCode, KeyEvent event)
  {
    if ( view == mEdittext ) {
      // commit = true;
      // TDLog.v( "Pref on key " + keyCode + " commit " + commit );
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
  //   switch ( pref_type ) {
  //     case INTEGER: return TDConst.NUMBER;
  //     case FLOAT:   return TDConst.NUMBER_DECIMAL;
  //   }
  //   return InputType.TYPE_NULL;
  // }

  // -------------------------------------------------------------------------
  // creators
 
  /** factory cstr a "forward" preference
   * @param cat   category
   * @param nm    preference name
   * @param tit   preference title
   * @param lvl   activity level
   * @param ctx   context
   * @param hlp   shared preferences helper
   */ 
  private static TDPref makeFwd( int cat, String nm, int tit, int lvl, Context ctx, TDPrefHelper hlp )
  { 
    // TDLog.v("PREF make fwd: " + nm + " level " + lvl );
    return new TDPref( cat, nm, FORWARD, tit, -1, lvl, PREF, null, null, ctx, hlp );
  }
 
  /** factory cstr a "special" preference
   * @param cat   category
   * @param nm    preference name
   * @param tit   preference title
   * @param lvl   activity level
   * @param ctx   context
   * @param hlp   shared preferences helper
   */ 
  private static TDPref makeSpecial( int cat, String nm, int tit, int sum, int lvl, String def_str, Context ctx, TDPrefHelper hlp )
  { 
    // TDLog.v("PREF make special: " + nm + " level " + lvl );
    boolean val = hlp.getBoolean( nm, def_str.startsWith("t") );
    TDPref ret = new TDPref( cat, nm, SPECIAL, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), def_str, ctx, hlp );
    ret.b_value = val;
    return ret;
  }

  /** factory cstr a "button" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_val preference default value
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeBtn( int cat, String nm, int tit, int sum, int lvl, String def_val, Context ctx, TDPrefHelper hlp )
  { 
    // TDLog.v("BTN " + nm + " " + def_val );
    String val = hlp.getString( nm, def_val );
    return new TDPref( cat, nm, BUTTON, tit, sum, lvl, PREF, val, def_val, ctx, hlp );
  }

  /** factory cstr a "checkbox" preference - not used
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_val preference default boolean value
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  // private static TDPref makeCbx( int cat, String nm, int tit, int sum, int lvl, boolean def_val, Context ctx, TDPrefHelper hlp )
  // { 
  //   boolean val = hlp.getBoolean( nm, def_val );
  //   TDPref ret = new TDPref( cat, nm, CHECKBOX, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), (def_val? "true" : "false"), ctx, hlp );
  //   ret.b_value = val;
  //   // TDLog.v("make CBX " + nm + " value " + val );
  //   return ret;
  // }

  /** factory cstr a "checkbox" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_str preference default (string) value
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeCbx( int cat, String nm, int tit, int sum, int lvl, String def_str, Context ctx, TDPrefHelper hlp )
  { 
    boolean val = hlp.getBoolean( nm, def_str.startsWith("t") );
    TDPref ret = new TDPref( cat, nm, CHECKBOX, tit, sum, lvl, BOOLEAN, (val? "true" : "false"), def_str, ctx, hlp );
    ret.b_value = val;
    // TDLog.v("make CBX " + nm + " value " + val );
    return ret;
  }

  /** factory cstr a "color" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_val preference default boolean value
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeColor( int cat, String nm, int tit, int sum, int lvl, int def_val, Context ctx, TDPrefHelper hlp )
  { 
    String def_str = Integer.toString( def_val );
    String str = hlp.getString( nm, def_str );
    // TDLog.v("[1] Helper " + nm + " color " + str + " default " + def_str );
    TDPref ret = new TDPref( cat, nm, COLORBOX, tit, sum, lvl, COLOR, str, def_str, ctx, hlp );
    int color = def_val;
    try {
      color = Integer.parseInt( str );
    } catch ( NumberFormatException e ) {
      TDLog.e( e.getMessage() );
    }
    ret.setColor( color );
    return ret;
  }

  /** factory cstr a "checkbox" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_str preference default (string) value
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeColor( int cat, String nm, int tit, int sum, int lvl, String def_str, Context ctx, TDPrefHelper hlp )
  { 
    String str = hlp.getString( nm, def_str );
    // TDLog.v("[2] Helper " + nm + " color " + str + " default " + def_str );
    TDPref ret = new TDPref( cat, nm, COLORBOX, tit, sum, lvl, COLOR, str, def_str, ctx, hlp );
    int color = 0;
    try {
      color = Integer.parseInt( str );
    } catch ( NumberFormatException e ) { color = Integer.parseInt( def_str ); }
    ret.setColor( color );
    return ret;
  }

  /** factory cstr a "text" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_val preference default text value
   * @param pt      preference type
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, String def_val, int pt, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val );
    TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, def_val, ctx, hlp );
    try {
      if ( pt == INTEGER ) {
        ret.i_value = Integer.parseInt( ret.value );
      } else if ( pt == FLOAT ) {
        ret.f_value = Float.parseFloat( ret.value );
      }
    } catch ( NumberFormatException e ) {
      TDLog.e( e.getMessage() );
    }
    return ret;
  }

  // private static TDPref makeEdt( int cat, String nm, int tit, int sum, int lvl, int id_def, int pt, Context ctx, TDPrefHelper hlp)
  // { 
  //   String val = hlp.getString( nm, ctx.getString(id_def) );
  //   TDPref ret = new TDPref( cat, nm, EDITTEXT, tit, sum, lvl, pt, val, ctx, hlp );
  //   // TDLog.v("EditText value " + ret.value );
  //   if ( pt == INTEGER ) {
  //     ret.i_value = Integer.parseInt( ret.value );
  //   } else if ( pt == FLOAT ) {
  //     ret.f_value = Float.parseFloat( ret.value );
  //   }
  //   return ret;
  // }

  /** factory cstr a "list" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param def_val preference default text value
   * @param opts    options resource ID
   * @param vals    values resource ID
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeLst( int cat, String nm, int tit, int sum, int lvl, String def_val, int opts, int vals, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, def_val ); // options ... the selected value
    // boolean team_dialog = false;
    // if ( nm.equals("DISTOX_TEAM_DIALOG" ) ) {
    //   team_dialog = true;
    //   TDLog.v( "Pref make list [1] <" + nm + "> helper get <" + val + "> default <" + def_val + ">" );
    // }
    String[] options = ctx.getResources().getStringArray( opts );
    String[] values  = ctx.getResources().getStringArray( vals );
    // String opt = getOptionFromValue( val, options, values );
    TDPref ret = new TDPref( cat, nm, LIST, tit, sum, lvl, OPTIONS, val, def_val, ctx, hlp );
    ret.options = options;
    ret.values  = values;
    int idx = ret.makeLstIndex( /* team_dialog */ );
    // if ( team_dialog ) {
    //   TDLog.v( "Pref make list [1] <" + nm + "> val <" + val + "> default <" + def_val + "> index " + idx );
    // }
    return ret;
  }

  /** factory cstr a "list" preference
   * @param cat     category
   * @param nm      preference name
   * @param tit     preference title
   * @param sum     preference description
   * @param lvl     activity level
   * @param id_def    preference default value resource ID
   * @param opts    options resource ID
   * @param vals    values resource ID
   * @param ctx     context
   * @param hlp     shared preferences helper
   */ 
  private static TDPref makeLst( int cat, String nm, int tit, int sum, int lvl, int id_def, int opts, int vals, Context ctx, TDPrefHelper hlp )
  { 
    String val = hlp.getString( nm, ctx.getResources().getString(id_def) ); // options stores the selected value
    String[] options = ctx.getResources().getStringArray( opts );
    String[] values  = ctx.getResources().getStringArray( vals );
    // String opt = getOptionFromValue( val, options, values );
    TDPref ret = new TDPref( cat, nm, LIST, tit, sum, lvl, OPTIONS, val, values[0], ctx, hlp );
    ret.options = options;
    ret.values  = values;
    int idx = ret.makeLstIndex( /* nm.equals("DISTOX_TEAM_DIALOG") */ );
    // TDLog.v( "Pref make list [2] " + nm + " val <" + val + "> index " + idx );
    return ret;
  }

  // -----------------------------------------------------------------------

  /** @return the list index equal to the "value"
   */
  private int makeLstIndex( /* boolean debug */ )
  {
    // if ( debug ) {
    //   StringBuilder sb = new StringBuilder();
    //   for ( String opt : options ) sb.append(" <").append( opt ).append(">");
    //   TDLog.v( "Pref make list index: val <" + value + "> opts size " + options.length + ":" + sb.toString() );
    // }
    if ( value == null || value.length() == 0 ) {
      for ( int k=0; k< values.length; ++k ) { 
        if ( values[k].length() == 0 ) {
          i_value = k;
          return k;
        }
      }
    } else {
      for ( int k=0; k< values.length; ++k ) { 
        if ( value.equals( values[k] ) ) {
          i_value = k;
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

  /** set the preference value
   * @param val  new value
   */
  void setValue( String val /*, boolean debug */ )
  {
    value = val;
    if ( pref_type == OPTIONS ) {
      makeLstIndex( /* debug */ );
    } else {
      try {
	if ( pref_type == INTEGER ) {
          i_value = Integer.parseInt( value );
        } else if ( pref_type == FLOAT ) {
          f_value = Float.parseFloat( value );
        } else if ( pref_type == BOOLEAN ) {
          b_value = Boolean.parseBoolean( value );
        }
      } catch ( NumberFormatException e ) {
	TDLog.e("FIXME number format exception " + e.getMessage() );
      }
    }
  }

  /** set the "button" preference value
   * @param val  new value
   */
  void setButtonValue( String val )
  {
    if ( mView != null ) {
      TextView tv = (TextView) mView.findViewById( R.id.value );
      if ( tv != null ) tv.setText( val );
    }
    value = val;
  }

  /** @return the preference integer value
   */
  int intValue()         { return ( pref_type == INTEGER || pref_type == OPTIONS )? i_value : 0; }

  /** @return the preference float value
   */
  float floatValue()     { return ( pref_type == FLOAT )? f_value : 0; }

  /** @return the preference string value
   */
  String stringValue()   { return value; }

  /** @return the preference boolean value
   */
  boolean booleanValue() { return ( pref_type == BOOLEAN )&& b_value; }

  // -----------------------------------------------
  /** construct the general "main" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of main preferences
   */
  static TDPref[] makeMainPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_ALL;
    String[] key = TDPrefKey.MAIN;
    int[] tit = TDPrefKey.MAINtitle;
    int[] dsc = TDPrefKey.MAINdesc;
    String[] def = TDPrefKey.MAINdef;
    int[] lvl = TDPrefKey.MAINlvl;
    return new TDPref[ ] {
      // makeBtn( cat, key[ 0], tit[ 0], dsc[ 0], N, def[ 0], ctx, hlp ), // DISTOX_CWD
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], INTEGER, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], R.array.sizeButtons, R.array.sizeButtonsValue, ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT, ctx, hlp ),
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], R.array.extraButtons, R.array.extraButtonsValue, ctx, hlp ),
      makeLst( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], R.array.localUserMan, R.array.localUserManValue, ctx, hlp ),
      makeLst( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], R.array.locale, R.array.localeValue, ctx, hlp ),
      makeLst( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], R.array.orientation, R.array.orientationValue, ctx, hlp ),
      // makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[8], def[ 8], ctx, hlp ), // IF_COSURVEY
      makeFwd( cat, key[ 7], tit[ 7],          lvl[ 7],          ctx, hlp ),    // IMPORT EXPORT
      makeFwd( cat, key[ 8], tit[ 8],          lvl[ 8],          ctx, hlp ),    // SURVEY DATA
      makeFwd( cat, key[ 9], tit[ 9],          lvl[ 9],          ctx, hlp ),    // SKETCHING
      makeFwd( cat, key[10], tit[10],          lvl[10],          ctx, hlp ),    // DEVICES
      makeFwd( cat, key[11], tit[11],          lvl[11],          ctx, hlp ),    // CAVE3D
      makeFwd( cat, key[12], tit[12],          lvl[12],          ctx, hlp ),    // GEEK
      makeFwd( cat, key[13], tit[13],          lvl[13],          ctx, hlp ),    // EXPORT SETTINGS
    };
  }

  /** construct the "survey" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "survey" preferences
   */
  static TDPref[] makeSurveyPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_SURVEY;
    String[] key = TDPrefKey.SURVEY;
    int[] tit = TDPrefKey.SURVEYtitle;
    int[] dsc = TDPrefKey.SURVEYdesc;
    String[] def = TDPrefKey.SURVEYdef;
    int[] lvl = TDPrefKey.SURVEYlvl;
    // TDLog.v("pref SURVEY TEAM DIALOG: " + key[1] + " default " + def[1] + " setting " + TDSetting.mTeamNames );
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[0], lvl[0], def[0], STRING, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[1], lvl[1], def[1], R.array.teamNames, R.array.teamNamesValue, ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[2], lvl[2], def[2], R.array.surveyStations, R.array.surveyStationsValue, ctx, hlp ),
      makeLst( cat, key[ 3], tit[ 3], dsc[3], lvl[3], def[3], R.array.stationNames, R.array.stationNamesValue, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[4], lvl[4], def[4], STRING, ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[5], lvl[5], def[5], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 6], tit[ 6], dsc[6], lvl[6], def[6], ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 7], dsc[7], lvl[7], def[7], ctx, hlp ),
      makeCbx( cat, key[ 8], tit[ 8], dsc[8], lvl[8], def[8], ctx, hlp ),
      // makeCbx( cat, key[ 8], tit[ 8], dsc[8], lvl[8], def[8], ctx, hlp ),
      makeFwd( cat, key[ 9], tit[ 9],         lvl[ 9],         ctx, hlp ),
      makeFwd( cat, key[10], tit[10],         lvl[10],         ctx, hlp ),
      makeFwd( cat, key[11], tit[11],         lvl[11],         ctx, hlp ),
      makeFwd( cat, key[12], tit[12],         lvl[12],         ctx, hlp )
    };
  }

  /** construct the "plot" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "plot" preferences
   */
  static TDPref[] makePlotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_PLOT;
    String[] key = TDPrefKey.PLOT;
    int[] tit = TDPrefKey.PLOTtitle;
    int[] dsc = TDPrefKey.PLOTdesc;
    String[] def = TDPrefKey.PLOTdef;
    int[] lvl = TDPrefKey.PLOTlvl;
    return new TDPref[ ] {
      // makeLst( cat, key[ 0], tit[ 0], dsc[0], lvl[0], def[0], R.array.pickerType, R.array.pickerTypeValue, ctx, hlp ),
      // makeCbx( cat, key[ 1], tit[ 1], dsc[1], lvl[1], def[1], ctx, hlp ),
      // makeLst( cat, key[ 1], tit[ 1], dsc[1], lvl[1], def[1], R.array.recentNr, R.array.recentNr, ctx, hlp ),
      makeCbx( cat, key[ 0], tit[ 0], dsc[0], lvl[ 0], def[0], ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[1], lvl[ 1], def[1], R.array.zoomCtrl, R.array.zoomCtrlValue, ctx, hlp ),
      // makeLst( cat, key[  ], tit[  ], dsc[ ], lvl[ ], def[ ], R.array.sectionStations, R.array.sectionStationsValue, ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[2], lvl[ 2], def[2], FLOAT,   ctx, hlp ), // X-SECTION H-THRESHOLD
      makeCbx( cat, key[ 3], tit[ 3], dsc[3], lvl[ 3], def[3], ctx, hlp ), // CHECK-MIDLINE
      makeCbx( cat, key[ 4], tit[ 4], dsc[4], lvl[ 4], def[4], ctx, hlp ), // CHECK-EXTEND
      makeEdt( cat, key[ 5], tit[ 5], dsc[5], lvl[ 5], def[5], FLOAT, ctx, hlp ), // DISTOX_TOOLBAR_SIZE
      makeFwd( cat, key[ 6], tit[ 6],         lvl[ 6],         ctx, hlp ),
      makeFwd( cat, key[ 7], tit[ 7],         lvl[ 7],         ctx, hlp ),
      makeFwd( cat, key[ 8], tit[ 8],         lvl[ 8],         ctx, hlp )
      // makeFwd( cat, key[11], tit[11],         lvl[11],         ctx, hlp ), // PLOT_WALLS
    };
  }

  /** construct the "calibration" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "calibration" preferences
   */
  static TDPref[] makeCalibPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_CALIB;
    String[] key = TDPrefKey.CALIB;
    int[] tit = TDPrefKey.CALIBtitle;
    int[] dsc = TDPrefKey.CALIBdesc;
    String[] def = TDPrefKey.CALIBdef;
    int[] lvl = TDPrefKey.CALIBlvl;
    return new TDPref[ ] {
      makeLst( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], R.array.groupBy, R.array.groupByValue, ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], ctx, hlp ),
    // r[ ] = makeCbx( cat, key[  ], tit[  ], dsc[  ], X, def[  ], ctx, hlp );
      makeLst( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], R.array.rawCData, R.array.rawCDataValue, ctx, hlp ),
      // makeLst( cat, key[ 6], tit[ 6], dsc[ 6], E, def[ 6], R.array.calibAlgo, R.array.calibAlgoValue, ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8], FLOAT, ctx, hlp ),
      makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9], FLOAT, ctx, hlp ),
      makeEdt( cat, key[10], tit[10], dsc[10], lvl[10], def[10], FLOAT, ctx, hlp ), // AUTO_CAL
      makeEdt( cat, key[11], tit[11], dsc[11], lvl[11], def[11], FLOAT, ctx, hlp ),
      makeEdt( cat, key[12], tit[12], dsc[12], lvl[12], def[12], FLOAT, ctx, hlp ),
      makeEdt( cat, key[13], tit[13], dsc[13], lvl[13], def[13], FLOAT, ctx, hlp )
    };
  }

  /** construct the "device" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "device" preferences
   */
  static TDPref[] makeDevicePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_DEVICE;
    String[] key = TDPrefKey.DEVICE;
    int[] tit = TDPrefKey.DEVICEtitle;
    int[] dsc = TDPrefKey.DEVICEdesc;
    String[] def = TDPrefKey.DEVICEdef;
    int[] lvl = TDPrefKey.DEVICElvl;
    
    return new TDPref[ ] {
      // makeEdt( cat, key[  ], tit[  ], dsc[  ], lvl[], def[  ], STRING,  ctx, hlp ),
      // makeLst( cat, key[  ], tit[  ], dsc[  ], lvl[], def[  ], R.array.deviceType, R.array.deviceTypeValue, ctx, hlp ),
      makeLst( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], R.array.deviceBT, R.array.deviceBTValue, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], R.array.connMode, R.array.connModeValue, ctx, hlp ),
      // makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[], def[ 2],          ctx, hlp ),
      makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2],          ctx, hlp ),
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], R.array.sockType, R.array.sockTypeValue, ctx, hlp ),
      // makeEdt( cat, key[  ], tit[  ], dsc[  ], lvl[], def[  ], INTEGER, ctx, hlp ),
      // makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], lvl[], def[ 4],          ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4],          ctx, hlp ),
      makeLst( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], R.array.feedbackMode, R.array.feedbackModeValue, ctx, hlp ),
      makeFwd( cat, key[ 6], tit[ 6],          lvl[ 6],                   ctx, hlp )
    };
  }

  /** construct the "export" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "export" preferences
   */
  static TDPref[] makeExportPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat      = TDPrefCat.PREF_CATEGORY_EXPORT;
    String[] key = TDPrefKey.EXPORT;
    int[] tit    = TDPrefKey.EXPORTtitle;
    int[] dsc    = TDPrefKey.EXPORTdesc;
    String[] def = TDPrefKey.EXPORTdef;
    int[] lvl    = TDPrefKey.EXPORTlvl;
    return new TDPref[ ] {
      makeLst( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], R.array.exportShots, R.array.exportShotsValue, ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], R.array.exportPlot, R.array.exportPlotValue, ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], R.array.exportPlotAuto, R.array.exportPlotAutoValue, ctx, hlp ), // DISTOX_AUTO_PLOT_EXPORT
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], FLOAT,  ctx, hlp ),
      makeFwd( cat, key[ 7], tit[ 7],          lvl[ 7],                  ctx, hlp ), // ENABLE
      makeFwd( cat, key[ 8], tit[ 8],          lvl[ 8],                  ctx, hlp ), // IMPORT
      makeFwd( cat, key[ 9], tit[ 9],          lvl[ 9],                  ctx, hlp ), // survex
      makeFwd( cat, key[10], tit[10],          lvl[10],                  ctx, hlp ), // therion
      makeFwd( cat, key[11], tit[11],          lvl[11],                  ctx, hlp ), // csurvey
      makeFwd( cat, key[12], tit[12],          lvl[12],                  ctx, hlp ), // compass
      makeFwd( cat, key[13], tit[13],          lvl[13],                  ctx, hlp ), // visualtopo
      makeFwd( cat, key[14], tit[14],          lvl[14],                  ctx, hlp ), // walls
      makeFwd( cat, key[15], tit[15],          lvl[15],                  ctx, hlp ), // svg
      makeFwd( cat, key[16], tit[16],          lvl[16],                  ctx, hlp ), // shapefile
      // makeFwd( cat, key[16], tit[16],       lvl[  ],                  ctx, hlp ), // png NO_PNG
      makeFwd( cat, key[17], tit[17],          lvl[17],                  ctx, hlp ), // dxf
      makeFwd( cat, key[18], tit[18],          lvl[18],                  ctx, hlp ), // kml
      makeFwd( cat, key[19], tit[19],          lvl[19],                  ctx, hlp ), // gpx
      makeFwd( cat, key[20], tit[20],          lvl[20],                  ctx, hlp )  // cvs
    };
  }

  /** construct the "export_enable" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "export" preferences
   */
  static TDPref[] makeExportEnablePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_EXPORT_ENABLE;
    String[] key = TDPrefKey.EXPORT_ENABLE;
    int[] tit = TDPrefKey.EXPORT_ENABLEtitle;
    // int[] dsc = TDPrefKey.EXPORT_ENABLEdesc;
    String[] def = TDPrefKey.EXPORT_ENABLEdef;
    int[] lvl = TDPrefKey.EXPORT_ENABLElvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], -1, lvl[ 0], def[ 0], ctx, hlp ), // compass
      makeCbx( cat, key[ 1], tit[ 1], -1, lvl[ 1], def[ 1], ctx, hlp ), // csurvey
      // makeCbx( cat, key[ 2], tit[ 2], -1, lvl[  ], def[ 2], ctx, hlp ), // ghtopo
      makeCbx( cat, key[ 2], tit[ 2], -1, lvl[ 2], def[ 2], ctx, hlp ), // polygon
      makeCbx( cat, key[ 3], tit[ 3], -1, lvl[ 3], def[ 3], ctx, hlp ), // survex
      makeCbx( cat, key[ 4], tit[ 4], -1, lvl[ 4], def[ 4], ctx, hlp ), // therion
      makeCbx( cat, key[ 5], tit[ 5], -1, lvl[ 5], def[ 5], ctx, hlp ), // topo
      makeCbx( cat, key[ 6], tit[ 6], -1, lvl[ 6], def[ 6], ctx, hlp ), // toporobot
      makeCbx( cat, key[ 7], tit[ 7], -1, lvl[ 7], def[ 7], ctx, hlp ), // visualtopo
      makeCbx( cat, key[ 8], tit[ 8], -1, lvl[ 8], def[ 8], ctx, hlp ), // walls
      makeCbx( cat, key[ 9], tit[ 9], -1, lvl[ 9], def[ 9], ctx, hlp ), // winkarst
      makeCbx( cat, key[10], tit[10], -1, lvl[10], def[10], ctx, hlp ), // csv
      makeCbx( cat, key[11], tit[11], -1, lvl[11], def[11], ctx, hlp ), // dxf
      makeCbx( cat, key[12], tit[12], -1, lvl[12], def[12], ctx, hlp ), // kml
      makeCbx( cat, key[13], tit[13], -1, lvl[13], def[13], ctx, hlp ), // gpx
      makeCbx( cat, key[14], tit[14], -1, lvl[14], def[14], ctx, hlp ), // shapefile
    };
  }

  /** construct the "import" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "import" preferences
   */
  static TDPref[] makeImportPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_IMPORT;
    String[] key = TDPrefKey.EXPORT_import;
    int[] tit = TDPrefKey.EXPORT_importtitle;
    int[] dsc = TDPrefKey.EXPORT_importdesc;
    String[] def = TDPrefKey.EXPORT_importdef;
    int[] lvl = TDPrefKey.EXPORT_importlvl;
    return new TDPref[ ] {
      makeBtn( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0],         ctx, hlp ),
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1],         ctx, hlp )
    };
  }

  /** construct the "geek import" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek import" preferences
   */
  static TDPref[] makeGeekImportPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_IMPORT;
    String[] key = TDPrefKey.GEEKIMPORT;
    int[] tit = TDPrefKey.GEEKIMPORTtitle;
    int[] dsc = TDPrefKey.GEEKIMPORTdesc;
    String[] def = TDPrefKey.GEEKIMPORTdef;
    int[] lvl = TDPrefKey.GEEKIMPORTlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0],         ctx, hlp ),
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], R.array.importDatamode, R.array.importDatamodeValue, ctx, hlp ),
      makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2],         ctx, hlp ), // DISTOX_AUTO_XSECTIONS
      makeCbx( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3],         ctx, hlp ), // DISTOX_AUTO_STATIONS
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4],         ctx, hlp ), // DISTOX_LRUD_COUNT
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5],         ctx, hlp ), // DISTOX_ZIP_SHARE_CATEGORY
      // makeLst( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ ], def[ 4], R.array.exportPlotAuto, R.array.exportPlotAutoValue, ctx, hlp ), // DISTOX_AUTO_PLOT_EXPORT
      // makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ ], def[ 2],         ctx, hlp ),
    };
  }

  /** construct the "shapefile" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "shapefile" preferences
   */
  static TDPref[] makeShpPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_SHP;
    String[] key = TDPrefKey.EXPORT_SHP;
    int[] tit = TDPrefKey.EXPORT_SHPtitle;
    int[] dsc = TDPrefKey.EXPORT_SHPdesc;
    String[] def = TDPrefKey.EXPORT_SHPdef;
    int[] lvl = TDPrefKey.EXPORT_SHPlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],         ctx, hlp )
    };
  }

  /** construct the "Survex" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "Survex" preferences
   */
  static TDPref[] makeSvxPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_SVX;
    String[] key = TDPrefKey.EXPORT_SVX;
    int[] tit = TDPrefKey.EXPORT_SVXtitle;
    int[] dsc = TDPrefKey.EXPORT_SVXdesc;
    String[] def = TDPrefKey.EXPORT_SVXdef;
    int[] lvl = TDPrefKey.EXPORT_SVXlvl;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], lvl[0], def[0], R.array.survexEol, R.array.survexEolValue, ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1],          ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2], def[2],          ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], lvl[3], def[3], INTEGER, ctx, hlp )
    };
  }

  /** construct the "Therion" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "Therion" preferences
   */
  static TDPref[] makeThPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_TH;
    String[] key = TDPrefKey.EXPORT_TH;
    int[] tit = TDPrefKey.EXPORT_THtitle;
    int[] dsc = TDPrefKey.EXPORT_THdesc;
    String[] def = TDPrefKey.EXPORT_THdef;
    int[] lvl = TDPrefKey.EXPORT_THlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0], ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1], ctx, hlp ),
      // makeCbx( cat, key[2], tit[2], dsc[2], lvl[ ], def[2], ctx, hlp ), // DISTOX_AUTO_STATIONS
      // makeCbx( cat, key[ ], tit[ ], dsc[ ], lvl[ ], def[ ], ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2] , def[2], ctx, hlp ),
      makeCbx( cat, key[3], tit[3], dsc[3], lvl[3], def[3], ctx, hlp ),
      // makeCbx( cat, key[4], tit[4], dsc[4], lvl[ ], def[4], ctx, hlp ),
      makeEdt( cat, key[4], tit[4], dsc[4], lvl[4], def[4], INTEGER,  ctx, hlp ),
      makeCbx( cat, key[5], tit[5], dsc[5], lvl[5], def[5], ctx, hlp )
    };
  }

  /** construct the "Compass" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "Compass" preferences
   */
  static TDPref[] makeDatPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_DAT;
    String[] key = TDPrefKey.EXPORT_DAT;
    int[] tit = TDPrefKey.EXPORT_DATtitle;
    int[] dsc = TDPrefKey.EXPORT_DATdesc;
    String[] def = TDPrefKey.EXPORT_DATdef;
    int[] lvl = TDPrefKey.EXPORT_DATlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0], ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1], ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2], def[2], ctx, hlp )
    };
  }

  /** construct the "Walls" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "Walls" preferences
   */
  static TDPref[] makeSrvPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_SRV;
    String[] key = TDPrefKey.EXPORT_SRV;
    int[] tit = TDPrefKey.EXPORT_SRVtitle;
    int[] dsc = TDPrefKey.EXPORT_SRVdesc;
    String[] def = TDPrefKey.EXPORT_SRVdef;
    int[] lvl = TDPrefKey.EXPORT_SRVlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0], ctx, hlp ),
    };
  }

  /** construct the "VisualTopo" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "VisualTopo" preferences
   */
  static TDPref[] makeTroPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_TRO;
    String[] key = TDPrefKey.EXPORT_TRO;
    int[] tit = TDPrefKey.EXPORT_TROtitle;
    int[] dsc = TDPrefKey.EXPORT_TROdesc;
    String[] def = TDPrefKey.EXPORT_TROdef;
    int[] lvl = TDPrefKey.EXPORT_TROlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0], ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1], ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2], def[2], ctx, hlp ) // DISTOX_VTOPO_TROX
    };
  }

  /** construct the "SVG" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "SVG" preferences
   */
  static TDPref[] makeSvgPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_SVG;
    String[] key = TDPrefKey.EXPORT_SVG;
    int[] tit = TDPrefKey.EXPORT_SVGtitle;
    int[] dsc = TDPrefKey.EXPORT_SVGdesc;
    String[] def = TDPrefKey.EXPORT_SVGdef;
    int[] lvl = TDPrefKey.EXPORT_SVGlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0],  def[ 0],         ctx, hlp ),
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1],  def[ 1],         ctx, hlp ),
      makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2],  def[ 2],         ctx, hlp ),
      makeCbx( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3],  def[ 3],         ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4],  def[ 4],         ctx, hlp ),
      // makeCbx( cat, key[ ], tit[ ], dsc[ ], lvl[  ],  def[  ],         ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5],  def[ 5], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6],  def[ 6], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7],  def[ 7], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8],  def[ 8], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9],  def[ 9], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[10], tit[10], dsc[10], lvl[10],  def[10], FLOAT,  ctx, hlp ),
      makeEdt( cat, key[11], tit[11], dsc[11], lvl[11],  def[11], INTEGER,  ctx, hlp ),
      makeEdt( cat, key[12], tit[12], dsc[12], lvl[12],  def[12], INTEGER,  ctx, hlp ),
      makeLst( cat, key[13], tit[13], dsc[13], lvl[13],  def[13], R.array.svgProgram, R.array.svgProgramValue, ctx, hlp )  // DISTOX_SVG_PROGRAM
    };
  }

  /** construct the "DXF" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "DXF" preferences
   */
  static TDPref[] makeDxfPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_DXF;
    String[] key = TDPrefKey.EXPORT_DXF;
    int[] tit = TDPrefKey.EXPORT_DXFtitle;
    int[] dsc = TDPrefKey.EXPORT_DXFdesc;
    String[] def = TDPrefKey.EXPORT_DXFdef;
    int[] lvl = TDPrefKey.EXPORT_DXFlvl;
    return new TDPref[ ] {
      // makeEdt( cat, key[ ]  tit[ ], dsc[ ], lvl[ ], def[ ], FLOAT,  ctx, hlp ),
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],         ctx, hlp ),
      makeLst( cat, key[1], tit[1], dsc[1], lvl[1], def[1], R.array.acadVersion, R.array.acadVersionValue, ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2], def[2],         ctx, hlp ),  // DISTOX_ACAD_SPLINE
      makeCbx( cat, key[3], tit[3], dsc[3], lvl[3], def[3],         ctx, hlp ),  // DISTOX_DXF_REFERENCE
      makeCbx( cat, key[4], tit[4], dsc[4], lvl[4], def[4],         ctx, hlp )   // DISTOX_ACAD_LAYER
      // makeCbx( cat, key[4], tit[4], dsc[4], lvl[ ], def[4],         ctx, hlp )   // DISTOX_AUTO_STATIONS
    };
  }

  // NO_PNG
  // /** construct the "PNG image" preferences array
  //  * @param ctx   context
  //  * @param hlp   shared preferences helper
  //  * @return array of "PNG image" preferences
  //  */
  // static TDPref[] makePngPrefs( Context ctx, TDPrefHelper hlp )
  // {
  //   int cat = TDPrefCat.PREF_CATEGORY_PNG;
  //   String[] key = TDPrefKey.EXPORT_PNG;
  //   int[] tit = TDPrefKey.EXPORT_PNGtitle;
  //   int[] dsc = TDPrefKey.EXPORT_PNGdesc;
  //   String[] def = TDPrefKey.EXPORT_PNGdef;
  //   return new TDPref[ ] {
  //     makeEdt( cat, key[0], tit[0], dsc[0], N, def[0], FLOAT,  ctx, hlp ),
  //     makeEdt( cat, key[1], tit[1], dsc[1], N, def[1], STRING, ctx, hlp ),
  //     makeCbx( cat, key[2], tit[2], dsc[2], N, def[2],         ctx, hlp ),  // DISTOX_SVG_GRID
  //     makeCbx( cat, key[3], tit[3], dsc[3], A, def[3],         ctx, hlp )   // DISTOX_THERION_SPLAYS
  //     // makeCbx( cat, key[4], tit[4], dsc[4], N, def[4],         ctx, hlp )   // DISTOX_AUTO_STATIONS
  //   };
  // }

  /** construct the "KML" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "KML" preferences
   */
  static TDPref[] makeKmlPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_KML;
    String[] key = TDPrefKey.EXPORT_KML;
    int[] tit = TDPrefKey.EXPORT_KMLtitle;
    int[] dsc = TDPrefKey.EXPORT_KMLdesc;
    String[] def = TDPrefKey.EXPORT_KMLdef;
    int[] lvl = TDPrefKey.EXPORT_KMLlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],         ctx, hlp ),
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1],         ctx, hlp )
    };
  }

  /** construct the "cSurvey" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "cSurvey" preferences
   */
  static TDPref[] makeCsxPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_CSX;
    String[] key = TDPrefKey.EXPORT_CSX;
    int[] tit = TDPrefKey.EXPORT_CSXtitle;
    int[] dsc = TDPrefKey.EXPORT_CSXdesc;
    String[] def = TDPrefKey.EXPORT_CSXdef;
    int[] lvl = TDPrefKey.EXPORT_CSXlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],         ctx, hlp ), // DISTOX_STATION_PREFIX
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1],         ctx, hlp )  // DISTOX_WITH_MEDIA
    };
  }

  /** construct the "GPX" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "GPX" preferences
   */
  static TDPref[] makeGpxPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_GPX;
    String[] key = TDPrefKey.EXPORT_GPX;
    int[] tit = TDPrefKey.EXPORT_GPXtitle;
    int[] dsc = TDPrefKey.EXPORT_GPXdesc;
    String[] def = TDPrefKey.EXPORT_GPXdef;
    int[] lvl = TDPrefKey.EXPORT_GPXlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],         ctx, hlp ) // DISTOX_GPX_SINGLE_TRACK
    };
  }

  /** construct the "CVS" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "CVS" preferences
   */
  static TDPref[] makeCsvPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_CATEGORY_CSV;
    String[] key = TDPrefKey.EXPORT_CSV;
    int[] tit = TDPrefKey.EXPORT_CSVtitle;
    int[] dsc = TDPrefKey.EXPORT_CSVdesc;
    String[] def = TDPrefKey.EXPORT_CSVdef;
    int[] lvl = TDPrefKey.EXPORT_CSVlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],         ctx, hlp ), // DISTOX_CSV_RAW
      makeLst( cat, key[1], tit[1], dsc[1], lvl[1], def[1], R.array.csvSeparator, R.array.csvSeparatorValue, ctx, hlp ), // DISTOX_CSV_SEP
      makeLst( cat, key[2], tit[2], dsc[2], lvl[2], def[2], R.array.survexEol, R.array.survexEolValue, ctx, hlp ) // DISTOX_SURVEX_EOL
    };
  }

  /** construct the "shot data" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "shot data" preferences
   */
  static TDPref[] makeShotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_SHOT_DATA;
    String[] key = TDPrefKey.DATA;
    int[] tit = TDPrefKey.DATAtitle;
    int[] dsc = TDPrefKey.DATAdesc;
    String[] def = TDPrefKey.DATAdef;
    int[] lvl = TDPrefKey.DATAlvl;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], FLOAT,   ctx, hlp ), // CLOSE_DISTANCE
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], FLOAT,   ctx, hlp ), // MAX_SHOT_LENGTH
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT,   ctx, hlp ), // MIN_LEG_LENGTH
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], R.array.legShots, R.array.legShotsValue, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], FLOAT,   ctx, hlp ), // EXTEND_THRS
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], FLOAT,   ctx, hlp ),
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6],          ctx, hlp ), // AZIMUTH_MANUAL
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],          ctx, hlp ), // PREV_NEXT
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ), // BACKSIGHT
      makeLst( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9], R.array.feedbackMode, R.array.feedbackModeValue, ctx, hlp ) // DISTOX_LEG_FEEDBACK
      // makeEdt( cat, key[10], tit[10], dsc[10], lvl[  ], def[10], INTEGER, ctx, hlp ), // TIMER
      // makeEdt( cat, key[11], tit[11], dsc[11], lvl[  ], def[11], INTEGER, ctx, hlp ), // VOLUME
    };
  }

  /** construct the "units" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "units" preferences
   */
  static TDPref[] makeUnitsPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_SHOT_UNITS;
    String[] key = TDPrefKey.UNITS;
    int[] tit = TDPrefKey.UNITStitle;
    int[] dsc = TDPrefKey.UNITSdesc;
    String[] def = TDPrefKey.UNITSdef;
    int[] arr = TDPrefKey.UNITSarr;
    int[] val = TDPrefKey.UNITSval;
    int[] lvl = TDPrefKey.UNITSlvl;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], lvl[0], def[0], arr[0], val[0], ctx, hlp ), // LENGTH
      makeLst( cat, key[1], tit[1], dsc[1], lvl[1], def[1], arr[1], val[1], ctx, hlp ), // ANGLE
      makeLst( cat, key[2], tit[2], dsc[2], lvl[2], def[2], arr[2], val[2], ctx, hlp ), // GRID
      makeLst( cat, key[3], tit[3], dsc[3], lvl[3], def[3], arr[3], val[3], ctx, hlp )  // MEASURE
    };
  }

  /** construct the "accuracy" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "accuracy" preferences
   */
  static TDPref[] makeAccuracyPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_ACCURACY;
    String[] key = TDPrefKey.ACCURACY;
    int[] tit = TDPrefKey.ACCURACYtitle;
    int[] dsc = TDPrefKey.ACCURACYdesc;
    String[] def = TDPrefKey.ACCURACYdef;
    int[] lvl = TDPrefKey.ACCURACYlvl;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], lvl[0], def[0], FLOAT, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], FLOAT, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], lvl[2], def[2], FLOAT, ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], lvl[3], def[3], FLOAT, ctx, hlp )
    };
  }

  /** construct the "location" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "location" preferences
   */
  static TDPref[] makeLocationPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_LOCATION;
    String[] key = TDPrefKey.LOCATION;
    int[] tit = TDPrefKey.LOCATIONtitle;
    int[] dsc = TDPrefKey.LOCATIONdesc;
    String[] def = TDPrefKey.LOCATIONdef;
    int[] lvl = TDPrefKey.LOCATIONlvl;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], lvl[0], def[0], R.array.unitLocation, R.array.unitLocationValue, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], STRING, ctx, hlp ),
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2], def[2],         ctx, hlp ),
      makeCbx( cat, key[3], tit[3], dsc[3], lvl[3], def[3],         ctx, hlp ),
      makeEdt( cat, key[4], tit[4], dsc[4], lvl[4], def[4], INTEGER, ctx, hlp ),
      makeLst( cat, key[5], tit[5], dsc[5], lvl[5], def[5], R.array.geoImportApp, R.array.geoImportAppValue, ctx, hlp )
    };
  }

  /** construct the "sketch display" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "sketch display" preferences
   */
  static TDPref[] makeScreenPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_PLOT_SCREEN;
    String[] key = TDPrefKey.SCREEN;
    int[] tit = TDPrefKey.SCREENtitle;
    int[] dsc = TDPrefKey.SCREENdesc;
    String[] def = TDPrefKey.SCREENdef;
    int[] lvl = TDPrefKey.SCREENlvl;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7], INTEGER, ctx, hlp )  // SPLAY ALPHA
    };
  }

  /** construct the "line items" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "line items" preferences
   */
  static TDPref[] makeLinePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_TOOL_LINE;
    String[] key = TDPrefKey.LINE;
    int[] tit    = TDPrefKey.LINEtitle;
    int[] dsc    = TDPrefKey.LINEdesc;
    String[] def = TDPrefKey.LINEdef;
    int[] lvl = TDPrefKey.LINElvl;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], FLOAT,   ctx, hlp ),
      makeLst( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], R.array.lineStyle, R.array.lineStyleValue, ctx, hlp ),
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4],          ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], INTEGER, ctx, hlp ), // DISTOX_SLOPE_LSIDE
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], FLOAT,   ctx, hlp ),
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],          ctx, hlp ),
      // makeLst( cat, key[ 7], tit[ 7], dsc[ 7], lvl[  ], def[ 7], R.array.lineContinue, R.array.lineContinueValue, ctx, hlp ),
      // makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ), // WITH CONTINUE LINE
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ),
    };
  }

  /** construct the "point items" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "point items" preferences
   */
  static TDPref[] makePointPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_TOOL_POINT;
    String[] key = TDPrefKey.POINT;
    int[] tit    = TDPrefKey.POINTtitle;
    int[] dsc    = TDPrefKey.POINTdesc;
    String[] def = TDPrefKey.POINTdef;
    int[] lvl    = TDPrefKey.POINTlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],        ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], FLOAT, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], lvl[2], def[2], FLOAT, ctx, hlp ),
      makeCbx( cat, key[3], tit[3], dsc[3], lvl[3], def[3],        ctx, hlp ),
      makeCbx( cat, key[4], tit[4], dsc[4], lvl[4], def[4],        ctx, hlp )
    };
  }

  // AUTOWALLS
  // static TDPref[] makeWallsPrefs( Context ctx, TDPrefHelper hlp )
  // {
  //   int cat = TDPrefCat.PREF_PLOT_WALLS;
  //   String[] key = TDPrefKey.WALLS;
  //   int[] tit    = TDPrefKey.WALLStitle;
  //   int[] dsc    = TDPrefKey.WALLSdesc;
  //   String[] def = TDPrefKey.WALLSdef;
  //   return new TDPref[ ] {
  //     makeLst( cat, key[0], tit[0],  dsc[0], T, def[0], R.array.wallsType, R.array.wallsTypeValue, ctx, hlp ),
  //     makeEdt( cat, key[1], tit[1],  dsc[1], T, def[1], INTEGER, ctx, hlp ),
  //     makeEdt( cat, key[2], tit[2],  dsc[2], T, def[2], INTEGER, ctx, hlp ),
  //     makeEdt( cat, key[3], tit[3],  dsc[3], T, def[3], FLOAT,   ctx, hlp ),
  //     makeEdt( cat, key[4], tit[4],  dsc[4], T, def[4], FLOAT,   ctx, hlp ),
  //     makeEdt( cat, key[5], tit[5],  dsc[5], T, def[5], FLOAT,   ctx, hlp )
  //   };
  // }

  /** construct the "sketch drawing" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "sketch drawing" preferences
   */
  static TDPref[] makeDrawPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_PLOT_DRAW;
    String[] key = TDPrefKey.DRAW;
    int[] tit    = TDPrefKey.DRAWtitle;
    int[] dsc    = TDPrefKey.DRAWdesc;
    String[] def = TDPrefKey.DRAWdef;
    int[] lvl    = TDPrefKey.DRAWlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0],          ctx, hlp ), // DISTOX_UNSCALED_POINTS  point
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], FLOAT,   ctx, hlp ), // DISTOX_DRAWING_UNITS
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT,   ctx, hlp ), // DISTOX_LABEL_SIZE
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], FLOAT,   ctx, hlp ), // DISTOX_LINE_THICKNESS line
      makeLst( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], R.array.lineStyle, R.array.lineStyleValue, ctx, hlp ),
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5],          ctx, hlp ), // DISTOX_LINE_CLOSE
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], INTEGER, ctx, hlp ), // DISTOX_LINE_SEGMENT
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7], FLOAT,   ctx, hlp ), // DISTOX_ARROW_LENGTH
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ), // DISTOX_AUTO_SECTION_PT
      // makeLst( cat, key[ 8], tit[ 8], dsc[ 8], lvl[  ], def[ 8], R.array.lineContinue, R.array.lineContinueValue, ctx, hlp ),
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9],          ctx, hlp ), // DISTOX_AREA_BORDER
      // makeEdt( cat, key[10], tit[10], dsc[10], lvl[  ], def[10], FLOAT,   ctx, hlp ),
      // makeEdt( cat, key[11], tit[11], dsc[11], lvl[  ], def[11], FLOAT,   ctx, hlp ),
      // makeEdt( cat, key[12], tit[12], dsc[12], lvl[  ], def[12], FLOAT,   ctx, hlp ) 
    };
  }

  /** construct the "sketch erasing" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "sketch erasing" preferences
   */
  static TDPref[] makeErasePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_PLOT_ERASE;
    String[] key = TDPrefKey.ERASE;
    int[] tit    = TDPrefKey.ERASEtitle;
    int[] dsc    = TDPrefKey.ERASEdesc;
    String[] def = TDPrefKey.ERASEdef;
    int[] lvl    = TDPrefKey.ERASElvl;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], lvl[0], def[0], INTEGER, ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], INTEGER, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], lvl[2], def[2], INTEGER, ctx, hlp )
    };
  }

  /** construct the "sketch editing" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "sketch editing" preferences
   */
  static TDPref[] makeEditPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_PLOT_EDIT;
    String[] key = TDPrefKey.EDIT;
    int[] tit    = TDPrefKey.EDITtitle;
    int[] dsc    = TDPrefKey.EDITdesc;
    String[] def = TDPrefKey.EDITdef;
    int[] lvl    = TDPrefKey.EDITlvl;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], lvl[0], def[0], FLOAT,   ctx, hlp ),
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], INTEGER, ctx, hlp ),
      makeEdt( cat, key[2], tit[2], dsc[2], lvl[2], def[2], INTEGER, ctx, hlp ),
      makeEdt( cat, key[3], tit[3], dsc[3], lvl[3], def[3], INTEGER, ctx, hlp )
    };
  }


  /** construct the "geek device" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek device" preferences
   */
  static TDPref[] makeGeekDevicePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_DEVICE;
    String[] key = TDPrefKey.GEEKDEVICE;
    int[] tit    = TDPrefKey.GEEKDEVICEtitle;
    int[] dsc    = TDPrefKey.GEEKDEVICEdesc;
    String[] def = TDPrefKey.GEEKDEVICEdef;
    int[] lvl    = TDPrefKey.GEEKDEVICElvl;
    return new TDPref[ ] {
      makeBtn( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], ctx, hlp ),          // BT ALIAS
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1],          ctx, hlp ), // BT_NONAME (change next line too)
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3],          ctx, hlp ), // SECOND DISTOX
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], INTEGER, ctx, hlp ),
      makeEdt( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7], INTEGER, ctx, hlp ),
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ), // FIRMWARE SANITY
      makeLst( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9], R.array.bricMode, R.array.bricModeValue, ctx, hlp ), // DISTOX_BRIC_MODE
      makeCbx( cat, key[10], tit[10], dsc[10], lvl[10], def[10],          ctx, hlp ), // DISTOX_BRIC_ZERO_LENGTH
      makeCbx( cat, key[11], tit[11], dsc[11], lvl[11], def[11],          ctx, hlp ), // DISTOX_CBRIC_INDEX_IS_ID
      makeCbx( cat, key[12], tit[12], dsc[12], lvl[12], def[12],          ctx, hlp ), // DISTOX_SAP5_BIT16_BUG
    };
  }

  /** construct the "geek line item" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek line item" preferences
   */
  static TDPref[] makeGeekLinePrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_LINE;
    String[] key = TDPrefKey.GEEKLINE;
    int[] tit    = TDPrefKey.GEEKLINEtitle;
    int[] dsc    = TDPrefKey.GEEKLINEdesc;
    String[] def = TDPrefKey.GEEKLINEdef;
    int[] lvl    = TDPrefKey.GEEKLINElvl;
    return new TDPref[ ] {
      makeEdt( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0], FLOAT,   ctx, hlp ), // REDUCE ANGLE
      makeEdt( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], FLOAT,   ctx, hlp ), // BEZIER ACCURACY
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT,   ctx, hlp ), //        CORNER
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], FLOAT,   ctx, hlp ), // WEED DISTANCE
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], FLOAT,   ctx, hlp ), //      LENGTH
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], FLOAT,   ctx, hlp ), //      BUFFER
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6],          ctx, hlp ), // SNAP
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],          ctx, hlp ), // CURVE
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ), // STRAIGHT
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9],          ctx, hlp ), // PATH MULTISELECT
      // makeCbx( cat, key[10], tit[10], dsc[10], lvl[  ], def[10],          ctx, hlp ), // COMPOSITE ACTIONS
    };
  }

  /** construct the "geek shot data" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek shot data" preferences
   */
  static TDPref[] makeGeekShotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_SHOT;
    String[] key = TDPrefKey.GEEKSHOT;
    int[] tit    = TDPrefKey.GEEKSHOTtitle;
    int[] dsc    = TDPrefKey.GEEKSHOTdesc;
    String[] def = TDPrefKey.GEEKSHOTdef;
    int[] lvl    = TDPrefKey.GEEKSHOTlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0],          ctx, hlp ), // DIVING_MODE
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1],          ctx, hlp ), // TAMPERING
      makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2],          ctx, hlp ), // BACKSIGHT_SPLAY
      makeCbx( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3],          ctx, hlp ), // RECENT_SHOT
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], INTEGER, ctx, hlp ), // RECENT TIMEOUT
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5],          ctx, hlp ), // EXTEND FRACTIONAL
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6],          ctx, hlp ), // BACKSHOT
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],          ctx, hlp ), // BEDDING PLANE
      makeCbx( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],          ctx, hlp ), // WITH SENSORS
      makeLst( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9], R.array.loopClosure, R.array.loopClosureValue, ctx, hlp ),
      makeEdt( cat, key[10], tit[10], dsc[10], lvl[10], def[10], FLOAT,   ctx, hlp ), // LOOP THR
      // makeEdt( cat, key[ 9], tit[ 9], dsc[ 9], lvl[  ], def[ 9], FLOAT,   ctx, hlp ), // DIST/ANGLE TOLERANCE
      // makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], lvl[  ], def[ 9],          ctx, hlp )  // SPLAYS AT ACTIVE STATION
      // makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], lvl[  ], def[ 9],          ctx, hlp )  // WITH RENAME
      makeCbx( cat, key[11], tit[11], dsc[11], lvl[11], def[11],          ctx, hlp ),// WITH ANDROID AZIMUTH
      makeEdt( cat, key[12], tit[12], dsc[12], lvl[12], def[12], INTEGER, ctx, hlp ), // TIMER
      makeEdt( cat, key[13], tit[13], dsc[13], lvl[13], def[13], INTEGER, ctx, hlp ), // VOLUME
      makeCbx( cat, key[14], tit[14], dsc[14], lvl[14], def[14],          ctx, hlp ), // BLUNDER SHOT
      makeCbx( cat, key[15], tit[15], dsc[15], lvl[15], def[15],          ctx, hlp ), // SPLAY STATION
      makeCbx( cat, key[16], tit[16], dsc[16], lvl[16], def[16],          ctx, hlp ), // SPLAY GROUP
      // makeCbx( cat, key[13], tit[13], dsc[13], lvl[  ], def[13],          ctx, hlp )  // TDMANAGER
    };
  }

  /** construct the "geek sketch" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek sketch" preferences
   */
  static TDPref[] makeGeekPlotPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_PLOT;
    String[] key = TDPrefKey.GEEKPLOT;
    int[] tit    = TDPrefKey.GEEKPLOTtitle;
    int[] dsc    = TDPrefKey.GEEKPLOTdesc;
    String[] def = TDPrefKey.GEEKPLOTdef;
    int[] lvl    = TDPrefKey.GEEKPLOTlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0],          ctx, hlp ), // PLOT_SHIFT
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1],          ctx, hlp ), // PLOT_SPLIT_MERGE
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], FLOAT,   ctx, hlp ), // STYLUS_SIZE // STYLUS_MM
      // makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[  ], def[ 2], INTEGER, ctx, hlp ), // STYLUS_SIZE
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], R.array.backupNumber, R.array.backupNumberValue, ctx, hlp ),
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], INTEGER, ctx, hlp ), // BACKUP_INTERVAL
      // makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], lvl[  ], def[ 9],          ctx, hlp ), // BACKUPS_CLEAR
      // makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], lvl[  ], def[ 5],          ctx, hlp ), // AUTO_XSECTIONS on export/save
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5],          ctx, hlp ), // SAVED_STATIONS
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6],          ctx, hlp ), // ALWAYS_UPDATE
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],          ctx, hlp ), // FULL_AFFINE
      makeLst( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8], R.array.canvasLevels, R.array.canvasLevelsValue, ctx, hlp ),  // WITH LEVELS
      makeBtn( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9], ctx, hlp ), // GRAPH_PAPER_SCALE
      makeCbx( cat, key[10], tit[10], dsc[10], lvl[10], def[10],          ctx, hlp ), // SLANT_XSECTION
      makeEdt( cat, key[11], tit[11], dsc[11], lvl[11], def[11], INTEGER, ctx, hlp ), // OBLIQUE_MAX
      makeEdt( cat, key[12], tit[12], dsc[12], lvl[12], def[12], INTEGER, ctx, hlp ), // LINE ENDS (POINTS)
      // makeEdt( cat, key[13], tit[13], dsc[13], lvl[13], def[13], FLOAT,   ctx, hlp ), // ZOOM LOWER BOUND
    };
  }

  /** construct the "geek splays" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek splays" preferences
   */
  static TDPref[] makeGeekSplayPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_SPLAY;
    String[] key = TDPrefKey.GEEKsplay;
    int[] tit    = TDPrefKey.GEEKsplaytitle;
    int[] dsc    = TDPrefKey.GEEKsplaydesc;
    String[] def = TDPrefKey.GEEKsplaydef;
    int[] lvl    = TDPrefKey.GEEKsplaylvl;
    return new TDPref[ ] {
      makeCbx( cat, key[ 0], tit[ 0], dsc[ 0], lvl[ 0], def[ 0],          ctx, hlp ), // SPLAY CLASSES
      // makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1],          ctx, hlp ), // SPLAY COLOR
      makeLst( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1], R.array.splayColors, R.array.splayColorsValue,   ctx, hlp ), // DISCRETE COLORS
      // makeCbx( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2],          ctx, hlp ), // SPLAY AS DOT
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], INTEGER, ctx, hlp ), // MAX CLINO SPLAY-PLAN
      makeLst( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], R.array.splayDash, R.array.splayDashValue,       ctx, hlp ), // DASH COHERENCE
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], FLOAT,   ctx, hlp ), // DASH PLAN
      makeEdt( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5], FLOAT,   ctx, hlp ), // DASH PROFILE
      makeEdt( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6], FLOAT,   ctx, hlp ), // DASH X-SECTION
      makeColor( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],        ctx, hlp ), // DASH COLOR SPLAY 
      makeColor( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8],        ctx, hlp ), // DOT COLOR SPLAY 
      makeColor( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 9],        ctx, hlp ), // LATEST COLOR SPLAY 
    };
  }

  /** construct the "geek main" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "geek main" preferences
   */
  static TDPref[] makeGeekPrefs( Context ctx, TDPrefHelper hlp )
  {
    // TDLog.v("make Geek Prefs");
    int cat = TDPrefCat.PREF_CATEGORY_GEEK;
    String[] key = TDPrefKey.GEEK;
    int[] tit    = TDPrefKey.GEEKtitle;
    int[] dsc    = TDPrefKey.GEEKdesc;
    String[] def = TDPrefKey.GEEKdef;
    int[] lvl    = TDPrefKey.GEEKlvl;
    if ( TDLevel.isDebugBuild( ) ) {
      // TDLog.v("Length " + key.length + " " + tit.length + " " + dsc.length + " " + def.length );
      return new TDPref[ ] {
        makeCbx( cat, key[0], tit[0], dsc[0],  lvl[0], def[0],  ctx, hlp ), // SINGLE_BACK
        makeCbx( cat, key[1], tit[1], dsc[1],  lvl[1], def[1],  ctx, hlp ), // NAV_BAR
        makeCbx( cat, key[2], tit[2], dsc[2],  lvl[2], def[2],  ctx, hlp ), // PALETTES
        // makeCbx( cat, key[1], tit[1], dsc[1],  lvl[1], def[1],  ctx, hlp ), // BACKUP CLEAR - CLEAR_BACKUPS
        makeCbx( cat, key[3], tit[3], dsc[3],  lvl[3], def[3],  ctx, hlp ), // KEYBOARD
        makeCbx( cat, key[4], tit[4], dsc[4],  lvl[4], def[4],  ctx, hlp ), // CURSOR
        makeCbx( cat, key[5], tit[5], dsc[5],  lvl[5], def[5],  ctx, hlp ), // PACKET LOGGER
        makeCbx( cat, key[6], tit[6], dsc[6],  lvl[6], def[6],  ctx, hlp ), // TH2EDIT
        makeFwd( cat, key[7], tit[7],          lvl[7],          ctx, hlp ), // GEEK_SHOT
        makeFwd( cat, key[8], tit[8],          lvl[8],          ctx, hlp ), // GEEK_SPLAY
        makeFwd( cat, key[9], tit[9],          lvl[9],          ctx, hlp ), // GEEK_PLOT
        makeFwd( cat, key[10], tit[10],          lvl[10],          ctx, hlp ), // GEEK_LINE
        // makeFwd( cat, key[7], tit[7],       lvl[ ],          ctx, hlp ), // PLOT_WALLS AUTOWALLS
        makeFwd( cat, key[11], tit[11],        lvl[11],          ctx, hlp ), // GEEK_DEVICE
        makeFwd( cat, key[12], tit[12],        lvl[12],          ctx, hlp ), // GEEK_IMPORT
        makeFwd( cat, key[13], tit[13],        lvl[13],          ctx, hlp ), // SKETCH // FIXME_SKETCH_3D FIXME_FIXME
        makeSpecial( cat, key[14], tit[14], dsc[14],  lvl[14], def[14],  ctx, hlp ), // WITH DEBUG
      };
    } else {
      return new TDPref[ ] {
        makeCbx( cat, key[0], tit[0], dsc[0],  lvl[0], def[0],  ctx, hlp ), // SINGLE_BACK
        makeCbx( cat, key[1], tit[1], dsc[1],  lvl[1], def[1],  ctx, hlp ), // PALETTES
        // makeCbx( cat, key[1], tit[1], dsc[1],  lvl[1], def[1],  ctx, hlp ), // BACKUP CLEAR - CLEAR_BACKUPS
        makeCbx( cat, key[2], tit[2], dsc[2],  lvl[2], def[2],  ctx, hlp ), // KEUBOARD
        makeCbx( cat, key[3], tit[3], dsc[3],  lvl[3], def[3],  ctx, hlp ), // CURSOR
        makeCbx( cat, key[3], tit[4], dsc[4],  lvl[4], def[4],  ctx, hlp ), // PACKET LOGGER
        makeCbx( cat, key[5], tit[5], dsc[5],  lvl[5], def[5],  ctx, hlp ), // TH2EDIT
        makeFwd( cat, key[6], tit[6],          lvl[6],          ctx, hlp ), // GEEK_SHOT
        makeFwd( cat, key[7], tit[7],          lvl[7],          ctx, hlp ), // GEEK_SPLAY
        makeFwd( cat, key[8], tit[8],          lvl[8],          ctx, hlp ), // GEEK_PLOT
        makeFwd( cat, key[9], tit[9],          lvl[9],          ctx, hlp ), // GEEK_LINE
        // makeFwd( cat, key[7], tit[7],       lvl[ ],          ctx, hlp ), // PLOT_WALLS AUTOWALLS
        makeFwd( cat, key[10], tit[10],        lvl[10],          ctx, hlp ), // GEEK_DEVICE
        makeFwd( cat, key[11], tit[11],        lvl[11],          ctx, hlp ), // GEEK_IMPORT
        makeFwd( cat, key[12], tit[12],        lvl[12],          ctx, hlp ), // SKETCH // FIXME_SKETCH_3D FIXME_FIXME
      };
    }
  }

  /** construct the "3D viewer" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "3D viewer" preferences
   */
  static TDPref[] makeCave3DPrefs( Context ctx, TDPrefHelper hlp )
  {
    // TDLog.v("make Cave3D Prefs");
    int cat = TDPrefCat.PREF_CATEGORY_CAVE3D;
    String[] key = TDPrefKey.CAVE3D;
    int[] tit    = TDPrefKey.CAVE3Dtitle;
    int[] dsc    = TDPrefKey.CAVE3Ddesc;
    String[] def = TDPrefKey.CAVE3Ddef;
    int[] lvl    = TDPrefKey.CAVE3Dlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],  ctx, hlp ), // NEG-CLINO
      // BT DEVICE
      makeCbx( cat, key[ 1], tit[ 1], dsc[ 1], lvl[ 1], def[ 1],            ctx, hlp ), // STATION-POINT SUMMARY
      makeEdt( cat, key[ 2], tit[ 2], dsc[ 2], lvl[ 2], def[ 2], INTEGER,   ctx, hlp ), // STATION POINT SIZE
      makeEdt( cat, key[ 3], tit[ 3], dsc[ 3], lvl[ 3], def[ 3], INTEGER,   ctx, hlp ), // STATION TEXT SIZE
      makeEdt( cat, key[ 4], tit[ 4], dsc[ 4], lvl[ 4], def[ 4], FLOAT,     ctx, hlp ), // SELECT RADIUS
      makeCbx( cat, key[ 5], tit[ 5], dsc[ 5], lvl[ 5], def[ 5],            ctx, hlp ), // MEASURE DIALOG
      makeCbx( cat, key[ 6], tit[ 6], dsc[ 6], lvl[ 6], def[ 6],            ctx, hlp ), // STATION TOAST 
      makeCbx( cat, key[ 7], tit[ 7], dsc[ 7], lvl[ 7], def[ 7],            ctx, hlp ), // GRID ABOVE
      makeEdt( cat, key[ 8], tit[ 8], dsc[ 8], lvl[ 8], def[ 8], INTEGER,   ctx, hlp ), // GRID SIZE
      makeCbx( cat, key[ 9], tit[ 9], dsc[ 9], lvl[ 9], def[ 7],            ctx, hlp ), // NAMES VISIBILITY
      makeFwd( cat, key[10], tit[10],          lvl[10],                     ctx, hlp ), // DEM3D
      makeFwd( cat, key[11], tit[11],          lvl[11],                     ctx, hlp )  // WALLS3D
    };
  }

  /** construct the "3D DEM" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "3D DEM" preferences
   */
  static TDPref[] makeDem3DPrefs( Context ctx, TDPrefHelper hlp )
  {
    // TDLog.v("make Cave3D Prefs");
    int cat = TDPrefCat.PREF_DEM3D;
    String[] key = TDPrefKey.DEM3D;
    int[] tit    = TDPrefKey.DEM3Dtitle;
    int[] dsc    = TDPrefKey.DEM3Ddesc;
    String[] def = TDPrefKey.DEM3Ddef;
    int[] lvl    = TDPrefKey.DEM3Dlvl;
    return new TDPref[ ] {
      makeEdt( cat, key[0], tit[0], dsc[0], lvl[0], def[0], FLOAT,   ctx, hlp ), // CAVE3D_DEM_BUFFER
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], INTEGER, ctx, hlp ), // CAVE3D_DEM_MAXSIZE
      makeLst( cat, key[2], tit[2], dsc[2], lvl[2], def[2], R.array.demReduce, R.array.demReduceValue,  ctx, hlp ),// CAVE3D_DEM_REDUCE
      makeEdt( cat, key[3], tit[3], dsc[3], lvl[3], def[3], STRING,  ctx, hlp )  // CAVE3D_TEXTURE_ROOT
    };
  }

  /** construct the "3D walls" preferences array
   * @param ctx   context
   * @param hlp   shared preferences helper
   * @return array of "3D walls" preferences
   */
  static TDPref[] makeWalls3DPrefs( Context ctx, TDPrefHelper hlp )
  {
    // TDLog.v("make Cave3D Prefs");
    int cat = TDPrefCat.PREF_WALLS3D;
    String[] key = TDPrefKey.WALLS3D;
    int[] tit    = TDPrefKey.WALLS3Dtitle;
    int[] dsc    = TDPrefKey.WALLS3Ddesc;
    String[] def = TDPrefKey.WALLS3Ddef;
    int[] lvl    = TDPrefKey.WALLS3Dlvl;
    return new TDPref[ ] {
      makeLst( cat, key[0], tit[0], dsc[0], lvl[0], def[0], R.array.splayUse, R.array.splayUseValue,       ctx, hlp ), // CAVE3D_SPLAY_USE
      makeCbx( cat, key[1], tit[1], dsc[1], lvl[1], def[1],          ctx, hlp ), // CAVE3D_ALL_SPLAY
      makeCbx( cat, key[2], tit[2], dsc[2], lvl[2], def[2],          ctx, hlp ), // CAVE3D_SPLAY_PROJ
      makeEdt( cat, key[3], tit[3], dsc[3], lvl[3], def[3], FLOAT,   ctx, hlp ), // CAVE3D_SPLAY_THR
      makeCbx( cat, key[4], tit[4], dsc[4], lvl[4], def[4],          ctx, hlp ), // CAVE3D_SPLIT_TRIANGLES
      makeEdt( cat, key[5], tit[5], dsc[5], lvl[5], def[5], FLOAT,   ctx, hlp ), // CAVE3D_SPLIT_RANDOM
      makeEdt( cat, key[6], tit[6], dsc[6], lvl[6], def[6], FLOAT,   ctx, hlp ), // CAVE3D_SPLIT_STRETCH
      makeEdt( cat, key[7], tit[7], dsc[7], lvl[7], def[7], FLOAT,   ctx, hlp ), // CAVE3D_POWERCRUST_DELTA
    };
  }

  static TDPref[] makeSketchPrefs( Context ctx, TDPrefHelper hlp )
  {
    int cat = TDPrefCat.PREF_GEEK_SKETCH;
    String[] key = TDPrefKey.SKETCH;
    int[] tit    = TDPrefKey.SKETCHtitle;
    int[] dsc    = TDPrefKey.SKETCHdesc;
    String[] def = TDPrefKey.SKETCHdef;
    int[] lvl    = TDPrefKey.SKETCHlvl;
    return new TDPref[ ] {
      makeCbx( cat, key[0], tit[0], dsc[0], lvl[0], def[0],          ctx, hlp ), // DISTOX_3D_SKETCH
      makeEdt( cat, key[1], tit[1], dsc[1], lvl[1], def[1], FLOAT,   ctx, hlp ),
    };
  }

  // NO_LOGS
  // /** construct the "logging" preferences array
  //  * @param ctx   context
  //  * @param hlp   shared preferences helper
  //  * @return array of "logging" preferences
  //  */
  // static TDPref[] makeLogPrefs( Context ctx, TDPrefHelper hlp )
  // {
  //   int cat = TDPrefCat.PREF_CATEGORY_LOG;
  //   String[] key = TDPrefKey.LOG;
  //   int[] tit    = TDPrefKey.LOGtitle;
  //   return new TDPref[ ] {
  //     makeLst( cat, key[0], R.string.pref_log_stream_title, R.string.pref_log_stream_summary, T, "0", R.array.logStream, R.array.logStreamValue, ctx, hlp ),
  //     makeCbx( cat, key[ 1], tit[ 1], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 2], tit[ 2], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 3], tit[ 3], -1, E, true,  ctx, hlp ),
  //     makeCbx( cat, key[ 4], tit[ 4], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 5], tit[ 5], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 6], tit[ 6], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 7], tit[ 7], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 8], tit[ 8], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[ 9], tit[ 9], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[10], tit[10], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[11], tit[11], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[12], tit[12], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[13], tit[13], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[14], tit[14], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[15], tit[15], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[16], tit[16], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[17], tit[17], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[18], tit[18], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[19], tit[19], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[20], tit[20], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[21], tit[21], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[22], tit[22], -1, T, false, ctx, hlp ),
  //     makeCbx( cat, key[23], tit[23], -1, T, false, ctx, hlp ),
  //     makeCbx( cat, key[24], tit[24], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[25], tit[25], -1, T, false, ctx, hlp ),
  //     makeCbx( cat, key[26], tit[26], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[27], tit[27], -1, T, false, ctx, hlp ),
  //     makeCbx( cat, key[28], tit[28], -1, T, false, ctx, hlp ),
  //     makeCbx( cat, key[29], tit[29], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[30], tit[30], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[31], tit[31], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[32], tit[32], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[33], tit[33], -1, E, false, ctx, hlp ),
  //     makeCbx( cat, key[34], tit[34], -1, E, false, ctx, hlp ) 
  //     // makeCbx( cat, key[34], tit[34], -1, E, false, ctx, hlp )  // DISTOX_LOG_SYNC
  //   };
  // }

}
