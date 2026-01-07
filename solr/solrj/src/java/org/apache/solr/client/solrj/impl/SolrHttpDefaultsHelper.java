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

/**
 * Helper class to calculate default values for HTTP client and thread pool configurations based on
 * the number of available processors (CPUs).
 *
 * <p>This approach allows Solr to scale better on machines with different CPU counts, rather than
 * using hard-coded values that may be too small for large machines or too large for small ones.
 */
public class SolrHttpDefaultsHelper {

  private static final int NUM_CPUS = Runtime.getRuntime().availableProcessors();

  /**
   * Calculate the default maximum number of connections per host based on CPU count. Uses a
   * multiplier to scale with available processors.
   *
   * <p>Formula: NUM_CPUS * 1000, with a minimum of 1000 and maximum of 100000
   *
   * @return the recommended maximum connections per host
   */
  public static int getDefaultMaxConnectionsPerHost() {
    int calculated = NUM_CPUS * 1000;
    return Math.max(1000, Math.min(100000, calculated));
  }

  /**
   * Calculate the default maximum number of total connections based on CPU count.
   *
   * <p>Formula: NUM_CPUS * 1000, with a minimum of 1000 and maximum of 100000
   *
   * @return the recommended maximum total connections
   */
  public static int getDefaultMaxConnections() {
    int calculated = NUM_CPUS * 1000;
    return Math.max(1000, Math.min(100000, calculated));
  }

  /**
   * Get the number of CPUs available to the JVM.
   *
   * @return the number of available processors
   */
  public static int getNumCPUs() {
    return NUM_CPUS;
  }

  /**
   * Calculate the default maximum pool size for thread pools based on CPU count. This is suitable
   * for I/O-bound operations like HTTP requests.
   *
   * <p>Formula: NUM_CPUS * 32, with a minimum of 32 and no upper limit. This allows for many
   * concurrent HTTP requests while still scaling with available processors.
   *
   * @return the recommended maximum thread pool size
   */
  public static int getDefaultMaxThreadPoolSize() {
    return Math.max(32, NUM_CPUS * 32);
  }

  /**
   * Calculate the default number of recovery threads based on CPU count. Recovery operations are
   * I/O intensive so we allow more threads than CPUs.
   *
   * <p>Formula: NUM_CPUS * 4, with a minimum of 4
   *
   * @return the recommended number of recovery threads
   */
  public static int getDefaultRecoveryThreads() {
    return Math.max(4, NUM_CPUS * 4);
  }
}
