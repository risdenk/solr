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

import org.apache.solr.update.UpdateShardHandlerConfig;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test that CPU-based defaults are properly integrated into configuration classes.
 */
public class CpuBasedDefaultsIntegrationTest {

  @Test
  public void testSolrHttpConstantsUseCpuBasedDefaults() {
    // Verify that the constants use CPU-based calculations
    int expectedConnections = SolrHttpDefaultsHelper.getDefaultMaxConnections();
    int expectedConnectionsPerHost = SolrHttpDefaultsHelper.getDefaultMaxConnectionsPerHost();

    assertEquals(
        "DEFAULT_MAXCONNECTIONS should use CPU-based calculation",
        expectedConnections,
        SolrHttpConstants.DEFAULT_MAXCONNECTIONS);

    assertEquals(
        "DEFAULT_MAXCONNECTIONSPERHOST should use CPU-based calculation",
        expectedConnectionsPerHost,
        SolrHttpConstants.DEFAULT_MAXCONNECTIONSPERHOST);

    // Both should scale with CPU count
    assertTrue(
        "Connection limits should scale with CPU count (at least 1000)",
        SolrHttpConstants.DEFAULT_MAXCONNECTIONS >= 1000);
    assertTrue(
        "Connection limits per host should scale with CPU count (at least 1000)",
        SolrHttpConstants.DEFAULT_MAXCONNECTIONSPERHOST >= 1000);
  }

  @Test
  public void testUpdateShardHandlerConfigUsesDefaults() {
    // Verify that UpdateShardHandlerConfig.DEFAULT uses the CPU-based defaults
    UpdateShardHandlerConfig defaultConfig = UpdateShardHandlerConfig.DEFAULT;

    assertEquals(
        "Default config should use CPU-based max connections",
        SolrHttpConstants.DEFAULT_MAXCONNECTIONS,
        defaultConfig.getMaxUpdateConnections());

    assertEquals(
        "Default config should use CPU-based max connections per host",
        SolrHttpConstants.DEFAULT_MAXCONNECTIONSPERHOST,
        defaultConfig.getMaxUpdateConnectionsPerHost());
  }

  @Test
  public void testTimeoutsAreNotCpuBased() {
    // Timeouts should remain constant, not CPU-based
    assertEquals(
        "Connection timeout should be 60000ms",
        60000,
        SolrHttpConstants.DEFAULT_CONNECT_TIMEOUT);

    assertEquals(
        "Socket timeout should be 600000ms",
        600000,
        SolrHttpConstants.DEFAULT_SO_TIMEOUT);
  }

  @Test
  public void testCpuBasedDefaultsAreReasonable() {
    int cpus = SolrHttpDefaultsHelper.getNumCPUs();
    int maxConnections = SolrHttpConstants.DEFAULT_MAXCONNECTIONS;
    int maxThreadPoolSize = SolrHttpDefaultsHelper.getDefaultMaxThreadPoolSize();

    // Verify relationships make sense
    assertTrue(
        "Max connections should be reasonable for CPU count",
        maxConnections >= 1000 && maxConnections <= 100000);

    assertTrue(
        "Max thread pool size should be at least as many as CPUs",
        maxThreadPoolSize >= cpus);

    // For a typical server (e.g., 8 CPUs), we should get reasonable values
    // 8 CPUs -> 8000 connections, 256 threads
    if (cpus == 8) {
      assertEquals("8 CPUs should give 8000 connections", 8000, maxConnections);
      assertEquals("8 CPUs should give 256 thread pool max", 256, maxThreadPoolSize);
    }
  }
}
