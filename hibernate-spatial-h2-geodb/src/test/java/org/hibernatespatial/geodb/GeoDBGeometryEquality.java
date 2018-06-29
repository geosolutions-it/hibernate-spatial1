/**
 * 
 */
package org.hibernatespatial.geodb;

import org.hibernatespatial.test.GeometryEquality;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Extends the test for {@link Geometry} equality, because GeoDB uses JTS
 * Geometry objects, which cannot be cast to {@link MGeometry} objects.
 * 
 * @author Jan Boonen, Geodan IT b.v.
 */
public class GeoDBGeometryEquality extends GeometryEquality {

	@Override
	public boolean test(Geometry geom1, Geometry geom2) {
		if (geom1 != null && geom1.isEmpty())
			return geom2 == null || geom2.isEmpty();
		return super.test(geom1, geom2);
	}

	@Override
	protected boolean testSimpleGeometryEquality(Geometry geom1, Geometry geom2) {
		return testVerticesEquality(geom1, geom2);
	}

	private boolean testVerticesEquality(Geometry geom1, Geometry geom2) {
		if (geom1.getNumPoints() != geom2.getNumPoints())
			return false;
		for (int i = 0; i < geom1.getNumPoints(); i++) {
			Coordinate cn1 = geom1.getCoordinates()[i];
			Coordinate cn2 = geom2.getCoordinates()[i];
			if (!cn1.equals2D(cn2))
				return false;
		}
		return true;
	}
}
