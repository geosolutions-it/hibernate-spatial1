/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
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
 * For more information, visit: http://www.cadrie.com/
 */
 
package com.cadrie.hibernate.spatial.postgis.test;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cadrie.hibernate.spatial.test.TestSpatialQueries;

public class TestPostgisSpatialQueries {

    private final static String DBNAME = "test";

    private static Connection conn;

    private static TestSpatialQueries delegate;

    static {
        String url = "jdbc:postgresql://localhost:5432/" + DBNAME;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, "postgres", "");
            delegate = new TestSpatialQueries();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create the delegate

    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        delegate.setUpBeforeClass(conn);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        delegate.tearDownAfterClass();
    }

    @Test
    public void testHGLGeometryType() throws Exception {
        delegate.testHGLGeometryType();
    }

    @Test
    public void testHQLAsBinary() throws Exception {
        String sql = "select asbinary(l.geom) from linestringtest as l where id = ?";
        delegate.testHQLAsBinary(sql);
    }

    @Test
    public void testHQLAsTextLineString() throws Exception {
        delegate.testHQLAsTextLineString();
    }

    @Test
    public void testHQLDimensionLineString() throws Exception {
        delegate.testHQLDimensionLineString();
    }

    @Test
    public void testHQLEnvelope() throws Exception {
        delegate.testHQLEnvelope();
    }

    @Test
    public void testHQLIntersectsLineString() throws Exception {
        String sqlString = "select count(*) from linestringtest where intersects(geomfromtext(?, 31370), geom)";
        delegate.testHQLIntersectsLineString(sqlString);
    }

    @Test
    public void testHQLIsEmpty() throws Exception {
        String sql = "select isempty(geom) from linestringtest where id = ? and geom is not null";
        delegate.testHQLIsEmpty(sql);
    }

    @Test
    public void testHQLIsSimple() throws Exception {
        String sql = "select issimple(geom) from linestringtest where id = ? and geom is not null";
        delegate.testHQLIsSimple(sql);
    }

    @Test
    public void testHQLOverlapsLineString() throws Exception {
        String sql = "select count(*) from linestringtest where overlaps(geomfromtext(?, 31370), geom) and geom is not null";
        delegate.testHQLOverlapsLineString(sql);
    }

    @Test
    public void testHQLSRID() throws Exception {
        delegate.testHQLSRID();
    }

    @Test
    public void testLineStringFiltering() throws Exception {
        String sql = "select count(*) from linestringtest where geom && geomFromText(?,31370)";
        delegate.testLineStringFiltering(sql);
    }

    @Test
    public void testMultiLineStringFiltering() throws Exception {
        String sql = "select count(*) from multilinestringtest where geom && geomFromText(?,31370)";
        delegate.testMultiLineStringFiltering(sql);
    }

    @Test
    public void testPolygonFiltering() throws Exception {
        String sql = "select count(*) from polygontest where geom && geomFromText(?,31370)";
        delegate.testPolygonFiltering(sql);
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestPostgisSpatialQueries.class);
    }

    @Test
    public void testHQLBoundary() throws Exception {
        delegate.testHQLBoundary();
    }

    @Test
    public void testHQLRelateLineString() throws Exception {
        delegate.testHQLRelateLineString();
    }

    @Test
    public void testHQLDistance() throws Exception {
        delegate.testHQLDistance();
    }

    @Test
    public void testHQLBuffer() throws Exception {
        delegate.testHQLBuffer();
    }

    @Test
    public void testHQLConvexHull() throws Exception {
        delegate.testHQLConvexHull();
    }

    @Test
    public void testHQLIntersection() throws Exception {
        delegate.testHQLIntersection();
    }

    @Test
    public void testHQLDifference() throws Exception {
        delegate.testHQLDifference();
    }

    @Test
    public void testHQLSymDifference() throws Exception {
        delegate.testHQLSymDifference();
    }

    @Test
    public void testHQLUnion() throws Exception {
        delegate.testHQLUnion();
    }

}