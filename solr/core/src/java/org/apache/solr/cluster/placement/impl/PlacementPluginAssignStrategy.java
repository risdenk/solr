/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.cluster.placement.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.cloud.SolrCloudManager;
import org.apache.solr.cloud.api.collections.Assign;
import org.apache.solr.cluster.placement.DeleteCollectionRequest;
import org.apache.solr.cluster.placement.DeleteReplicasRequest;
import org.apache.solr.cluster.placement.PlacementContext;
import org.apache.solr.cluster.placement.PlacementException;
import org.apache.solr.cluster.placement.PlacementPlugin;
import org.apache.solr.cluster.placement.PlacementPlan;
import org.apache.solr.cluster.placement.PlacementRequest;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.ReplicaPosition;

/**
 * This assign strategy delegates placement computation to "plugin" code.
 */
public class PlacementPluginAssignStrategy implements Assign.AssignStrategy {

  private final PlacementPlugin plugin;

  public PlacementPluginAssignStrategy(PlacementPlugin plugin) {
    this.plugin = plugin;
  }

  public List<ReplicaPosition> assign(SolrCloudManager solrCloudManager, List<Assign.AssignRequest> assignRequests)
      throws Assign.AssignmentException, IOException, InterruptedException {

    PlacementContext placementContext = new SimplePlacementContextImpl(solrCloudManager);

    List<PlacementRequest> placementRequests = new ArrayList<>(assignRequests.size());
    for (Assign.AssignRequest assignRequest : assignRequests) {
      placementRequests.add(PlacementRequestImpl.toPlacementRequest(placementContext.getCluster(), placementContext.getCluster().getCollection(assignRequest.collectionName), assignRequest));
    }

    final List<ReplicaPosition> replicaPositions = new ArrayList<>();
    try {
      List<PlacementPlan> placementPlans = plugin.computePlacements(placementRequests, placementContext);
      if (placementPlans != null) {
        for (PlacementPlan placementPlan : placementPlans) {
          replicaPositions.addAll(ReplicaPlacementImpl.toReplicaPositions(placementPlan.getRequest().getCollection().getName(), placementPlan.getReplicaPlacements()));
        }
      }
    } catch (PlacementException pe) {
      throw new Assign.AssignmentException(pe);
    }

    return replicaPositions;
  }

  @Override
  public void verifyDeleteCollection(SolrCloudManager solrCloudManager, DocCollection collection) throws Assign.AssignmentException, IOException, InterruptedException {
    PlacementContext placementContext = new SimplePlacementContextImpl(solrCloudManager);
    DeleteCollectionRequest modificationRequest = ModificationRequestImpl.createDeleteCollectionRequest(collection);
    try {
      plugin.verifyAllowedModification(modificationRequest, placementContext);
    } catch (PlacementException pe) {
      throw new Assign.AssignmentException(pe);
    }
  }

  @Override
  public void verifyDeleteReplicas(SolrCloudManager solrCloudManager, DocCollection collection, String shardId, Set<Replica> replicas) throws Assign.AssignmentException, IOException, InterruptedException {
    PlacementContext placementContext = new SimplePlacementContextImpl(solrCloudManager);
    DeleteReplicasRequest modificationRequest = ModificationRequestImpl.createDeleteReplicasRequest(collection, shardId, replicas);
    try {
      plugin.verifyAllowedModification(modificationRequest, placementContext);
    } catch (PlacementException pe) {
      throw new Assign.AssignmentException(pe);
    }
  }
}
