/* @file PopupMode
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid popup const integers
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.TDX.R;

public class PopupMode
{
  public static final int CODE_SELECT = 1; // select filter
  public static final int CODE_ERASE  = 2; // erase filter
  public static final int CODE_COLOR  = 3; // used by TopoGL
  public static final int CODE_EDIT   = 4; // edit popup

  public static final int FILTER_ALL     = 0;
  public static final int FILTER_POINT   = 1;
  public static final int FILTER_LINE    = 2;
  public static final int FILTER_AREA    = 3;
  public static final int FILTER_SHOT    = 4;
  public static final int FILTER_STATION = 5;

  public static final int FILTER_ERASE_MAX  = 4;
  public static final int FILTER_SELECT_MAX = 6;

  public static final int SCALE_SMALL  = 0;
  public static final int SCALE_MEDIUM = 1;
  public static final int SCALE_LARGE  = 2;
  public static final int SCALE_MAX    = 3;

  public static final int[] mEraseModes = {
    R.string.popup_erase_all,
    R.string.popup_erase_point,
    R.string.popup_erase_line,
    R.string.popup_erase_area
  };

  public static final int[] mSelectModes = {
    R.string.popup_select_all,
    R.string.popup_select_point,
    R.string.popup_select_line,
    R.string.popup_select_area,
    R.string.popup_select_shot,
    R.string.popup_select_station
  };

  public static final int[] mColorModes = { // used by TopoGL
    R.string.popup_color_none,
    R.string.popup_color_survey,
    R.string.popup_color_depth,
    R.string.popup_color_surface
  };

  // 2023-03-10 DROPPED 
  // public static final int[] mJoinModes = {
  //   R.string.popup_cont_none,
  //   R.string.popup_cont_start,
  //   R.string.popup_cont_end,
  //   R.string.popup_cont_both,
  //   R.string.popup_cont_continue
  // };
}
