---
layout:   post
title:    C2MON Client Configuration Shell
summary:  Learn how to configure C2MON using the C2MON Client Configuration Shell through JMX or HTTP
---

Beyond the Java [Configuration API]({{ site.baseurl }}{% link docs/client-api
/configuration-api.md %}),
 
Reconfiguration to a C2MON server instance can requested at runtime using the dedicated C2MON  Client Configuration 
Shell. The Shell offers methods to *fetch*, *create* or *remove* `DataTags`, and dynamically creates `Processes` or
 `Equipments` if required by the `DataTag` configuration. 
 
# Reconfiguration URI

Reconfiguration requests are sent using URIs which reference and describe `DataTags`. These URIs follow the following
 form:

```bash
scheme://host:port/path?query=option
```

The first part of the URI `scheme://host:port/path` is equivalent to the Tag's "hardware address". The hardware
 address is described in more detail in [Creating a new DAQ module from scratch]({{ site.baseurl }}{% link  docs/user-guide/daq-api/daq-module-developer-guide.md %}).  


Queries can be used to add configuration parameters to the Tag. For example, the name of the tag can be specified using
 `?tagName=TAG_EXAMPLE`. Some configuration parameters depend on the DAQ module corresponding to the DataTag, while
  others are universal:
 

|        	    | Parameter         	| Value Description     |
|--------------	|---------------------	| --------------------	|
| **General**	| tagName           	| String         	    |
|          	    | dataType          	| Java class name       |
|          	    | description       	| String          	    |
| **OPC UA** 	| itemName        	    | String         	    |
|          	    | commandType      	    | METHOD, CLASSIC  	    |
|          	    | setNamespace    	    | Integer         	    |
|          	    | setAddressType        | STRING, NUMERIC, GUID |
| **DIP**     	| publicationName       | String         	    |
|          	    | fieldName      	    | String         	    |
|          	    | fieldIndex    	    | Integer         	    |
| **REST**     	| url                   | String         	    |
|             	| mode            	    | GET, POST        	    |
|              	| getFrequency    	    | Integer         	    |
|              	| postFrequency    	    | Integer         	    |

In addition to the here listed query keys, it is possible to pass any query corresponding one of the following methods:
* the [DataTag.CreateBuilder](https://gitlab.cern.ch/c2mon/c2mon/-/blob/master/c2mon-shared/c2mon-shared-client/src/main/java/cern/c2mon/shared/client/configuration/api/tag/DataTag.java), 
* the [DataTagAddress](https://gitlab.cern.ch/c2mon/c2mon/-/blob/master/c2mon-shared/c2mon-shared-common/src/main/java/cern/c2mon/shared/common/datatag/DataTagAddress.java), 
* and the [protocol-specific HardwareAddress class](https://gitlab.cern.ch/c2mon/c2mon/-/blob/master/c2mon-shared/c2mon-shared-common/src/main/java/cern/c2mon/shared/common/datatag/DataTagAddress.java).

For example, to set namespace of a OPC UA tag, one may append `setNamespace=namespace`.

# Mappings

We must specify the Process and Equipment that a new DataTag should be created for within the C2MON Client Configuration 
Shell. This is done my defining appropriate *Mappings* within the C2MON Client properties. Regular expressions allow the 
fine-grained which Processes and Equipments an URIs is mapped to:

The following example associates any URI starting with the scheme `opc.tcp` with an OPC UA DAQ Process "P_DYNOPCUA" 
and the Equipment "E_DYNOPCUA", and any URI starting with `dip` with the Process "P_DYNDIP" and equipment "E_DYNDIP".
If DataTag is configured for a Process or Equipment which does not currently exist, it is created on-the-fly. 

```yaml
c2mon:
    client:
        dynconfig:
            mappings:

            -   processName: P_DYNDIP
                processID: 1001
                processDescription: DIP sample Process
                equipmentName: E_DYNDIP
                equipmentDescription: DIP sample Equipment
                uriPattern: ^dip.*

            -   processName: P_DYNOPCUA
                processID: 10002
                processDescription: OPC UA sample Process
                equipmentName: E_OPCUA
                equipmentDescription: OPC UA sample Equipment
                uriPattern: ^opc.tcp.*
```

# Starting the Agent

Command to start the Jar

# Interacting with the Agent

# JMX

# HTTP

Jolokia
intro

Look around: endpoints and methods

calling a method


1 Creating a datatag

2 deleting a datatag