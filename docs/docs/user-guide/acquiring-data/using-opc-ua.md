---
layout:   post
title:    Using OPC UA
summary:  Lean to deploy your own C2MON data acquisition module for OPC UA servers.
---

# What is OPC UA?

OPC UA is powerful and extensive machine-to-machine standard for process control and automation. The OPC family of 
standards has been widely successful in the automation industry, and remains cutting edge in automation environments. 
OPC UA as the newest addition to these standards offers a number of advantages over its predecessors, including:

* platform independence
* high reliability and redundancy
* enhanced performance
* comprehensive security model
* improved data modeling capabilities

C2MON offers an open source data acquisition module to easily monitor data points through OPC UA. 


# Learning by doing: Set up a C2MON instance to monitor data from an open OPC UA server

C2MON can easily be configured to acquire data through OPC UA. Provides a hand's on example by setting up a C2MON
 instance to monitor randomly generated data via a custom OPC UA server. 
 
## Configuring our services
In order to monitor our data points we must set up the services required for operating C2MON. This can be done in
 different ways as described in [Getting Started]({{ site.baseurl }}{% link docs/getting-started.md %}). In this
  example we will use Docker images.

We will setup out C2MON instance with the following services:
* C2MON server
* A MySQL database
* ActiveMQ as a message broker
* Elasticsearch, a search and analytics engine
* The [C2MON Web UI](https://github.com/c2mon/c2mon-web-ui/blob/master/README.md), a graphical web interface for C2MON
* An [OPC UA server](https://github.com/Azure-Samples/iot-edge-opc-plc) as a data source
* The [C2MON OPC UA DAQ](https://github.com/c2mon/c2mon-daq-opcua) to monitor our OPC UA Server
* [Grafana](https://grafana.com/) for visualization
* [Prometheus](https://prometheus.io/) for monitoring

Not all of these services are strictly required: in this example we employ a selection of services from the rich infrastructure of C2MON client and data acquisition applications. 

<!----LINK TO PUBLIC RESOURCE REPO ---->

The C2MON server requires ActiveMQ and MySQL to run. Additionally, we need the edge server to run in the background as a data source. 
Let's start these services for now:

```bash
docker-compose up -d mq db elasticsearch c2mon edge
```

## Configuring C2MON via the C2MON Client Configuration Shell

In order to acquire data from the C2MON server, we need to configure DataTags.
We will use the interactive [C2MON Client Configuration Shell]({{ site.baseurl }}{% link docs/user-guide/dynamic-configuration-shell.md %}) to configure our DataTags for convenience.
The shell allows us to easily create and configure DataTags on-the-fly. 
Use cases requiring more fine-grained configuration can be addressed through the Java-based Client API for subscribing to tags which is described in detail in [Client API]({{ site.baseurl }}{% link docs/user-guide/configuration-api.md %}).

We are using an industrial IoT sample [OPC UA server](https://github.com/Azure-Samples/iot-edge-opc-plc) by Microsoft as a data source.
The server offers different nodes generating random data and anomalies which can be explored using any OPC UA browser.

We use a simple configuration for the shell of mapping all addresses with the binary UA scheme to the same OPC UA Process and Equipment:

```yaml
c2mon:
    client:
        dynconfig:
            mappings:
            -   processName: OPC_UA_DAQ
                processID: 10000
                processDescription: OPC UA Process
                equipmentName: MS_IOT_SERVER
                equipmentDescription: OPC UA Equipment
                uriPattern: ^opc.tcp.*
```

As you can see, we specify the same Process name which we have defined in the arguments to our docker-compose file.    

Let's start the shell and run the following commands to subscribe to one data point on the server:

```bash
java -jar c2mon-client-config-shell-1.9.11-SNAPSHOT.jar --spring.config.additional-location=file://<PROPERTIES FILE LOCATION>
get-tags opc.tcp://edge:50000?itemName=RandomSignedInt32?tagName=RandomSignedInt&dataType=java.lang.Integer&setNamespace=2
```

As we can see from the output of the shell, the DataTag configuration is processed on the C2MON server. 
The Process `OPC_UA_DAQ` and Equipment `MS_IOT_SERVER` are matched to our URI by the uriPattern expression, and are created on the server since they do not yet exist.
Let's configure another tag in the shell:

```bash
get-tags opc.tcp://edge:50000?itemName=SpikeData?tagName=SpikeData&dataType=java.lang.Double&setNamespace=2;opc.tcp://edge:50000?itemName=AlternatingBoolean?tagName=AlternatingBoolean&dataType=java.lang.Boolean&setNamespace=2
```

We should now have one Process referring to one Equipment and to DataTags `RandomSignedInt`, `SpikeData` and `AlternatingBoolean`.

## Data collection and visualization

In order to collect data from these DataTags, the `OPC_UA_DAQ` Process needs to be running as well. 
To explore the data we also need to run one of our client applications.

Let's leave the C2MON Client Configuration Shell and start the Process as well as the C2MON Web UI:

```bash
'docker-compose up -d daq web-ui
```

We can now have a look around our configuration and our data in the [Web UI](http://localhost:8080/c2mon-web-ui/). 
For example, you can take a look the latest values the DataTags we just defined by navigating through the "DAQ Process Viewer" to our Process, Equipment, and DataTags, and to "View Trend:"

![trend viewer]({{ site.baseurl }}{% link assets/img/user-guide/acquiring-data/trend-viewer-example.png %})

## Metric collection and monitoring

We can also examine our data in Grafana, where we can additionally visualize and analyze a range of metrics that are exposed through the OPC UA DAQ.
These metrics can help us identify the source of errors, bottlenecks, or performance issues, and to generate insight into the operation of our Processes. 
We use the monitoring tool Prometheus to scrape the C2MON OPC UA DAQ for interesting metrics. 

Let's start Grafana and Prometheus:

```bash
docker-compose up -d grafana prometheus
```

Grafana is now reachable under [localhost:3000](localhost:3000) with Elasticsearch and Prometheus as default data sources.
Explore the included dashboard for relevant metrics and sights.

This step by step tutorial shows how to easily configure C2MON to read from an OPC UA server as a data source.     