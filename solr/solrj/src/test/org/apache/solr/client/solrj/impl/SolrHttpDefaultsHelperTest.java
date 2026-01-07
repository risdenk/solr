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

package org.apache.solr.client.solrj.impl;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for SolrHttpDefaultsHelper to ensure CPU-based defaults are calculated correctly.
 */
public class SolrHttpDefaultsHelperTest {

  @Test
  public void testGetNumCPUs() {
    int cpus = SolrHttpDefaultsHelper.getNumCPUs();
    assertTrue("Number of CPUs should be positive", cpus > 0);
    assertEquals(
        "Should match Runtime value",
        Runtime.getRuntime().availableProcessors(),
        cpus);
  }

  @Test
  public void testGetDefaultMaxConnectionsPerHost() {
    int maxConnections = SolrHttpDefaultsHelper.getDefaultMaxConnectionsPerHost();

    // Should be within acceptable bounds
    assertTrue("Max connections per host should be at least 1000", maxConnections >= 1000);
    assertTrue("Max connections per host should not exceed 100000", maxConnections <= 100000);

    // Should scale with CPUs
    int cpus = SolrHttpDefaultsHelper.getNumCPUs();
    int expected = Math.max(1000, Math.min(100000, cpus * 1000));
    assertEquals("Should match expected calculation", expected, maxConnections);
  }

  @Test
  public void testGetDefaultMaxConnections() {
    int maxConnections = SolrHttpDefaultsHelper.getDefaultMaxConnections();

    // Should be within acceptable bounds
    assertTrue("Max connections should be at least 1000", maxConnections >= 1000);
    assertTrue("Max connections should not exceed 100000", maxConnections <= 100000);

    // Should scale with CPUs
    int cpus = SolrHttpDefaultsHelper.getNumCPUs();
    int expected = Math.max(1000, Math.min(100000, cpus * 1000));
    assertEquals("Should match expected calculation", expected, maxConnections);
  }

  @Test
  public void testConnectionLimitsAreConsistent() {
    // Both connection limits should be the same by default
    assertEquals(
        "Max connections and max connections per host should be equal",
        SolrHttpDefaultsHelper.getDefaultMaxConnections(),
        SolrHttpDefaultsHelper.getDefaultMaxConnectionsPerHost());
  }

  @Test
  public void testConnectionsScaleWithCPUs() {
    int cpus = SolrHttpDefaultsHelper.getNumCPUs();
    int maxConnections = SolrHttpDefaultsHelper.getDefaultMaxConnections();

    // If we have between 1 and 100 CPUs, the formula should be straightforward
    if (cpus >= 1 && cpus <= 100) {
      assertEquals("Should equal CPUs * 1000", cpus * 1000, maxConnections);
    }
    // If we have more than 100 CPUs, should cap at 100000
    else if (cpus > 100) {
      assertEquals("Should cap at 100000", 100000, maxConnections);
    }
  }
}
