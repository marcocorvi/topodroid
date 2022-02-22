/*
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
 *
 * This class has been taken from "Hello, Android" source code
***/
package com.topodroid.ui;

import android.os.Build;
import android.view.MotionEvent;

public class MotionEventWrap
{
   protected MotionEvent event;

   /** cstr
    * @param event    motion event
    */
   public MotionEventWrap( MotionEvent event )
   {
      this.event = event;
   }

   /** @return a wrapper of the given event
    * @param event    motion event
    */
   public static MotionEventWrap wrap(MotionEvent event)
   {
      // FIXME NFE 

      // Use Build.VERSION.SDK_INT if you don't have to support Cupcake
      if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) { // DEPRECATED API-16
         // TDLog.d("MotionEventWrap using Eclair version");
         return new MotionEventEclair(event);
      } else {
         // TDLog.d("MotionEventWrap using Cupcake/Donut version");
         return new MotionEventWrap(event);
      }
   }
   
   /** @return the action ID from the wrapped event
    */
   public int getAction() { return event.getAction(); }

   // this is the X coord (pixels, from the center ?) adjusted for containing window and views
   // to get the real X use getRawX()
   
   /** @return the X coordinate from the wrapped event
    */
   public float getX() { return event.getX(); }
   
   /** @return the X coordinate from the wrapped event
    * @param pointerIndex touch index
    */
   public float getX(int pointerIndex)
   {
      verifyPointerIndex(pointerIndex);
      return getX();
   }

   /** @return the Y coordinate from the wrapped event
    */
   public float getY() { return event.getY(); }
   
   /** @return the Y coordinate from the wrapped event
    * @param pointerIndex touch index
    */
   public float getY(int pointerIndex)
   {
      verifyPointerIndex(pointerIndex);
      return getY();
   }
 
   /** @return the number of touches - always 1
    */
   public int getPointerCount() { return 1; }

   /** @return the touch ID - always 0
    * @param pointerIndex touch index
    */
   public int getPointerId(int pointerIndex)
   {
      verifyPointerIndex(pointerIndex);
      return 0;
   }

   /** verify that the touch index is valid
    * @param pointerIndex touch index
    */
   private void verifyPointerIndex(int pointerIndex) 
   {
      if (pointerIndex > 0) {
         throw new IllegalArgumentException(
               "Invalid pointer index for Donut/Cupcake");
      }
   }
   
}

