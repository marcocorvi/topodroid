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

public class TDPref implements AdapterView.OnItemSelectedListener
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

  private static TDPref makePref( TDPrefKey k, Context ctx, TDPrefHelper hlp )
  {
    switch ( k.type ) {
      case TDPrefKey.LONG:
        return makeEdt( k.cat, k.key, k.title, k.summary, k.level, k.dflt, INTEGER, ctx, hlp );
      case TDPrefKey.BOOL:
        return makeCbx(  k.cat, k.key, k.title, k.summary, k.level, k.dflt, ctx, hlp );
      case TDPrefKey.FLT:
        return makeEdt( k.cat, k.key, k.title, k.summary, k.level, k.dflt, FLOAT, ctx, hlp );
      case TDPrefKey.STR:
        return makeEdt( k.cat, k.key, k.title, k.summary, k.level, k.dflt, STRING, ctx, hlp );
      case TDPrefKey.ARR:
        return makeLst( k.cat, k.key, k.title, k.summary, k.level, k.dflt, k.label, k.value, ctx, hlp );
      case TDPrefKey.BTN:
        return makeBtn( k.cat, k.key, k.title, k.summary, k.level, k.dflt, ctx, hlp );
      case TDPrefKey.XTR:
        return makeSpecial( k.cat, k.key, k.title, k.summary, k.level, k.dflt, ctx, hlp );
      case TDPrefKey.FWRD:
        return makeFwd( k.cat, k.key, k.title, k.level, ctx, hlp );
      case TDPrefKey.COL:
        return makeColor( k.cat, k.key, k.title, k.summary, k.level, k.dflt, ctx, hlp );
    }
    return null;
  }

 
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

  // /** factory cstr a "color" preference
  //  * @param cat     category
  //  * @param nm      preference name
  //  * @param tit     preference title
  //  * @param sum     preference description
  //  * @param lvl     activity level
  //  * @param def_val preference default boolean value
  //  * @param ctx     context
  //  * @param hlp     shared preferences helper
  //  */ 
  // private static TDPref makeColor( int cat, String nm, int tit, int sum, int lvl, int def_val, Context ctx, TDPrefHelper hlp )
  // { 
  //   String def_str = Integer.toString( def_val );
  //   String str = hlp.getString( nm, def_str );
  //   // TDLog.v("[1] Helper " + nm + " color " + str + " default " + def_str );
  //   TDPref ret = new TDPref( cat, nm, COLORBOX, tit, sum, lvl, COLOR, str, def_str, ctx, hlp );
  //   int color = def_val;
  //   try {
  //     color = Integer.parseInt( str );
  //   } catch ( NumberFormatException e ) {
  //     TDLog.e( e.getMessage() );
  //   }
  //   ret.setColor( color );
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
  public void setButtonValue( String val )
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

  static TDPref[] makePrefs( TDPrefKey[] prefs, Context ctx, TDPrefHelper hlp, int cnt )
  {
    TDPref[] ret = new TDPref[ cnt ];
    for ( int k = 0; k < cnt; ++k ) {
      ret[k] = makePref( prefs[k], ctx, hlp );
    }
    return ret;
  }

  static TDPref[] makePrefs( TDPrefKey[] prefs, Context ctx, TDPrefHelper hlp )
  {
    return makePrefs( prefs, ctx, hlp, prefs.length );
  }

  static TDPref[] makeMainPrefs( Context ctx, TDPrefHelper hlp )   { return makePrefs( TDPrefKey.mMain, ctx, hlp ); }
  static TDPref[] makeSurveyPrefs( Context ctx, TDPrefHelper hlp ) { return makePrefs( TDPrefKey.mSurvey, ctx, hlp ); }
  static TDPref[] makePlotPrefs( Context ctx, TDPrefHelper hlp )   { return makePrefs( TDPrefKey.mPlot, ctx, hlp ); }
  static TDPref[] makeCalibPrefs( Context ctx, TDPrefHelper hlp )  { return makePrefs( TDPrefKey.mCalib, ctx, hlp ); }
  static TDPref[] makeDevicePrefs( Context ctx, TDPrefHelper hlp ) { return makePrefs( TDPrefKey.mDevice, ctx, hlp ); }
  static TDPref[] makeExportPrefs( Context ctx, TDPrefHelper hlp ) { return makePrefs( TDPrefKey.mExport, ctx, hlp ); }
  static TDPref[] makeExportEnablePrefs( Context ctx, TDPrefHelper hlp ) { return makePrefs( TDPrefKey.mExportEnable, ctx, hlp ); }
  static TDPref[] makeImportPrefs( Context ctx, TDPrefHelper hlp )       { return makePrefs( TDPrefKey.mExportImport, ctx, hlp ); }
  static TDPref[] makeGeekImportPrefs( Context ctx, TDPrefHelper hlp )   { return makePrefs( TDPrefKey.mGeekImport, ctx, hlp ); }
  static TDPref[] makeShpPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportShp, ctx, hlp ); }
  static TDPref[] makeSvxPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportSvx, ctx, hlp ); }
  static TDPref[] makeThPrefs( Context ctx, TDPrefHelper hlp )           { return makePrefs( TDPrefKey.mExportTh, ctx, hlp ); }
  static TDPref[] makeDatPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportDat, ctx, hlp ); }
  static TDPref[] makeSrvPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportSrv, ctx, hlp ); }
  static TDPref[] makePlyPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportPly, ctx, hlp ); }
  static TDPref[] makeTroPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportTro, ctx, hlp ); }
  static TDPref[] makeSvgPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportSvg, ctx, hlp ); }
  static TDPref[] makeDxfPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportDxf, ctx, hlp ); }
  static TDPref[] makeKmlPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportKml, ctx, hlp ); }
  static TDPref[] makeCsxPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportCsv, ctx, hlp ); }
  static TDPref[] makeGpxPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportGpx, ctx, hlp ); }
  static TDPref[] makeCsvPrefs( Context ctx, TDPrefHelper hlp )          { return makePrefs( TDPrefKey.mExportCsv, ctx, hlp ); }
  static TDPref[] makeShotPrefs( Context ctx, TDPrefHelper hlp )         { return makePrefs( TDPrefKey.mData, ctx, hlp ); }
  static TDPref[] makeUnitsPrefs( Context ctx, TDPrefHelper hlp )        { return makePrefs( TDPrefKey.mUnits, ctx, hlp ); }
  static TDPref[] makeAccuracyPrefs( Context ctx, TDPrefHelper hlp )     { return makePrefs( TDPrefKey.mAccuracy, ctx, hlp ); }
  static TDPref[] makeLocationPrefs( Context ctx, TDPrefHelper hlp )     { return makePrefs( TDPrefKey.mLocation, ctx, hlp ); }
  static TDPref[] makeScreenPrefs( Context ctx, TDPrefHelper hlp )       { return makePrefs( TDPrefKey.mScreen, ctx, hlp ); }
  static TDPref[] makeLinePrefs( Context ctx, TDPrefHelper hlp )         { return makePrefs( TDPrefKey.mLine, ctx, hlp ); }
  static TDPref[] makePointPrefs( Context ctx, TDPrefHelper hlp )        { return makePrefs( TDPrefKey.mPoint, ctx, hlp ); }
  static TDPref[] makeDrawPrefs( Context ctx, TDPrefHelper hlp )         { return makePrefs( TDPrefKey.mDraw, ctx, hlp ); }
  static TDPref[] makeErasePrefs( Context ctx, TDPrefHelper hlp )        { return makePrefs( TDPrefKey.mErase, ctx, hlp ); }
  static TDPref[] makeEditPrefs( Context ctx, TDPrefHelper hlp )         { return makePrefs( TDPrefKey.mEdit, ctx, hlp ); }
  static TDPref[] makeGeekDevicePrefs( Context ctx, TDPrefHelper hlp )   { return makePrefs( TDPrefKey.mGeekDevice, ctx, hlp ); }
  static TDPref[] makeGeekLinePrefs( Context ctx, TDPrefHelper hlp )     { return makePrefs( TDPrefKey.mGeekLine, ctx, hlp ); }
  static TDPref[] makeGeekShotPrefs( Context ctx, TDPrefHelper hlp )     { return makePrefs( TDPrefKey.mGeekShot, ctx, hlp ); }
  static TDPref[] makeGeekPlotPrefs( Context ctx, TDPrefHelper hlp )     { return makePrefs( TDPrefKey.mGeekPlot, ctx, hlp ); }
  static TDPref[] makeGeekSplayPrefs( Context ctx, TDPrefHelper hlp )    { return makePrefs( TDPrefKey.mGeekSplay, ctx, hlp ); }
  static TDPref[] makeGeekPrefs( Context ctx, TDPrefHelper hlp ) 
  { 
    if ( TDLevel.isDebugBuild( ) ) {
      return makePrefs( TDPrefKey.mGeek, ctx, hlp );
    } else {
      return makePrefs( TDPrefKey.mGeek, ctx, hlp, 14 );
    }
  }
  static TDPref[] makeCave3DPrefs( Context ctx, TDPrefHelper hlp )       { return makePrefs( TDPrefKey.mCave3D, ctx, hlp ); }
  static TDPref[] makeDem3DPrefs( Context ctx, TDPrefHelper hlp )        { return makePrefs( TDPrefKey.mDem3D, ctx, hlp ); }
  static TDPref[] makeWalls3DPrefs( Context ctx, TDPrefHelper hlp )      { return makePrefs( TDPrefKey.mWalls3D, ctx, hlp ); }
  static TDPref[] makeSketchPrefs( Context ctx, TDPrefHelper hlp )       { return makePrefs( TDPrefKey.mSketch, ctx, hlp ); }


}
