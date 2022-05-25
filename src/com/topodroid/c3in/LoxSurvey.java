/* @file LoxSurvey.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch Survey
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;


class LoxSurvey
{
  int id;
  int pid; // parent id
  String name;
  String title;

  LoxSurvey( int _id, int _pid, String n, String t )
  {
    id    = _id;
    pid   = _pid;
    name  = n;
    title = t;
    // LOGI("Survey cstr %d", id );
  }

  int Id() { return id; }
  int Parent() { return pid; }

  String Name()  { return name; }
  String Title() { return title; }

}
