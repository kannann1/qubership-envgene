# Parameter Transformation in EnvGene

This document describes the principles by which parameters are transformed in EnvGene, particularly during the CMDB import process. It covers both standard parameter transformations and the new multiline complex parameter support using native YAML block styles.

## Table of Contents

1. [Overview](#overview)
2. [Current Parameter Transformation](#current-parameter-transformation)
   - [Simple Parameters](#simple-parameters)
   - [Complex Parameters](#complex-parameters)
   - [Credential Parameters](#credential-parameters)
3. [New Multiline YAML Parameter Support](#new-multiline-yaml-parameter-support)
   - [Why Multiline YAML?](#why-multiline-yaml)
   - [How to Use](#how-to-use)
   - [Technical Implementation](#technical-implementation)
4. [Examples](#examples)
   - [Before: Single-line JSON Format](#before-single-line-json-format)
   - [After: Multiline YAML Format](#after-multiline-yaml-format)
5. [Best Practices](#best-practices)

## Overview

EnvGene transforms parameters from their original format in Git repositories to a format suitable for CMDB import. This transformation follows specific rules depending on the parameter type and complexity.

## Current Parameter Transformation

### Simple Parameters

Simple parameters (strings, numbers, booleans) are passed through with minimal transformation:

```yaml
# In Git
simple_param: "value"

# In CMDB
simple_param="value";
```

### Complex Parameters

Complex parameters (maps, lists, nested structures) are transformed into escaped JSON strings:

```yaml
# In Git
complex_param:
  key1: value1
  key2: 
    nested: value2
  list:
    - item1
    - item2

# In CMDB
complex_param='{"key1":"value1","key2":{"nested":"value2"},"list":["item1","item2"]}';
```

This transformation makes complex parameters difficult to read and maintain in the CMDB UI.

## Problem Statement

EnvGene currently transforms complex parameters (maps, lists, nested structures) into single-line JSON strings during CMDB import. This transformation makes complex parameters difficult to read and maintain in the CMDB UI, especially for troubleshooting purposes.

For example, a complex parameter structure like:

```yaml
global:
  name: zookeeper
  secrets:
    zooKeeper:
      adminUsername: zadmin
      adminPassword: zadmin
```

Is transformed into:

```bash
global='{"name":"zookeeper","secrets":{"zooKeeper":{"adminUsername":"zadmin","adminPassword":"zadmin"}}}';
```

This single-line format becomes increasingly unreadable as parameter complexity grows, making it challenging to troubleshoot and maintain parameters directly in the CMDB UI.

## New Multiline YAML Parameter Support

### Why Multiline YAML?

The single-line JSON format for complex parameters has several drawbacks:
- Difficult to read and understand
- Hard to troubleshoot
- Challenging to modify directly in the CMDB UI

To address these issues, EnvGene now supports multiline YAML-formatted values during import to CMDB using native YAML block styles.

### Configuration

This feature is disabled by default for backward compatibility. To enable it, add the following to your `configuration/config.yml` file:

```yaml
enable_multiline_yaml_support: true
```

When enabled, EnvGene will detect and preserve YAML block styles during CMDB import.

### How to Use

To preserve multiline formatting for a parameter, use YAML block style notation (| or >) in your YAML files:

```yaml
# In Git
global: |
  name: zookeeper
  secrets:
    zooKeeper:
      adminUsername: zadmin
      adminPassword: zadmin
      clientUsername: zclient
      clientPassword: zclient
```

During import to CMDB, this parameter will be preserved as a multiline string, making it much more readable in the CMDB UI:

```bash
# In CMDB
global='
name: zookeeper
secrets:
  zooKeeper:
    adminUsername: zadmin
    adminPassword: zadmin
    clientUsername: zclient
    clientPassword: zclient
';
```

### Technical Implementation

EnvGene detects parameters using YAML block styles and preserves their multiline formatting during the CMDB import process. This is achieved by:

1. Detecting when a parameter value uses a YAML block style (| or >)
2. Preserving the multiline formatting using `LiteralScalarString` from the `ruyaml` library
3. Ensuring the formatted string is properly escaped for CMDB import

## Examples

### Before: Single-line JSON Format

```bash
# In Git (standard YAML format)
backupDaemon:
  backupSchedule: "*/15 * * * *"
  enabled: true
  evictionPolicy: "1h/1d,7d/delete"
  storage: "1Gi"
  storageClass: "custom-csi-cinder-delete"

# In CMDB (transformed to single-line JSON)
backupDaemon='{"backupSchedule": "*/15 * * * *", "enabled": true, "evictionPolicy": "1h/1d,7d/delete", "storage": "1Gi", "storageClass": "custom-csi-cinder-delete"}';
```

### After: Multiline YAML Format

```bash
# In Git (using YAML block style)
backupDaemon: |
  backupSchedule: "*/15 * * * *"
  enabled: true
  evictionPolicy: "1h/1d,7d/delete"
  storage: "1Gi"
  storageClass: "custom-csi-cinder-delete"

# In CMDB (preserved as multiline)
backupDaemon='
backupSchedule: "*/15 * * * *"
enabled: true
evictionPolicy: "1h/1d,7d/delete"
storage: "1Gi"
storageClass: "custom-csi-cinder-delete"
';
```

### JSON Format Example

```bash
# In Git (using YAML block style for JSON content)
client: |
  {
    "enabled": true,
    "resources": {
      "limits": {
        "cpu": "25m",
        "memory": "256Mi"
      },
      "requests": {
        "cpu": "25m",
        "memory": "64Mi"
      }
    }
  }

# In CMDB (preserved as multiline)
client='
{
  "enabled": true,
  "resources": {
    "limits": {
      "cpu": "25m",
      "memory": "256Mi"
    },
    "requests": {
      "cpu": "25m",
      "memory": "64Mi"
    }
  }
}
';
```

Both YAML and JSON formats can be preserved as multiline strings using standard YAML block styles, making them much more readable in the CMDB UI.

## Best Practices

1. **Configuration:**
   - Enable the feature by setting `enable_multiline_yaml_support: true` in your configuration

2. **When to use YAML block styles:**
   - For complex nested structures with multiple levels
   - When parameters need to be readable in the CMDB UI
   - For troubleshooting purposes
   - When the parameter structure changes frequently

3. **When to use the standard format:**
   - For simple parameters (strings, numbers, booleans)
   - For backward compatibility with existing systems
   - When parameter values are generated programmatically
   - When the parameter is consumed by systems that expect a specific format

4. **Block style selection:**
   - Use the literal style (`|`) when you want to preserve newlines exactly as they appear
   - Use the folded style (`>`) when you want newlines to be converted to spaces

