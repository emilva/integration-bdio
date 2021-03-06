/**
 * Integration Bdio
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.bdio.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.bdio.model.dependencyid.DependencyId;
import com.blackducksoftware.integration.hub.bdio.model.dependencyid.NameDependencyId;
import com.blackducksoftware.integration.hub.bdio.model.dependencyid.NameVersionDependencyId;
import com.blackducksoftware.integration.hub.bdio.model.dependencyid.StringDependencyId;

public class DependencyIdTest {

    @Test
    public void testStringDependencyId() {
        final DependencyId id1 = new StringDependencyId("hello");
        final DependencyId id2 = new StringDependencyId("hello");
        final DependencyId idDiff = new StringDependencyId("goodbye");

        assertEquals(id1, id2);
        assertNotEquals(id1, idDiff);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), idDiff.hashCode());
        assertEquals(id1.toString(), id2.toString());
        assertNotEquals(id1.toString(), idDiff.toString());

    }

    @Test
    public void testNameDependencyId() {
        final DependencyId id1 = new NameDependencyId("hello");
        final DependencyId id2 = new NameDependencyId("hello");
        final DependencyId idDiff = new NameDependencyId("goodbye");

        assertEquals(id1, id2);
        assertNotEquals(id1, idDiff);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), idDiff.hashCode());
        assertEquals(id1.toString(), id2.toString());
        assertNotEquals(id1.toString(), idDiff.toString());
    }

    @Test
    public void testNameVersionDependencyId() {
        final DependencyId id1 = new NameVersionDependencyId("hello", "hi");
        final DependencyId id2 = new NameVersionDependencyId("hello", "hi");
        final DependencyId idDiff = new NameVersionDependencyId("hello", "bye");

        assertEquals(id1, id2);
        assertNotEquals(id1, idDiff);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), idDiff.hashCode());
        assertEquals(id1.toString(), id2.toString());
        assertNotEquals(id1.toString(), idDiff.toString());
    }

    @Test
    public void testDiffDependencyId() {
        final DependencyId id1 = new NameDependencyId("hello");
        final DependencyId id2 = new NameVersionDependencyId("hello", "hi");
        final DependencyId id3 = new NameDependencyId("goodbye");

        assertNotEquals(id1, id2);
        assertNotEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.toString(), id2.toString());

        assertNotEquals(id1, id3);
        assertNotEquals(id1.hashCode(), id3.hashCode());
        assertNotEquals(id1.toString(), id3.toString());

        assertNotEquals(id2, id3);
        assertNotEquals(id2.hashCode(), id3.hashCode());
        assertNotEquals(id2.toString(), id3.toString());

    }

}
