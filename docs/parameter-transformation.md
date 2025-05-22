# Parameter Transformation in EnvGene

This document describes the principles by which parameters are transformed in EnvGene, particularly during the CMDB import process. It covers both standard parameter transformations and the new multiline complex parameter support.

## Table of Contents

1. [Overview](#overview)
2. [Current Parameter Transformation](#current-parameter-transformation)
   - [Simple Parameters](#simple-parameters)
   - [Complex Parameters](#complex-parameters)
3. [New Multiline YAML Parameter Support](#new-multiline-yaml-parameter-support)
   - [Why Multiline YAML?](#why-multiline-yaml)
   - [How to Use](#how-to-use)
   - [Technical Implementation](#technical-implementation)
4. [Examples](#examples)
   - [Before: Single-line JSON Format](#before-single-line-json-format)
   - [After: Multiline YAML Format](#after-multiline-yaml-format)

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

## New Multiline YAML Parameter Support

### Why Multiline YAML?

The single-line JSON format for complex parameters has several drawbacks:
- Difficult to read and understand
- Hard to troubleshoot
- Challenging to modify directly in the CMDB UI

To address these issues, EnvGene now supports multiline YAML-formatted values during import to CMDB.

### How to Use

To mark a parameter as a multiline YAML parameter, prefix its value with `preserve_complex_data:` followed by the YAML content:

```yaml
# In Git
global: 'preserve_complex_data:
  name: zookeeper
  secrets:
    zooKeeper:
      adminUsername: zadmin
      adminPassword: zadmin
      clientUsername: zclient
      clientPassword: zclient
'
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

EnvGene detects parameters marked with the `preserve_complex_data:` prefix and preserves their multiline formatting during the CMDB import process. This is achieved by:

1. Detecting the `preserve_complex_data:` marker at the beginning of parameter values
2. Removing the marker
3. Preserving the multiline formatting using `LiteralScalarString` from the `ruyaml` library
4. Ensuring the formatted string is properly escaped for CMDB import

## Examples

### Before: Single-line JSON Format

```bash
backupDaemon='{"backupSchedule": "*/15 * * * *", "enabled": true, "evictionPolicy": "1h/1d,7d/delete", "storage": "1Gi", "storageClass": "custom-csi-cinder-delete"}';
client='{"enabled": true, "resources": {"limits": {"cpu": "25m", "memory": "256Mi"}, "requests": {"cpu": "25m", "memory": "64Mi"}}}';
```

### After: Multiline YAML Format

```bash
# YAML format
backupDaemon='
backupSchedule: "*/15 * * * *"
enabled: true
evictionPolicy: "1h/1d,7d/delete"
storage: "1Gi"
storageClass: "custom-csi-cinder-delete"
';

# JSON format preserved as multiline
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

Both YAML and JSON formats can be preserved as multiline strings, making them much more readable in the CMDB UI.


