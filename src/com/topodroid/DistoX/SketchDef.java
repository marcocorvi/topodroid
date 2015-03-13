/** @file SketchDef.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: defines
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20130310 created
 */
package com.topodroid.DistoX;

class SketchDef
{
    // static final float LINE_STEP = 0.5f; // 0.5 m between line 3d points
    // static final float INNER_BORDER_STEP = 0.2f; // 0.2 m between border-line 3d points
    // static final float SECTION_STEP = 0.5f;  
    static final float CLOSE_GAP  = 1.0f;
    static final int POINT_MIN    =  4; //  4 minimum number of 3D points on a line
    // static final int POINT_MAX    = 20; // 12 maximum number of 3D points on a line. UNUSED
    static final float MIN_DISTANCE = 20.0f; // minimum closeness distance (select at)

    // public static final float ZOOM_INC = 1.4f;
    // public static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public final static int DISPLAY_NGBH = 0;
    public final static int DISPLAY_SINGLE = 1;
    public final static int DISPLAY_ALL = 2;
    public final static int DISPLAY_MAX = 3;

    public static final int SYMBOL_POINT = 1;
    public static final int SYMBOL_LINE  = 2;
    public static final int SYMBOL_AREA  = 3;

    public static final int MODE_MOVE    = 0;
    public static final int MODE_DRAW    = 1;
    public static final int MODE_EDIT    = 2;  // change the surface as a whole
    public static final int MODE_SELECT  = 3;  // select a point to edit
    // public static final int MODE_HEAD = 4;
    // public static final int MODE_JOIN = 5;

    public static final int TOUCH_NONE = 0;
    public static final int TOUCH_MOVE = 2;
    public static final int TOUCH_ZOOM = 5;

    // public static final int VIEW_NONE  = 0;
    // public static final int VIEW_3D    = 1; 

    public static final int EDIT_NONE = 0;
    public static final int EDIT_CUT = 1;
    public static final int EDIT_STRETCH = 2;
    public static final int EDIT_EXTRUDE = 3;

    public static final int SELECT_NONE = 0;
    public static final int SELECT_SECTION = 1;
    public static final int SELECT_STEP = 2;
    public static final int SELECT_SHOT = 3;
    public static final int SELECT_JOIN = 4;
    
    public static final int LINE_SECTION = -1; // section line type

    static final String[] mode_name = { "none", "draw", "move", "item" };
    // static final String[] view_type = { "none", "3d" };
    static final String[] edit_name = { "none", "cut", "stretch", "extrude" };

}
