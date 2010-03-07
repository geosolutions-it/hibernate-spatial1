/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2010 Geovise BVBA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */

package org.hibernatespatial.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.CustomType;
import org.hibernatespatial.GeometryUserType;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Feb 21, 2010
 * Time: 2:04:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpatialFeaturesTestDriver {

    private static Logger LOGGER = LoggerFactory.getLogger(SpatialFeaturesTestDriver.class);

    private static SessionFactory factory;
    private final AbstractExpectationsFactory expectationsFactory;

    public SpatialFeaturesTestDriver(AbstractExpectationsFactory expectationsFactory) {
        this.expectationsFactory = expectationsFactory;
    }

    public static void setUpBeforeClass() throws Exception {

        // set up hibernate and register Spatialtest as a persistent entity
        LOGGER.info("Setting up Hibernate");
        Configuration config = new Configuration();
        config.configure();
        config.addClass(GeomEntity.class);

        //configure Hibernate Spatial based on this config
        HSConfiguration hsc = new HSConfiguration();
        hsc.configure(config);
        HBSpatialExtension.setConfiguration(hsc);

        // build the session factory
        factory = config.buildSessionFactory();

        LOGGER.info("Hibernate set-up complete.");
    }


    public void test_dimension() throws SQLException {
        Map<Integer, Integer> dbexpected = expectationsFactory.getExpectedDimension();
        String hql = "SELECT id, dimension(geom) FROM GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }

    public void test_astext() throws SQLException {
        Map<Integer, String> dbexpected = expectationsFactory.getAsText();
        String hql = "SELECT id, astext(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }

    public void test_asbinary() throws SQLException {
        Map<Integer, byte[]> dbexpected = expectationsFactory.getAsBinary();
        String hql = "SELECT id, asbinary(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }


    public void test_geometrytype() throws SQLException {
        Map<Integer, String> dbexpected = expectationsFactory.getGeometryType();
        String hql = "SELECT id, geometrytype(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }

    public void test_srid() throws SQLException {
        Map<Integer, Integer> dbexpected = expectationsFactory.getSrid();
        String hql = "SELECT id, srid(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }

    public void test_issimple() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getIsSimple();
        String hql = "SELECT id, issimple(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }

    public void test_isempty() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getIsEmpty();
        String hql = "SELECT id, isEmpty(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }


    public void test_boundary() throws SQLException {
        Map<Integer, Geometry> dbexpected = expectationsFactory.getBoundary();
        String hql = "SELECT id, boundary(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }


    public void test_envelope() throws SQLException {
        Map<Integer, Geometry> dbexpected = expectationsFactory.getEnvelope();
        String hql = "SELECT id, envelope(geom) from GeomEntity";
        retrieveHQLResultsAndCompare(dbexpected, hql);
    }

    public void test_within() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getWithin(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, within(geom, :filter) from GeomEntity where within(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

    private Map<String, Object> createFilterParams(String filterParamName, Geometry filterGeom) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(filterParamName, filterGeom);
        return params;
    }

    public void test_equals() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getEquals(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, equals(geom, :filter) from GeomEntity where equals(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

    public void test_crosses() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getCrosses(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, crosses(geom, :filter) from GeomEntity where crosses(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);

    }

    public void test_disjoint() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getDisjoint(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, disjoint(geom, :filter) from GeomEntity where disjoint(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

    public void test_intersects() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getIntersects(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, intersects(geom, :filter) from GeomEntity where intersects(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

    public void test_overlaps() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getOverlaps(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, overlaps(geom, :filter) from GeomEntity where overlaps(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

    public void test_touches() throws SQLException {
        Map<Integer, Boolean> dbexpected = expectationsFactory.getTouches(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, touches(geom, :filter) from GeomEntity where touches(geom, :filter) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

    public void test_relate() throws SQLException {
        String matrix = "T*T***T**";
        Map<Integer, Boolean> dbexpected = expectationsFactory.getRelate(expectationsFactory.getTestPolygon(), matrix);
        String hql = "SELECT id, relate(geom, :filter, :matrix) from GeomEntity where relate(geom, :filter, :matrix) = true and srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        params.put("matrix", matrix);
        retrieveHQLResultsAndCompare(dbexpected, hql, params);

        matrix = "FF*FF****";
        dbexpected = expectationsFactory.getRelate(expectationsFactory.getTestPolygon(), matrix);
        params.put("matrix", matrix);
        retrieveHQLResultsAndCompare(dbexpected, hql, params);

    }

    public void test_distance() throws SQLException {
        Map<Integer, Double> dbexpected = expectationsFactory.getDistance(expectationsFactory.getTestPolygon());
        String hql = "SELECT id, distance(geom, :filter) from GeomEntity where srid(geom) = 4326";
        Map<String, Object> params = createFilterParams("filter", expectationsFactory.getTestPolygon());
        retrieveHQLResultsAndCompare(dbexpected, hql, params);
    }

//
//    "SELECT buffer(e.geometry, 1) FROM PointEntity e WHERE e.id=1",
//    "SELECT buffer(e.geometry, 1) FROM LineStringEntity e WHERE e.id=1",
//    "SELECT buffer(e.geometry, 1) FROM PolygonEntity e WHERE e.id=1",
//
//    "SELECT convexhull(geomunion(e.geometry, :polygon)) FROM PolygonEntity e WHERE e.id=2",
//    "SELECT intersection(e.geometry, :polygon) FROM PolygonEntity e WHERE e.id=2",
//    "SELECT difference(e.geometry, :polygon) FROM PolygonEntity e WHERE e.id=2",
//    "SELECT symdifference(e.geometry, :polygon) FROM PolygonEntity e WHERE e.id=2",
//    "SELECT geomunion(e.geometry, :linestring) FROM LineStringEntity e WHERE e.id=2",
//
//    "SELECT area(e.geometry) FROM PolygonEntity e WHERE e.id=2",
//    "SELECT centroid(e.geometry) FROM PolygonEntity e WHERE e.id=1",
//    "SELECT pointonsurface(e.geometry) FROM PolygonEntity e WHERE e.id=2",


    private <T> void retrieveHQLResultsAndCompare(Map<Integer, T> dbexpected, String hql) {
        Map<Integer, T> hsreceived = new HashMap<Integer, T>();
        doInSession(hql, hsreceived, null);
        compare(dbexpected, hsreceived);
    }

    private <T> void retrieveHQLResultsAndCompare(Map<Integer, T> dbexpected, String hql, Map<String, Object> params) {
        Map<Integer, T> hsreceived = new HashMap<Integer, T>();
        Geometry geom = expectationsFactory.getTestPolygon();
        doInSession(hql, hsreceived, params);
        compare(dbexpected, hsreceived);
    }

    private <T> void compare(Map<Integer, T> expected, Map<Integer, T> received) {
        for (Integer id : expected.keySet()) {
            LOGGER.debug("Case :" + id);
            LOGGER.debug("expectationsFactory: " + expected.get(id));
            LOGGER.debug("received: " + received.get(id));
            compare(id, expected.get(id), received.get(id));
        }
    }


    private void compare(Integer id, Object expected, Object received) {
        if (expected instanceof byte[]) {
            assertArrayEquals("Failure on test for case " + id, (byte[]) expected, (byte[]) received);

        } else if (expected instanceof GeometryCollection) {
            if (!(received instanceof GeometryCollection)) fail();
            GeometryCollection expectedCollection = (GeometryCollection) expected;
            GeometryCollection receivedCollection = (GeometryCollection) received;
            for (int partIndex = 0; partIndex < expectedCollection.getNumGeometries(); partIndex++) {
                Geometry partExpected = expectedCollection.getGeometryN(partIndex);
                Geometry partReceived = receivedCollection.getGeometryN(partIndex);
                compare(id, partExpected, partReceived);
            }
        } else if (expected instanceof Geometry) {
            //TODO - apparent failure in case of empty geometries??
            assertTrue("Failure on test for case " + id, ((Geometry) expected).equals((Geometry) received));

        } else {
            assertEquals("Failure on test for case " + id, expected, received);
        }
    }

    private <T> void doInSession(String hql, Map<Integer, T> result, Map<String, Object> params) {
        Session session = null;
        try {
            session = factory.openSession();
            Query query = session.createQuery(hql);
            setParameters(params, query);
            addQueryResults(result, query);
        } finally {
            if (session != null) session.close();
        }
    }

    private <T> void addQueryResults(Map<Integer, T> result, Query query) {
        List<Object[]> rows = (List<Object[]>) query.list();
        for (Object[] row : rows) {
            Integer id = (Integer) row[0];
            T val = (T) row[1];
            result.put(id, val);
        }
    }

    private void setParameters(Map<String, Object> params, Query query) {
        if (params == null) return;
        for (String param : params.keySet()) {
            Object value = params.get(param);
            if (value instanceof Geometry) {
                query.setParameter(param, value, new CustomType(GeometryUserType.class, null));
            } else {
                query.setParameter(param, value);
            }
        }
    }

}
