/*
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package com.topodroid.ui;

import android.view.MotionEvent;

public class MotionEventEclair extends MotionEventWrap
{
   /** cstr - save the event in the superclass
    * @param event   motion event
    */
   public MotionEventEclair(MotionEvent event) {
      super(event);
   }

   /** @return X coordinate of a touch
    * @param pointerIndex  touch index
    */
   public float getX(int pointerIndex) {
      return event.getX(pointerIndex);
   }

   /** @return Y coordinate of a touch
    * @param pointerIndex  touch index
    */
   public float getY(int pointerIndex) {
      return event.getY(pointerIndex);
   }

   /** @return the number of simultaneous touches
    */
   public int getPointerCount() {
      return event.getPointerCount();
   }

   /** @return ID (???) of a touch
    * @param pointerIndex  touch index
    */
   public int getPointerId(int pointerIndex) {
      return event.getPointerId(pointerIndex);
   }
}
