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
package com.blackducksoftware.integration.hub.bdio.simple;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.hub.bdio.simple.model.BdioBillOfMaterials;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioComponent;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioProject;
import com.blackducksoftware.integration.hub.bdio.simple.model.SimpleBdioDocument;
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalIdFactory;
import com.google.gson.Gson;

public class BdioNodeFactoryTest {
    private final JsonTestUtils jsonTestUtils = new JsonTestUtils();
    private final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    @Test
    public void testWriterOutput() throws FileNotFoundException, IOException, URISyntaxException, JSONException {
        final String expectedJson = jsonTestUtils.getExpectedJson("sample.jsonld");

        // we simply write the final structure out through a StringWriter so we can compare what is generated to a stock
        // file
        final Writer writer = new StringWriter();
        try (BdioWriter bdioWriter = new BdioWriter(new Gson(), writer)) {
            bdioWriter.writeSimpleBdioDocument(getSimpleBdioDocument());
        }

        final String actualJson = writer.toString();
        jsonTestUtils.verifyJsonArraysEqual(expectedJson, actualJson);
    }

    @Test
    public void testOutputStreamOutput() throws FileNotFoundException, IOException, URISyntaxException, JSONException {
        final String expectedJson = jsonTestUtils.getExpectedJson("sample.jsonld");

        // we simply write the final structure out through a ByteArrayOutputStream so we can compare what is generated
        // to a stock file
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BdioWriter bdioWriter = new BdioWriter(new Gson(), outputStream)) {
            bdioWriter.writeSimpleBdioDocument(getSimpleBdioDocument());
        }

        final String actualJson = outputStream.toString(StandardCharsets.UTF_8.name());
        jsonTestUtils.verifyJsonArraysEqual(expectedJson, actualJson);
    }

    @Test
    public void testCodeLocationOverride() {
        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);
        BdioBillOfMaterials bdioBillOfMaterials = bdioNodeFactory.createBillOfMaterials("", "name", "version");
        assertEquals("name/version Black Duck I/O Export", bdioBillOfMaterials.spdxName);

        bdioBillOfMaterials = bdioNodeFactory.createBillOfMaterials("override", "name", "version");
        assertEquals("override", bdioBillOfMaterials.spdxName);
    }

    private SimpleBdioDocument getSimpleBdioDocument() {
        final SimpleBdioDocument simpleBdioDocument = new SimpleBdioDocument();

        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);

        final String projectGroup = "com.blackducksoftware.gradle.test";
        final String projectName = "gradleTestProject";
        final String projectVersion = "99.5-SNAPSHOT";
        final ExternalId mavenExternalId = externalIdFactory.createMavenExternalId(projectGroup, projectName, projectVersion);
        final String projectExternalId = mavenExternalId.createExternalId();
        final String projectBdioId = mavenExternalId.createBdioId();

        final BdioBillOfMaterials bdioBillOfMaterials = bdioNodeFactory.createBillOfMaterials("", projectName, projectVersion);
        bdioBillOfMaterials.putCustomData("testVersion", "1.2.3-SNAPSHOT");
        // we are overriding the default value of a new uuid just to pass the json comparison
        bdioBillOfMaterials.id = "uuid:45772d33-5353-44f1-8681-3d8a15540646";

        final BdioProject bdioProject = bdioNodeFactory.createProject(projectName, projectVersion, projectBdioId, "maven", projectExternalId);

        final ExternalId cxfBundleExternalId = externalIdFactory.createMavenExternalId("org.apache.cxf", "cxf-bundle", "2.7.7");
        final BdioComponent cxfBundle = bdioNodeFactory.createComponent("cxf-bundle", "2.7.7", cxfBundleExternalId);

        final ExternalId velocityExternalId = externalIdFactory.createMavenExternalId("org.apache.velocity", "velocity", "1.7");
        final BdioComponent velocity = bdioNodeFactory.createComponent("velocity", "1.7", velocityExternalId);

        final ExternalId commonsCollectionsExternalId = externalIdFactory.createMavenExternalId("commons-collections", "commons-collections", "3.2.1");
        final BdioComponent commonsCollections = bdioNodeFactory.createComponent("commons-collections", "3.2.1", commonsCollectionsExternalId);

        final ExternalId commonsLangExternalId = externalIdFactory.createMavenExternalId("commons-lang", "commons-lang", "2.6");
        final BdioComponent commonsLang = bdioNodeFactory.createComponent("commons-lang", "2.6", commonsLangExternalId);

        // we will now relate the constructed bdio nodes

        // first, add the cxfBundle component as a child of the project - this project has a single direct dependency
        bdioPropertyHelper.addRelationship(bdioProject, cxfBundle);

        // now, the cxfBundle component itself has two dependencies, which will appear in the final BOM as they are
        // transitive dependencies of the project
        bdioPropertyHelper.addRelationships(cxfBundle, Arrays.asList(velocity, commonsLang));

        // and the velocity component also has two dependencies - it will only add one additional entry to our final BOM
        // as the commonsLang component was already included from the cxfBundle component above
        bdioPropertyHelper.addRelationships(velocity, Arrays.asList(commonsCollections, commonsLang));

        final List<BdioComponent> bdioComponents = new ArrayList<>();
        bdioComponents.add(cxfBundle);
        bdioComponents.add(velocity);
        bdioComponents.add(commonsCollections);
        bdioComponents.add(commonsLang);

        simpleBdioDocument.billOfMaterials = bdioBillOfMaterials;
        simpleBdioDocument.project = bdioProject;
        simpleBdioDocument.components = bdioComponents;

        return simpleBdioDocument;
    }

}
