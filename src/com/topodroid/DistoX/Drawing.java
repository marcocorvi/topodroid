/* @file Drawing.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing const integers
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class Drawing
{
  static final int CODE_SELECT = 1;
  static final int CODE_ERASE  = 2;

  static final int FILTER_ALL     = 0;
  static final int FILTER_POINT   = 1;
  static final int FILTER_LINE    = 2;
  static final int FILTER_AREA    = 3;
  static final int FILTER_SHOT    = 4;
  static final int FILTER_STATION = 5;

  static final int FILTER_ERASE_MAX  = 4;
  static final int FILTER_SELECT_MAX = 6;

  static final int SCALE_SMALL  = 0;
  static final int SCALE_MEDIUM = 1;
  static final int SCALE_LARGE  = 2;
  static final int SCALE_MAX    = 3;

  static final int[] mEraseModes = {
    R.string.popup_erase_all,
    R.string.popup_erase_point,
    R.string.popup_erase_line,
    R.string.popup_erase_area
  };

  static final int[] mSelectModes = {
    R.string.popup_select_all,
    R.string.popup_select_point,
    R.string.popup_select_line,
    R.string.popup_select_area,
    R.string.popup_select_shot,
    R.string.popup_select_station
  };

  static final int[] mJoinModes = {
    R.string.popup_cont_none,
    R.string.popup_cont_start,
    R.string.popup_cont_end,
    R.string.popup_cont_both,
    R.string.popup_cont_continue
  };
}
