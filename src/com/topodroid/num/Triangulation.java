package com.topodroid.num;

import com.topodroid.utils.TDMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public class Triangulation 
{
	final private List< TriShot > shots;
	final private HashMap< String, Tri2Leg > legs;
	final private ArrayList< String > mirroredStations;
	final private HashMap< String, Tri2Station > adjustedStations;
	final private HashMap< String, Tri2StationAxle > axles;
	final private HashMap< String, TriS2tationStatus > stationStatus;

	final private HashMap< String, TriShot > triangleShots;
	final private HashMap< String, ArrayList< TriShot > > triangleSimilarShots;
	final private ArrayList< Tri2Leg > triangleLegs;
	final private HashMap< String, Tri2Leg > triangleLegsMap;
	final private HashSet< String > triangleStations;
	final private HashSet< String > triangleUnadjustedStations;
	private UUID triangleUUID;

	Triangulation(List< TriShot > shs, ArrayList< String > mrSt) { 
		shots = shs;
		legs = new HashMap<>();
		mirroredStations = mrSt;
		adjustedStations = new HashMap<>();
		axles = new HashMap<>();
		stationStatus = new HashMap<>();

		triangleShots = new HashMap<  >();
		triangleSimilarShots = new HashMap<  >();
		triangleLegs = new ArrayList<  >();
		triangleLegsMap = new HashMap<>();
		triangleStations = new HashSet<  >();
		triangleUnadjustedStations = new HashSet<  >();
	}

	HashMap< String, Tri2StationAxle > getStationAxles() {
		return axles;
	}

	HashMap< String, TriS2tationStatus > getStationStatus() {
		return stationStatus;
	}

	private void resetTriangle() {
		triangleShots.clear();
		triangleSimilarShots.clear();
		triangleLegs.clear();
		triangleLegsMap.clear();
		triangleStations.clear();
		triangleUnadjustedStations.clear();
		triangleUUID = null;
	}

	private boolean addTriangleSimilarShot( TriShot sh ) {
		String name = sh.name();
		if (! triangleShots.containsKey(name)) return false;

		if (! triangleSimilarShots.containsKey(name)) triangleSimilarShots.put(name, new ArrayList<  >());
		ArrayList< TriShot > list = triangleSimilarShots.get(name);
		list.add(sh);
		sh.triangle = triangleUUID;

		return true;
	}

	private int countAdjustedStations(TriShot sh) {
		int count = 0;
		if (adjustedStations.containsKey(sh.from)) count++;
		if (adjustedStations.containsKey(sh.to)) count++;
		return count;
	}
	
	private void getPreadjustedLeg() {
		
	}

	void triangulate() 
	{
		for ( TriShot sh : shots ) sh.triangle = null;
		int ns = shots.size();
		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			for (int n1 = 0; n1 < ns; ++n1) {
				TriShot sh1 = shots.get(n1);
				if (sh1.triangle != null) continue;
				if (adjustedStations.isEmpty()) {
					triangleUUID = UUID.randomUUID();
					addTriangleShot(sh1);
				}
				else {
					if (countAdjustedStations(sh1) == 1) {
						triangleUUID = UUID.randomUUID();
						addTriangleShot(sh1);
					}
					else {
						continue;
					}
				}

				// Getting second shot of the triangle (mandatory).
				boolean addedSecondShot = false;
				int n2;
				for (n2 = n1+1; n2 < ns; ++n2) {
					TriShot sh2 = shots.get(n2);
					if (sh2.triangle != null) continue;
					if (addTriangleShot(sh2)) {
						addedSecondShot = true;
						break;
					}
				}
				if (!addedSecondShot) {
					resetTriangle();
					continue;
				}

				// getPreadjustedLeg();

				// Getting third shot of the triangle (optional).
				for (int n3 = n2+1; n3 < ns; ++n3) {
					TriShot sh3 = shots.get(n3);
					if (sh3.triangle != null) continue;
					if (!addTriangleSimilarShot(sh3)) {
						addTriangleShot(sh3);
					}
				}

				if(adjust()) {
					tryAgain = true;
				}
				else {
					for (TriShot sh: triangleShots.values()) {
						sh.triangle = null;
					}
					for (ArrayList<TriShot> ar: triangleSimilarShots.values()) {
						for (TriShot sh: ar) {
							sh.triangle = null;
						}
					}
				}
				resetTriangle();
			}
		}

		for (TriShot sh: shots) {
			markAsUnadjusted(sh.from);
			markAsUnadjusted(sh.to);
		}
	}

	private void markAsUnadjusted(String stationName) {
		if (! stationStatus.containsKey(stationName)) {
			stationStatus.put(stationName, TriS2tationStatus.UNADJUSTED);
		}
	}
	
	private boolean addTriangleShot(TriShot sh ) {
		if (triangleShots.size() == 3) return false;

		String from = sh.from.trim();
		String to = sh.to.trim();

		if (from.isEmpty() || to.isEmpty() || from.equals(to)) return false;

		int nrStations = triangleStations.size();
		// For the first triangle.
		if (adjustedStations.size() < 2) {
			if (nrStations == 2) {
				if ((! triangleStations.contains(from)) && (! triangleStations.contains(to))) return false;
			}
			else if (nrStations == 3) {
				if (! triangleStations.contains(from) || (! triangleStations.contains(to))) return false;
			}
		}
		else {
			// For all remaining triangles.
			if (triangleUnadjustedStations.size() == 1) {
				if (nrStations == 3) {
					if ((! triangleStations.contains(from)) || (! triangleStations.contains(to))) return false;
				}
				else {
					if ((!(triangleStations.contains(from) && adjustedStations.containsKey(to))) &&
							(!(triangleStations.contains(to) && adjustedStations.containsKey(from))))
						return false;
				}
			}
			else {
				switch (nrStations) {
					case 0 -> {
                                if ((! adjustedStations.containsKey(from)) && (! adjustedStations.containsKey(to))) return false;
                                }
					case 2 -> {
                                if ((! triangleStations.contains(from)) && (! triangleStations.contains(to))) return false;
                                }
					case 3 -> {
                                if ((! triangleStations.contains(from)) || (! triangleStations.contains(to))) return false;
                                }
				}
			}
		}

		String name = sh.name();
		if (triangleShots.containsKey(name)) throw new RuntimeException("Tri2Triangle.addShot: shot already exists (THIS SHOULD NEVER HAPPEN!)");

		sh.triangle = triangleUUID;
		triangleShots.put(name, sh);
		addTriangleStation(from);
		addTriangleStation(to);
		Tri2Leg leg = new Tri2Leg(sh);
		addTriangleLeg(leg);

		return true;
	}

	private void addTriangleLeg(Tri2Leg leg) {
		triangleLegs.add(leg);
		triangleLegsMap.put(leg.name(), leg);
		leg.isOrdered = false;
	}

	private void addTriangleStation(String name) {
		triangleStations.add(name);
		if (!adjustedStations.containsKey(name)) triangleUnadjustedStations.add(name);
	}

	private List< Tri2Leg > triangleOrderedLegs(int adjustedLegs) {
		LinkedHashSet< Tri2Leg > orderedLegs = new LinkedHashSet<>();
		Tri2Leg leg = null;

		// Setting initial leg. It won't have it's azimuth adjusted as it will be the reference azimuth for the other legs.
		if (adjustedLegs == 0) {
			leg = triangleLegs.get(0);
		}
		else {
			for (Tri2Leg l : triangleLegs) {
				if (l.isAdjusted) {
					leg = l;
					break;
				}
			}
			if (leg == null) throw new RuntimeException("Tri2Triangle.orderLegs: can´t find adjusted leg");
		}
		
		leg.isOrdered = true;
		orderedLegs.add(leg);
		String firstStation = leg.from;
		String lastStation = leg.to;

		for (Tri2Leg l : triangleLegs) {
			if (l.isOrdered) continue;

			if (l.containsStation(lastStation)) {
				if (l.to.equals(lastStation)) l.invert();
				l.isOrdered = true;
				orderedLegs.add(l);
				lastStation = l.to;
				break;
			}
		}

		for (Tri2Leg l : triangleLegs) {
			if (l.isOrdered) continue;

			if (l.containsStation(lastStation)) {
				if (l.to.equals(lastStation)) l.invert();
				if (! l.to.equals(firstStation)) throw new RuntimeException("Tri2Triangle.orderLegs: invalid final leg");
				l.isOrdered = true;
				orderedLegs.add(l);
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

	private boolean calculateLeg(String f, String t) {
		if ((! adjustedStations.containsKey(f)) || (! adjustedStations.containsKey(t))) return false;

		String name = TriShot.name(f, t);

		if (triangleLegsMap.containsKey(name)) return false;

		Tri2Leg leg;

		if (legs.containsKey(name)) {
			leg = legs.get(name);
		}
		else {
			Tri2Point from = adjustedStations.get(f).point;
			Tri2Point to = adjustedStations.get(t).point;

			double[] lac = calculateLAC(from.x, from.y, from.z, to.x, to.y, to.z);
			leg = new Tri2Leg(lac[0], lac[1], lac[2], f, t);

			addLeg(leg);
		}

		addTriangleLeg(leg);

		return true;
	}

	private double[] calculateLAC(double x1, double y1, double z1, double x2, double y2, double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;

		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

		double azimuth = (TDMath.isEqual(dx, 0d, TDMath.measurementEpsilonD) && TDMath.isEqual(dy, 0d, TDMath.measurementEpsilonD)) ? 
			0d : TDMath.in360(TDMath.atan2Dd(dy, dx));

		double clino = (TDMath.isEqual(length, 0d, TDMath.measurementEpsilonD)) ? 0d : TDMath.asinDd(dz / length);

		return new double[]{length, azimuth, clino};
	}

	private boolean adjust() {
		if (triangleStations.size() != 3) return false;

		// In case this triangle includes 2 previous adjusted stations that don´t have a leg connecting them.
		if (triangleLegs.size() != 3) {
			ArrayList< String > stationsArray = new ArrayList<>(triangleStations);
			if (! calculateLeg(stationsArray.get(0), stationsArray.get(1))) {
				if (! calculateLeg(stationsArray.get(1), stationsArray.get(2))) {
					if (! calculateLeg(stationsArray.get(0), stationsArray.get(2))) {
						return false;
					}
				}
			}
		}

		int nrAdjustedLegs = 0;
		for (Tri2Leg leg : triangleLegs) {
			if (leg.isAdjusted) nrAdjustedLegs++;
		}
		// TDLog.v("nrAdjustedLegs: " + nrAdjustedLegs);
		if (nrAdjustedLegs > 1) return false;

		List< Tri2Leg > orlg = triangleOrderedLegs(nrAdjustedLegs);

		// Adjusting clino.
		Tri2Leg leg0 = orlg.get(0);
		Tri2Leg leg1 = orlg.get(1);
		Tri2Leg leg2 = orlg.get(2);
		double h0 = legHeight(leg0);
		double h1 = legHeight(leg1);
		double h2 = legHeight(leg2);
		double totalLength = leg1.length + leg2.length;
		// If there are no adjusted shot in this triangle, the clino of the first shot/leg will also be adjusted.
		if (nrAdjustedLegs == 0) totalLength += leg0.length;
		double adjustH = -(h0 + h1 + h2) / totalLength;
		if (! TDMath.isEqual(adjustH, 0d, TDMath.measurementEpsilonD)) {
			double clino0 = (nrAdjustedLegs == 0) ?
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

		Tri2Leg firstLeg = orlg.get(0);
		String firstFromName = firstLeg.from;
		boolean isReference = false;
		if (adjustedStations.isEmpty()) {
			Tri2Point origin = new Tri2Point(0d, 0d, 0d);
			adjustedStations.put(firstFromName, new Tri2Station(firstFromName, origin));
			stationStatus.put(firstFromName, TriS2tationStatus.REFERENCE);
			isReference = true;
		}
		Tri2Point pFrom = adjustedStations.get(firstFromName).point;
		for (Tri2Leg leg : orlg) {
			String legName = leg.name();
			addLeg(leg);
			if (triangleShots.containsKey(legName)) {
				TriShot sh = triangleShots.get(legName);
				updateShot(sh, leg);
				if (triangleSimilarShots.containsKey(legName)) {
					ArrayList< TriShot > similarShotsList = triangleSimilarShots.get(legName);
					for (TriShot similarSh : similarShotsList) {
						updateShot(similarSh, leg);
					}
				}
			}

			String toName = leg.to;
			if (adjustedStations.containsKey(toName)) {
				pFrom = adjustedStations.get(toName).point;
			}
			else {
				Tri2Point pTo = calculatePoint(pFrom, leg.length, leg.azimuth, leg.clino);
				adjustedStations.put(toName, new Tri2Station(toName, pTo));
				pFrom = pTo;
				if (isReference) {
					stationStatus.put(toName, TriS2tationStatus.REFERENCE);
					isReference = false;
				}
				else {
					stationStatus.put(toName, TriS2tationStatus.ADJUSTED);
					Tri2StationAxle axle = new Tri2StationAxle(
						toName,
						leg0.name(),
						triangleShots,
						triangleSimilarShots
					);
					axles.put(toName, axle);
				}
			}
		}

		return true;
	}

	private void addLeg(Tri2Leg leg) {
		String name = leg.name();
		if (legs.containsKey(name)) return;
		legs.put(name, leg);
		leg.isAdjusted = true;
	}

	private Tri2Point calculatePoint(Tri2Point from, double length, double azimuth, double clino) {
		double x = from.x + length * TDMath.sinDd(azimuth) * TDMath.cosDd(clino);
		double y = from.y + length * TDMath.cosDd(azimuth) * TDMath.cosDd(clino);
		double z = from.z + length * TDMath.sinDd(clino);

		return new Tri2Point(x, y, z);
	}

	private void updateShot( TriShot sh, Tri2Leg leg ) {
		// Only setting the per shot declination azimuth of the shot to match the  azimuth of the leg.
		// double newAzimuth = (leg.from.equals(sh.from)) ? leg.azimuth : TDMath.add180(leg.azimuth);
		// double shotDeclination = newAzimuth - sh.bearing();
		// sh.mAvgLeg.mDecl = (float)shotDeclination; // per shot declination

		// Setting both azimuth and clino.
		if (leg.from.equals(sh.from)) {
			sh.mAvgLeg.set((float)leg.length, (float)leg.azimuth, (float)leg.clino);
		}
		else {
			sh.mAvgLeg.set((float)leg.length, (float)TDMath.add180(leg.azimuth), (float)(-leg.clino));
		}
	}

	private double getClino(double length, double heightAdjust) {
		if (length == 0) {
				throw new IllegalArgumentException("length cannot be zero.");
		}

		double ratio = heightAdjust / length;
		if (ratio < -1 || ratio > 1) {
				throw new IllegalArgumentException("heightAdjust / length must be in the range [-1, 1].");
		}

        return TDMath.asinDd(ratio);
	}

	private double legHeight(Tri2Leg leg) {
		return height(leg.length, leg.clino);
	}

	private double height(double length, double clino) {
        return length * TDMath.sinDd(clino);
	}
}

