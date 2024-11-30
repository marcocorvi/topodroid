package com.topodroid.num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;

public class Tri2Triangle 
{
	private HashMap< String, TriShot > shots;
	private HashMap< String, ArrayList< TriShot > > similarShots;
	private ArrayList< Tri2Leg > legs;
	private HashSet< String > stations;
	private ArrayList< String > mirroredStations;

	Tri2Triangle(TriShot sh, ArrayList< String > mrst) { 
		shots = new HashMap< String, TriShot >();
		similarShots = new HashMap< String, ArrayList< TriShot > >();
		legs = new ArrayList< Tri2Leg >();
		stations = new HashSet< String >();
		mirroredStations = mrst;
		boolean addShotResult = addShot( sh );
		if (! addShotResult) throw new RuntimeException("Tri2Triangle: failed to add initial shot");
	}

	boolean addSimilarShot( TriShot sh ) {
		String name = sh.name();
		if (! shots.containsKey(name)) return false;

		if (! similarShots.containsKey(name)) similarShots.put(name, new ArrayList< TriShot >());
		ArrayList< TriShot > list = similarShots.get(name);
		list.add(sh);
		sh.triangle = this;

		return true;
	}
	
	boolean addShot(TriShot sh ) {
		if (shots.size() == 3) return false;

		if (sh.from.isEmpty() || sh.to.isEmpty() || (sh.from == sh.to)) return false;

		if (stations.size() == 2) {
			if ((! stations.contains(sh.from)) && (! stations.contains(sh.to))) return false;
		}
		else if (stations.size() == 3) {
			if (! stations.contains(sh.from) || (! stations.contains(sh.to))) return false;
		}

		String name = sh.name();
		if (shots.containsKey(name)) return false;

		sh.triangle = this;
		shots.put(name, sh);
		stations.add(sh.from);
		stations.add(sh.to);
		Tri2Leg leg = new Tri2Leg(sh);
		legs.add(leg);

		return true;
	}

	int nrShots() { return shots.size(); }

	private List< Tri2Leg > orderedLegs(int adjustedShots) {
		LinkedHashSet< Tri2Leg > orderedLegs = new LinkedHashSet<>();
		Tri2Leg leg = null;

		// Setting initial leg. It won't have it's azimuth adjusted as it will be the reference azimuth for the other legs.
		if (adjustedShots == 0) {
			leg = legs.get(0);
		}
		else {
			String adjustedShotName = null;
			for (TriShot sh : shots.values()) {
				if (sh.adjusted) {
					adjustedShotName = sh.name();
					break;
				}
			}
			if (adjustedShotName == null) throw new RuntimeException("Tri2Triangle.orderLegs: adjustedShotName is null");
			for (Tri2Leg l : legs) {
				if (l.name().equals(adjustedShotName)) {
					leg = l;
					break;
				}
			}
			if (leg == null) throw new RuntimeException("Tri2Triangle.orderLegs: leg is null");
		}
		
		leg.isOrdered = true;
		orderedLegs.add(leg);
		String firstStation = leg.from;
		String lastStation = leg.to;

		for (Tri2Leg l : legs) {
			if (l.isOrdered) continue;

			if (l.containsStation(lastStation)) {
				if (l.to.equals(lastStation)) l.invert();
				l.isOrdered = true;
				orderedLegs.add(l);
				lastStation = l.to;
				break;
			}
		}

		for (Tri2Leg l : legs) {
			if (l.isOrdered) continue;

			if (l.containsStation(lastStation)) {
				if (l.to.equals(lastStation)) l.invert();
				if (! l.to.equals(firstStation)) throw new RuntimeException("Tri2Triangle.orderLegs: invalid final leg");
				l.isOrdered = true;
				orderedLegs.add(l);
				lastStation = l.to;
				break;
			}
		}

		return new ArrayList<>(orderedLegs);
	}

	private double[] calculateAngles(double a, double b, double c) {
		double angleA = TDMath.acosDd((b * b + c * c - a * a) / (2 * b * c));
		double angleB = TDMath.acosDd((a * a + c * c - b * b) / (2 * a * c));
		double angleC = TDMath.acosDd((a * a + b * b - c * c) / (2 * a * b));

		return new double[]{angleA, angleB, angleC};
	}

	void adjust() {
		ArrayList< TriShot > shotsList = new ArrayList<>(shots.values());
		int nrAdjustedShots = 0;
		for (TriShot sh : shotsList) {
			if (sh.adjusted) nrAdjustedShots++;
		}
		// TDLog.v("nrAdjustedShots: " + nrAdjustedShots);
		if (nrAdjustedShots > 1) return;

		List< Tri2Leg > orlg = orderedLegs(nrAdjustedShots);

		// Adjusting clino.
		Tri2Leg leg0 = orlg.get(0);
		Tri2Leg leg1 = orlg.get(1);
		Tri2Leg leg2 = orlg.get(2);
		double h0 = legHeight(leg0);
		double h1 = legHeight(leg1);
		double h2 = legHeight(leg2);
		double totalLength = leg1.length + leg2.length;
		// If there are no adjusted shot in this triangle, the clino of the first shot/leg will also be adjusted.
		if (nrAdjustedShots == 0) totalLength += leg0.length;
		double adjustH = -(h0 + h1 + h2) / totalLength;
		if (! TDMath.isEqual(adjustH, 0d)) {		
			double clino0 = (nrAdjustedShots == 0) ?
					getClino(leg0.length, h0 + (leg0.length * adjustH)) :
					leg0.clino;
			double clino1 = getClino(leg1.length, h1 + (leg1.length * adjustH));
			double clino2 = getClino(leg2.length, h2 + (leg2.length * adjustH));

			// Debug clino
			// double originalClinoError = h0 + h1 + h2;
			// double newH0 = height(leg0.length, clino0);
			// double newH1 = height(leg1.length, clino1);
			// double newH2 = height(leg2.length, clino2);
			// double endClinoError = newH0 + newH1 + newH2;
			// TDLog.v("Tri2Triangle.adjust (original): leg0.clino() = " + leg0.clino + ", leg1.clino() = " + leg1.clino + ", leg2.clino() = " + leg2.clino);
			// TDLog.v("Tri2Triangle.adjust (adjusted): clino0 = " + clino0 + ", clino1 = " + clino1 + ", clino2 = " + clino2);
			// TDLog.v("Tri2Triangle.adjust (original): h0 = " + h0 + ", h1 = " + h1 + ", h2 = " + h2);
			// TDLog.v("Tri2Triangle.adjust (adjusted): newH0 = " + newH0 + ", newH1 = " + newH1 + ", newH2 = " + newH2);
			// TDLog.v("Tri2Triangle.adjust (original): originalClinoError = " + originalClinoError);
			// TDLog.v("Tri2Triangle.adjust (adjusted): endClinoError = " + endClinoError);

			// Applying new clinos before adjusting azimuths
			leg0.clino = clino0;
			leg1.clino = clino1;
			leg2.clino = clino2;
		}

		// Adjusting azimuth
		double[] angles = calculateAngles(leg0.lengthH(), leg1.lengthH(), leg2.lengthH());
		// The azimuth of the first shot/leg is never adjusted: it is the reference used to calculate the azimuths of the 
		// other 2 shots/legs.
		double azimuth0 = TDMath.in360(leg0.azimuth);
		// The user sets which stations are mirrored, so we need to adjust the azimuths accordingly. There is no way to
		// know which stations should be mirrored automatically.
		// A mirrored station results in a triangle mirrored around its first leg.
		if (mirroredStations.contains(leg1.to)) angles[2] = - angles[2];
		double azimuth1 = TDMath.in360(TDMath.add180(azimuth0) - angles[2]);
		double azimuth2 = TDMath.in360(TDMath.add180(azimuth1) - angles[0]);

		// Debug azimuth
		// TDLog.v("Tri2Triangle.adjust (original): leg0.azimuth = " + leg0.azimuth + ", leg1.azimuth = " + leg1.azimuth + ", leg2.azimuth = " + leg2.azimuth);
		// TDLog.v("Tri2Triangle.adjust (adjusted): azimuth0 = " + azimuth0 + ", azimuth1 = " + azimuth1 + ", azimuth2 = " + azimuth2);

		leg0.azimuth = azimuth0;
		leg1.azimuth = azimuth1;
		leg2.azimuth = azimuth2;

		HashMap< String, Tri2Leg > legsMap = new HashMap<>();
		for (Tri2Leg l : legs) {
			legsMap.put(l.name(), l);
		}

		for (TriShot sh : shotsList) {
			String name = sh.name();
			if (legsMap.containsKey(name)) {
				Tri2Leg leg = legsMap.get(name);
				updateShot(sh, leg);
				if (similarShots.containsKey(name)) {
					ArrayList< TriShot > similarShotsList = similarShots.get(name);
					for (TriShot similarSh : similarShotsList) {
						updateShot(similarSh, leg);
					}
				}
			}
		}
	}

	private void updateShot( TriShot sh, Tri2Leg leg ) {
		// Only setting the azimuth of the shot to the azimuth of the leg.
		// double newAzimuth = (leg.from.equals(sh.from)) ? leg.azimuth : TDMath.add180(leg.azimuth);
		// double shotDeclination = newAzimuth - sh.bearing();
		// sh.mAvgLeg.mDecl = (float)shotDeclination; // per shot declination

		// Setting both azimuth and clino. 
		sh.mAvgLeg.set((float)leg.length, (float)leg.azimuth, (float)leg.clino);
		sh.adjusted = true;
	}

	private double getClino(double length, double heightAdjust) {
		if (length == 0) {
				throw new IllegalArgumentException("length cannot be zero.");
		}

		double ratio = heightAdjust / length;
		if (ratio < -1 || ratio > 1) {
				throw new IllegalArgumentException("heightAdjust / length must be in the range [-1, 1].");
		}

		double newClino = TDMath.asinDd(ratio);

		return newClino;
	}

	private double legHeight(Tri2Leg leg) {
		return height(leg.length, leg.clino);
	}

	private double height(double length, double clino) {
		double height = length * TDMath.sinDd(clino);
		return height;
	}
}

