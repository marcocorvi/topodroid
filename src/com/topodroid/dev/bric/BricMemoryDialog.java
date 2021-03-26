/** @file BricMemoryDialog.java
 */
package com.topodroid.dev.bric;

import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.DeviceActivity;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import com.topodroid.ui.MyDialog;

import android.os.Bundle;
import android.content.res.Resources;
import android.content.Context;

import android.util.Log;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;

public class BricMemoryDialog extends MyDialog
                              implements View.OnClickListener
{
  private final Resources mRes;
  private final DeviceActivity mParent;

  private EditText et_year;
  private EditText et_month;
  private EditText et_day;
  private EditText et_hour;
  private EditText et_minute;
  private EditText et_second;
  // private EditText et_centisecond;

  int current_year;
  int current_month;
  int current_day;
  int current_hour;
  int current_minute;
  int current_second;

  public BricMemoryDialog( Context ctx, DeviceActivity parent, Resources res )
  {
    super( ctx, R.string.BricMemoryDialog );
    mParent = parent;
    mRes    = res;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.bric_memory_dialog, R.string.bric_memory  );

    et_year   = (EditText) findViewById( R.id.bric_year );
    et_month  = (EditText) findViewById( R.id.bric_month );
    et_day    = (EditText) findViewById( R.id.bric_day );
    et_hour   = (EditText) findViewById( R.id.bric_hour );
    et_minute = (EditText) findViewById( R.id.bric_minute );
    et_second = (EditText) findViewById( R.id.bric_second );
    // et_centisecond = (EditText) findViewById( R.id.bric_battery );

    String[] time = TDUtil.currentDateTimeBric().split(" ");
    for ( int k=1; k<6; ++k ) { // make it pretty
      if ( time[k].charAt(0) == '0' ) time[k] = time[k].replaceFirst("0", " ");
    }
    et_year   .setText( time[0] ); current_year   = getInt( time[0], 0, 4000 );
    et_month  .setText( time[1] ); current_month  = getInt( time[1], 1, 12 );
    et_day    .setText( time[2] ); current_day    = getInt( time[2], 1, 31 );
    et_hour   .setText( time[3] ); current_hour   = getInt( time[3], 0, 23 );
    et_minute .setText( time[4] ); current_minute = getInt( time[4], 0, 59 );
    et_second .setText( time[5] ); current_second = getInt( time[5], 0, 59 );
    
    ((Button)findViewById( R.id.button_reset )).setOnClickListener( this );
    ((Button)findViewById( R.id.button_clear )).setOnClickListener( this );
    ((Button)findViewById( R.id.button_cancel )).setOnClickListener( this );
    // Log.v("DistoX", "Bric info dialog created");
  }

  private int getText( EditText et, int min, int max )
  {
    if ( et.getText() == null ) return max;
    return getInt( et.getText().toString(), min, max );
  }

  private int getInt( String s, int min, int max )
  {
    try { 
      int v = Integer.parseInt( s.trim() );
      // Log.v("DistoX", "parser <" + v + "> " );
      if ( v < min ) return min;
      if ( v > max ) return max;
      return v;
    } catch ( NumberFormatException e ) {
      Log.v("DistoX", "parser Number Format Error <" + s + "> " );
    }
    return min;
  }

  @Override
  public void onClick(View view)
  {
    if ( view.getId() == R.id.button_reset ) {
      boolean ok = true;
      // Log.v("DistoX", "BRIC memory dialog : reset ");
      int yy = getText( et_year, 0, 4000 );
      if ( yy > current_year ) {
        // Log.v("DistoX", "BRIC memory dialog : fail year " + yy + " " + current_year);
        ok = false;
      } else { 
        int mm = getText( et_month, 1, 12 );
        if ( yy == current_year && mm > current_month ) {
          // Log.v("DistoX", "BRIC memory dialog : fail month " + mm + " " + current_month );
          ok = false;
        } else {
          int md = 31; 
          if ( mm == 4 || mm == 6 || mm == 9 || mm == 11 ) { md = 30; }
          else if ( mm == 2 ) { md = ( ((yy % 4)==0) && ((yy%100)!=0) )? 29 : 28; }
          int dd = getText( et_day, 1, md );
          if ( yy == current_year && mm == current_month && dd > current_day ) {
            // Log.v("DistoX", "BRIC memory dialog : fail day " + dd + " " + current_day );
            ok = false;
          } else {
            int HH = getText( et_hour, 0, 23 );
            if ( yy == current_year && mm == current_month && dd == current_day && HH > current_hour ) {
              // Log.v("DistoX", "BRIC memory dialog : fail hour : " + HH + " " + current_hour );
              ok = false;
            } else {
              int MM = getText( et_minute, 0, 59 );
              if ( yy == current_year && mm == current_month && dd == current_day && HH == current_hour && MM > current_minute ) {
                // Log.v("DistoX", "BRIC memory dialog : fail minute : " + MM + " " + current_minute );
                ok = false;
              } else {
                int SS = getText( et_second, 0, 59 );
                if ( yy == current_year && mm == current_month && dd == current_day && HH == current_hour && MM == current_minute && SS > current_second ) {
                  // Log.v("DistoX", "BRIC memory dialog : fail second : " + SS + " " + current_second );
                  ok = false;
                } else {
                  // Log.v("DistoX", "BRIC memory dialog : " + yy + " " + mm + " " + dd + " " + HH + " " + MM + " " + SS );
                  mParent.doBricMemoryReset( yy, mm, dd, HH, MM, SS );
                }
              }
            }
          }
        }
      }
      if ( ! ok ) {
        TDToast.makeWarn( R.string.bric_future_time );
      } 
    } else if ( view.getId() == R.id.button_clear ) {
      Log.v("DistoX", "BRIC memory dialog : clear ");
      mParent.doBricMemoryClear( );
    }
    dismiss();
  }


}
