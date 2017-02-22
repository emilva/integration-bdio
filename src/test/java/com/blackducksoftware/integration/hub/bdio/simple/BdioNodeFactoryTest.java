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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONParser;

import com.blackducksoftware.integration.hub.bdio.simple.model.BdioBillOfMaterials;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioComponent;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioExternalIdentifier;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioNode;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioProject;
import com.google.gson.Gson;

public class BdioNodeFactoryTest {
    @Test
    public void testFactory() throws FileNotFoundException, IOException, URISyntaxException, JSONException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("sample.jsonld");
        final File file = new File(url.toURI().getPath());
        final String expectedJson = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);

        final String projectGroup = "com.blackducksoftware.gradle.test";
        final String projectName = "gradleTestProject";
        final String projectVersion = "99.5-SNAPSHOT";
        final String projectBdioId = bdioPropertyHelper.createBdioId(projectGroup, projectName, projectVersion);
        final BdioExternalIdentifier projectExternalIdentifier = bdioPropertyHelper.createMavenExternalIdentifier(projectGroup, projectName, projectVersion);

        final BdioBillOfMaterials bdioBillOfMaterials = bdioNodeFactory.createBillOfMaterials(projectName);
        // we are overriding the default value of a new uuid just to pass the json comparison
        bdioBillOfMaterials.id = "uuid:45772d33-5353-44f1-8681-3d8a15540646";

        final BdioProject bdioProject = bdioNodeFactory.createProject(projectName, projectVersion, projectBdioId, projectExternalIdentifier);

        final BdioComponent cxfBundle = bdioNodeFactory.createComponent("cxf-bundle", "2.7.7",
                bdioPropertyHelper.createBdioId("org.apache.cxf", "cxf-bundle", "2.7.7"),
                bdioPropertyHelper.createMavenExternalIdentifier("org.apache.cxf", "cxf-bundle", "2.7.7"));
        final BdioComponent velocity = bdioNodeFactory.createComponent("velocity", "1.7",
                bdioPropertyHelper.createBdioId("org.apache.velocity", "velocity", "1.7"),
                bdioPropertyHelper.createMavenExternalIdentifier("org.apache.velocity", "velocity", "1.7"));
        final BdioComponent commonsCollections = bdioNodeFactory.createComponent("commons-collections", "3.2.1",
                bdioPropertyHelper.createBdioId("commons-collections", "commons-collections", "3.2.1"),
                bdioPropertyHelper.createMavenExternalIdentifier("commons-collections", "commons-collections", "3.2.1"));
        final BdioComponent commonsLang = bdioNodeFactory.createComponent("commons-lang", "2.6",
                bdioPropertyHelper.createBdioId("commons-lang", "commons-lang", "2.6"),
                bdioPropertyHelper.createMavenExternalIdentifier("commons-lang", "commons-lang", "2.6"));

        // we will now relate the constructed bdio nodes

        // first, add the cxfBundle component as a child of the project - this project has a single direct dependency
        bdioPropertyHelper.addRelationship(bdioProject, cxfBundle);

        // now, the cxfBundle component itself has two dependencies, which will appear in the final BOM as they are
        // transitive dependencies of the project
        bdioPropertyHelper.addRelationship(cxfBundle, velocity);
        bdioPropertyHelper.addRelationship(cxfBundle, commonsLang);

        // and the velocity component also has two dependencies - it will only add one additional entry to our final BOM
        // as the commonsLang component was already included from the cxfBundle component above
        bdioPropertyHelper.addRelationship(velocity, commonsCollections);
        bdioPropertyHelper.addRelationship(velocity, commonsLang);

        final List<BdioNode> bdioNodes = new ArrayList<>();
        bdioNodes.add(bdioBillOfMaterials);
        bdioNodes.add(bdioProject);
        bdioNodes.add(cxfBundle);
        bdioNodes.add(velocity);
        bdioNodes.add(commonsCollections);
        bdioNodes.add(commonsLang);

        // we simply write the final structure out to a String so we can compare what is generated to a stock file
        final StringWriter writer = new StringWriter();
        try (BdioWriter bdioWriter = new BdioWriter(new Gson(), writer)) {
            bdioWriter.writeBdioNodes(bdioNodes);
        }

        final JSONArray expected = (JSONArray) JSONParser.parseJSON(expectedJson);
        final JSONArray actual = (JSONArray) JSONParser.parseJSON(writer.toString());
        assertEquals(expected.length(), actual.length());
        JSONAssert.assertEquals(expected, actual, false);
    }

}
