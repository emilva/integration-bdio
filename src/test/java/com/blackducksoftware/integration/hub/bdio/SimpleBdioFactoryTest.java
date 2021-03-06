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
package com.blackducksoftware.integration.hub.bdio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraphTransformer;
import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.SimpleBdioDocument;
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.bdio.utility.JsonTestUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SimpleBdioFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testConstructor() {
        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);
        final DependencyGraphTransformer dependencyGraphTransformer = new DependencyGraphTransformer(bdioPropertyHelper, bdioNodeFactory);
        final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        assertNotNull(simpleBdioFactory);
        assertNotNull(simpleBdioFactory.getBdioPropertyHelper());
        assertNotNull(simpleBdioFactory.getBdioNodeFactory());
        assertNotNull(simpleBdioFactory.getDependencyGraphTransformer());
        assertNotNull(simpleBdioFactory.getExternalIdFactory());

        assertFalse(bdioPropertyHelper == simpleBdioFactory.getBdioPropertyHelper());
        assertFalse(bdioNodeFactory == simpleBdioFactory.getBdioNodeFactory());
        assertFalse(dependencyGraphTransformer == simpleBdioFactory.getDependencyGraphTransformer());
        assertFalse(externalIdFactory == simpleBdioFactory.getExternalIdFactory());
    }

    @Test
    public void testDependencyInjectionConstructor() {
        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);
        final DependencyGraphTransformer dependencyGraphTransformer = new DependencyGraphTransformer(bdioPropertyHelper, bdioNodeFactory);
        final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory(bdioPropertyHelper, bdioNodeFactory, dependencyGraphTransformer, externalIdFactory);

        assertNotNull(simpleBdioFactory);
        assertNotNull(simpleBdioFactory.getBdioPropertyHelper());
        assertNotNull(simpleBdioFactory.getBdioNodeFactory());
        assertNotNull(simpleBdioFactory.getDependencyGraphTransformer());

        assertTrue(bdioPropertyHelper == simpleBdioFactory.getBdioPropertyHelper());
        assertTrue(bdioNodeFactory == simpleBdioFactory.getBdioNodeFactory());
        assertTrue(dependencyGraphTransformer == simpleBdioFactory.getDependencyGraphTransformer());
        assertTrue(externalIdFactory == simpleBdioFactory.getExternalIdFactory());
    }

    @Test
    public void testConstructingBdioWriters() throws IOException {
        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        final Gson gson = new Gson();
        final Writer writer = new StringWriter();
        final OutputStream outputStream = new ByteArrayOutputStream();

        final BdioWriter writerBdioWriter = simpleBdioFactory.createBdioWriter(gson, writer);
        assertNotNull(writerBdioWriter);

        final BdioWriter outputStreamBdioWriter = simpleBdioFactory.createBdioWriter(gson, outputStream);
        assertNotNull(outputStreamBdioWriter);
    }

    @Test
    public void testTryFinally() throws IOException {
        final SimpleBdioFactory simpleBdioFactory = Mockito.spy(new SimpleBdioFactory());
        Mockito.doThrow(RuntimeException.class).when(simpleBdioFactory).createBdioWriter(Mockito.any(Gson.class), Mockito.any(OutputStream.class));
        final SimpleBdioDocument simpleBdioDocument = createSimpleBdioDocument(simpleBdioFactory);

        final File bdioFile = File.createTempFile("bdio", "jsonld");
        bdioFile.deleteOnExit();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        expectedException.expect(RuntimeException.class);
        simpleBdioFactory.writeSimpleBdioDocumentToFile(bdioFile, gson, simpleBdioDocument);
    }

    @Test
    public void testTryFinally2() throws IOException {
        final SimpleBdioFactory simpleBdioFactory = Mockito.spy(new SimpleBdioFactory());
        Mockito.doThrow(RuntimeException.class).when(simpleBdioFactory).writeSimpleBdioDocument(Mockito.any(BdioWriter.class), Mockito.any(SimpleBdioDocument.class));
        final SimpleBdioDocument simpleBdioDocument = createSimpleBdioDocument(simpleBdioFactory);

        final File bdioFile = File.createTempFile("bdio", "jsonld");
        bdioFile.deleteOnExit();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        expectedException.expect(RuntimeException.class);
        simpleBdioFactory.writeSimpleBdioDocumentToFile(bdioFile, gson, simpleBdioDocument);
    }

    @Test
    public void testWritingBdioToFile() throws IOException, URISyntaxException, JSONException {
        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        final SimpleBdioDocument simpleBdioDocument = createSimpleBdioDocument(simpleBdioFactory);

        final File bdioFile = File.createTempFile("bdio", "jsonld");
        bdioFile.deleteOnExit();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        assertEquals(0, bdioFile.length());

        // overriding default UUID so the expected value matches the actual value
        simpleBdioDocument.billOfMaterials.id = "uuid:static-uuid-for-testing";
        simpleBdioFactory.writeSimpleBdioDocumentToFile(bdioFile, gson, simpleBdioDocument);

        assertNotEquals(0, bdioFile.length());

        final JsonTestUtils jsonTestUtils = new JsonTestUtils();
        final String expectedJson = jsonTestUtils.getExpectedJson("simple-bdio-factory-integration-test-output.jsonld");
        final String actualJson = IOUtils.toString(new FileInputStream(bdioFile), StandardCharsets.UTF_8);

        jsonTestUtils.verifyJsonArraysEqual(expectedJson, actualJson, false);
    }

    private SimpleBdioDocument createSimpleBdioDocument(final SimpleBdioFactory simpleBdioFactory) {
        final MutableDependencyGraph mutableDependencyGraph = simpleBdioFactory.createMutableDependencyGraph();

        final Dependency bdioTestDependency = simpleBdioFactory.createDependency("bdio-test", "1.1.2", simpleBdioFactory.getExternalIdFactory().createMavenExternalId("com.blackducksoftware.integration", "bdio-test", "1.1.2"));
        final Dependency bdioReaderDependency = simpleBdioFactory.createDependency("bdio-reader", "1.2.0", simpleBdioFactory.getExternalIdFactory().createMavenExternalId("com.blackducksoftware.integration", "bdio-reader", "1.2.0"));
        final Dependency commonsLangDependency = simpleBdioFactory.createDependency("commons-lang3", "3.6", simpleBdioFactory.getExternalIdFactory().createMavenExternalId("org.apache.commons", "commons-lang3", "3.6"));

        mutableDependencyGraph.addChildrenToRoot(bdioTestDependency);
        mutableDependencyGraph.addChildrenToRoot(bdioReaderDependency);
        mutableDependencyGraph.addChildWithParent(commonsLangDependency, bdioReaderDependency);

        final SimpleBdioDocument simpleBdioDocument = simpleBdioFactory.createSimpleBdioDocument("test code location", "integration-bdio", "0.0.1",
                simpleBdioFactory.createMavenExternalId("com.blackducksoftware.integration", "integration-bdio", "0.0.1"), mutableDependencyGraph);
        return simpleBdioDocument;
    }

}
