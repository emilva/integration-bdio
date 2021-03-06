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
package com.blackducksoftware.integration.hub.bdio.graph;

import java.util.HashSet;
import java.util.Set;

import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency;

public class DependencyGraphCombiner {
    public void addGraphAsChildrenToRoot(final MutableDependencyGraph destinationGraph, final DependencyGraph sourceGraph) {
        final Set<Dependency> encountered = new HashSet<>();
        for (final Dependency dependency : sourceGraph.getRootDependencies()) {
            destinationGraph.addChildToRoot(dependency);
            copyDependencyFromGraph(destinationGraph, dependency, sourceGraph, encountered);
        }
    }

    public void addGraphAsChildrenToParent(final MutableDependencyGraph destinationGraph, final Dependency parent, final DependencyGraph sourceGraph) {
        final Set<Dependency> encountered = new HashSet<>();
        for (final Dependency dependency : sourceGraph.getRootDependencies()) {
            destinationGraph.addChildWithParent(dependency, parent);
            copyDependencyFromGraph(destinationGraph, dependency, sourceGraph, encountered);
        }
    }

    public void copyDependencyFromGraph(final MutableDependencyGraph destinationGraph, final Dependency parentDependency, final DependencyGraph sourceGraph, final Set<Dependency> encountered) {
        for (final Dependency dependency : sourceGraph.getChildrenForParent(parentDependency)) {
            if (!encountered.contains(dependency)) {
                encountered.add(dependency);

                copyDependencyFromGraph(destinationGraph, dependency, sourceGraph, encountered);
            }
            destinationGraph.addChildWithParent(dependency, parentDependency);
        }
    }

}
