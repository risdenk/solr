# CPU-Based Defaults for Thread Pools and HTTP Connections

## Overview

As of this update, Apache Solr now uses CPU-based defaults for thread pool sizes and HTTP connection limits instead of hard-coded values. This allows Solr to automatically scale its configuration based on the available hardware, providing better out-of-the-box performance across different machine sizes.

## Changed Default Values

### HTTP Connection Limits

The following constants in `SolrHttpConstants` now use CPU-based calculations:

- **DEFAULT_MAXCONNECTIONS**: `Number of CPUs × 1000` (min: 1000, max: 100000)
- **DEFAULT_MAXCONNECTIONSPERHOST**: `Number of CPUs × 1000` (min: 1000, max: 100000)

**Example:**
- On a 4-CPU machine: 4,000 connections
- On an 8-CPU machine: 8,000 connections
- On a 16-CPU machine: 16,000 connections
- On a 128-CPU machine: 100,000 connections (capped at maximum)

### Thread Pool Sizes

The `HttpShardHandlerFactory` now uses CPU-based defaults for thread pool configuration:

- **maximumPoolSize**: `Number of CPUs × 4` (min: 32)

**Example:**
- On a 4-CPU machine: 32 threads maximum (minimum)
- On an 8-CPU machine: 32 threads maximum
- On a 16-CPU machine: 64 threads maximum
- On a 32-CPU machine: 128 threads maximum

### Recovery Thread Pool (Future Enhancement)

A helper method is available for recovery thread calculations:

- **getDefaultRecoveryThreads()**: `Number of CPUs × 4` (min: 4)

## Benefits

1. **Automatic Scaling**: Solr adapts to the available hardware without manual tuning
2. **Better Performance**: Machines with more CPUs can handle more concurrent operations
3. **Resource Efficiency**: Machines with fewer CPUs don't over-allocate resources
4. **Container-Friendly**: CPU count is retrieved dynamically, supporting containerized environments where CPU limits may change

## Backward Compatibility

All existing explicit configurations continue to work as before. If you have configured custom values in your `solr.xml` or other configuration files, those values will be used instead of the CPU-based defaults.

Example configuration in `solr.xml`:
```xml
<shardHandlerFactory name="shardHandlerFactory" class="HttpShardHandlerFactory">
  <int name="maximumPoolSize">500</int>
  <int name="maxConnectionsPerHost">20000</int>
</shardHandlerFactory>
```

## Implementation Details

The new defaults are provided by the `SolrHttpDefaultsHelper` class in the `org.apache.solr.client.solrj.impl` package. This utility class:

- Uses `Runtime.getRuntime().availableProcessors()` to determine CPU count
- Calculates appropriate defaults based on sensible multipliers
- Applies minimum and maximum bounds to ensure reasonable values
- Dynamically retrieves CPU count (not cached) to support containerized environments

## Migration Notes

For most users, no action is required. The new defaults will automatically apply when upgrading.

However, if you have tuned your configuration for a specific hard-coded default (e.g., 100,000 connections), you may want to review whether explicit configuration is still needed after the upgrade, especially if:

1. You're running on a machine with fewer than 100 CPUs
2. You want to maintain the old behavior exactly

## Related Classes

- `org.apache.solr.client.solrj.impl.SolrHttpDefaultsHelper` - Provides CPU-based calculations
- `org.apache.solr.client.solrj.impl.SolrHttpConstants` - HTTP connection constants
- `org.apache.solr.handler.component.HttpShardHandlerFactory` - Distributed search thread pools
- `org.apache.solr.update.UpdateShardHandlerConfig` - Update handler configuration

## References

- SOLR-XXXXX: Make thread pools and HTTP timeouts CPU-based instead of hard-coded
