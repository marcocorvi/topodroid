package com.topodroid.io.svg;

import com.topodroid.TDX.DrawingPath;
import com.topodroid.TDX.DrawingPointPath;

import java.util.ArrayList;
import java.util.HashMap;

public class SvgGroupedPaths {
  final ArrayList<DrawingPointPath> xsectionsPoints = new ArrayList<>();
  final HashMap< String, ArrayList<DrawingPath> > points = new HashMap<>();
  final HashMap< String, ArrayList< DrawingPath > > lines  = new HashMap<>();
  final HashMap< String, ArrayList< DrawingPath > > areas  = new HashMap<>();
}
