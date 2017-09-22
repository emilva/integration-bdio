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
package com.blackducksoftware.integration.hub.bdio.simple.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class BdioNode {
    @SerializedName("@id")
    public String id;

    @SerializedName("@type")
    public String type;

    @SerializedName("externalIdentifier")
    public BdioExternalIdentifier bdioExternalIdentifier;

    @SerializedName("customData")
    public Map<String, String> customData;

    @SerializedName("relationship")
    public List<BdioRelationship> relationships = new ArrayList<>();

    public String putCustomData(final String key, final String value) {
        if (customData == null) {
            customData = new HashMap<>();
        }
        return customData.put(key, value);
    }
}
