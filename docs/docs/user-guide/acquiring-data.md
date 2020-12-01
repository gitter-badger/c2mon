---
layout:   post
title:    Acquiring Data
summary:  Learn how to setup C2MON to acquire data using OPC UA
---

We have gotten to know the configuration of Tags, Equipments and Processes in the [overview]({{ site.baseurl }}{% link docs/overview/index.md %}).
Let's take a look at how to go about applying these configurations to acquire data from running processes.

C2MON as a modular relies on Data Acquisition Processes, or DAQs, to collect data from sources.
These are independent Java processes which fetch their respective configuration from the server layer, and collect data according to their configured sources.
DAQs can be implemented in vastly different ways to meet the requirements of the respective data sources or of user needs - you can easily implement your own solution by following the [DAQ module developer guide]({{ site.baseurl }}{% link docs/user-guide/daq-api/daq-module-testing-guide.md %}). 

In this section we will walk-through the setup of a C2MON instance with sample configurations and and data sources.
This walk-through provides a hand's on example by setting up the C2MON environment and goes into detail on how to monitor randomly generated data via a custom OPC UA server using the open source [OPC UA DAQ]({{ site.baseurl }}{% link docs/user-guide/acquiring-data/using-opc-ua.md %}).
You should be familar with Docker and with basic C2MON terminology before starting this walk-through.


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

 
## Configuring our services
In order to monitor our data points we must set up the services required for operating C2MON. 
This can be done in different ways as described in [Getting Started]({{ site.baseurl }}{% link docs/getting-started.md %}). 
In this example we will use Docker images.

Let's set up our C2MON instance with the following services:
* Our C2MON server
* A [MySQL](https://www.mysql.com/de/) database for persistence
* [ActiveMQ](http://activemq.apache.org/) as a message broker
* [Elasticsearch](https://www.elastic.co/), a search and analytics engine
* The [C2MON Web UI](https://github.com/c2mon/c2mon-web-ui/blob/master/README.md), a graphical web interface for C2MON
* An [OPC UA server](https://github.com/Azure-Samples/iot-edge-opc-plc) as a data source
* The [C2MON OPC UA DAQ](https://github.com/c2mon/c2mon-daq-opcua) to monitor our OPC UA Server
* [Grafana](https://grafana.com/) for visualization
* [Prometheus](https://prometheus.io/) for monitoring

<!----LINK TO PUBLIC RESOURCE REPO ---->
We will use the [docker-compose](https://docs.docker.com/compose/) tool to define and run these services in containers.
You can find the docker-compose file we will use along with all configuration options and dependencies in the repository [C2MON Compose Sample](https://gitlab.cern.ch/estockin/c2mon-compose-sample).

Let's take a look at our docker-compose file:

```yaml
version: "3.8"
services:

  c2mon:
    image: cern/c2mon:1.9.3-SNAPSHOT
    ports:
      - "9001:9001"
    environment:
      - C2MON_SERVER_ELASTICSEARCH_ENABLED=true
      - C2MON_SERVER_ELASTICSEARCH_HOST=elasticsearch
      - C2MON_SERVER_ELASTICSEARCH_PORT=9200
      - C2MON_SERVER_ELASTICSEARCH_EMBEDDED=false
      - C2MON_SERVER_ELASTICSEARCH_CLIENT=rest
      - C2MON_SERVER_ELASTICSEARCH_SCHEME=http
      - C2MON_SERVER_JMS_EMBEDDED=false
      - C2MON_SERVER_JMS_URL=tcp://mq:61616
      - C2MON_SERVER_CACHEDBACCESS_JDBC_VALIDATION-QUERY=SELECT 1
      - C2MON_SERVER_JDBC_DRIVER-CLASS-NAME=com.mysql.jdbc.Driver
      - C2MON_SERVER_JDBC_URL=jdbc:mysql://db/tim
      - C2MON_SERVER_JDBC_USERNAME=root
      - C2MON_SERVER_CACHEDBACCESS_JDBC_JDBC-URL=jdbc:mysql://db/tim
      - C2MON_SERVER_HISTORY_JDBC_JDBC-URL=jdbc:mysql://db/tim
      - C2MON_SERVER_CONFIGURATION_JDBC_JDBC-URL=jdbc:mysql://db/tim
      - C2MON_SERVER_TESTMODE=false
    restart: on-failure
 
  elasticsearch:
    image: gitlab-registry.cern.ch/c2mon/c2mon/es:elasticsearch-6.4.3-c2mon-1.9.11-SNAPSHOT
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - cluster.name=c2mon
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - TAKE_FILE_OWNERSHIP="1"
    ulimits:
      memlock:
        soft: -1
        hard: -1

  mq:
    image: gitlab-registry.cern.ch/c2mon/c2mon/mq:activemq-5.15.2-c2mon-1.9.11-SNAPSHOT
    ports:
      - "61616:61616"
      - "61614:61614"
      - "1883:1883"
      - "8086:8086"
      - "8161:8161"

  db:
    image: gitlab-registry.cern.ch/c2mon/c2mon/mysql:mysql-5.7.15-c2mon-1.9.11-SNAPSHOT
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ALLOW_EMPTY_PASSWORD="yes"

  web-ui:
    image: cern/c2mon-web-ui:0.1.14-SNAPSHOT
    ports:
      - "3306"
      - target: 8080
        published: 8080
        protocol: tcp
        mode: host
    environment:
      - C2MON_CLIENT_JMS_URL=tcp://mq:61616 
      - C2MON_CLIENT_HISTORY_JDBC_URL=jdbc:mysql://db/tim
      - C2MON_CLIENT_HISTORY_JDBC_USERNAME=root
      - C2MON_CLIENT_HISTORY_JDBC_VALIDATION-QUERY=SELECT 1

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    command: --config.file=/etc/prometheus/prometheus.yml
    
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_NAME=Main Org.
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Editor
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
      - GF_DASHBOARDS_DEFAULT_HOME_DASHBOARD_PATH=/etc/grafana/provisioning/dashboards/dashboards.json
    depends_on: 
      - prometheus
    volumes:
      - ./grafana/provisioning/:/etc/grafana/provisioning

  edge:
    image: mcr.microsoft.com/iotedge/opc-plc
    ports:
      - "50000:50000"
    command: --unsecuretransport

  daq:
    image: gitlab-registry.cern.ch/c2mon/c2mon-daq-opcua
    ports:
      - "8912:8912"
      - "8913:8913"
    environment:
      - "_JAVA_OPTIONS=-Dc2mon.daq.name=OPC_UA_DAQ -Dc2mon.daq.jms.mode=single -Dc2mon.daq.jms.url=tcp://mq:61616 -Dc2mon.client.jms.url=tcp://mq:61616 -Dc2mon.daq.opcua.trustAllServers=true -Dc2mon.daq.opcua.certifierPriority.noSecurity=3 -Dc2mon.daq.opcua.portSubstitutionMode=NONE -Dc2mon.daq.opcua.hostSubstitutionMode=SUBSTITUTE_LOCAL"
      - LOG_PATH=/c2mon-daq-opcua-1.9.11-SNAPSHOT/tmp
      - SPRING_JMX_ENABLED=true
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*,jolokia,prometheus
      - MANAGEMENT_SERVER_PORT=8912
      - C2MON_DAQ_JMS_MODE=single
```

You may note that we use images for Elasticsearch, ActiveMQ and MySQL that are published under a C2MON domain: these images are preconfigured to integrate smoothly with our C2MON server.
Not all services are strictly required: in this example we employ a selection of services from the rich infrastructure of C2MON client and data acquisition applications.

Let's start our C2MON server for now. This service requires ActiveMQ, MySQL and Elasticsearch (if enabled) to start smoothly. Additionally, we need the edge server to run in the background as a data source. 
Let's start these services for now:

```bash
docker-compose up -d mq db elasticsearch c2mon edge
```

## Configuring C2MON via the C2MON Client Configuration Shell

In order to acquire data from the C2MON server, we need to configure DataTags.
We will use the interactive [C2MON Client Configuration Shell]({{ site.baseurl }}{% link docs/user-guide/client-api/client-configuration-shell.md %}) to configure our DataTags for convenience.
The shell allows us to easily create and configure DataTags on-the-fly. 
Use cases requiring more fine-grained configuration can be addressed through the Java-based Client API for subscribing to tags which is described in detail in [Client API]({{ site.baseurl }}{% link docs/user-guide/client-api/configuration-api.md %}).

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
This allows the C2MON server to associate the Tags with the OPC UA DAQ Process that we will start up later.  

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
Let's exit the Shell by typing `exit`. 

## Data collection and visualization

In order to collect data from these DataTags, the `OPC_UA_DAQ` Process needs to be running as well. 
To explore the data we also need to run one of our client applications.

Let's leave the C2MON Client Configuration Shell and start the Process as well as the C2MON Web UI:

```bash
docker-compose up -d daq web-ui
```

We can now have a look around our configuration and our data in the [Web UI](http://localhost:8080/c2mon-web-ui/). 
For example, you can take a look the latest values the DataTags we just defined by navigating through the "DAQ Process Viewer" to our Process, Equipment, and DataTags, and to "View Trend:"

![trend viewer]({{ site.baseurl }}{% link assets/img/user-guide/acquiring-data/trend-viewer-example.png %})

## Metric collection and monitoring

We can also examine our data in Grafana, where we can additionally visualize and analyze a range of metrics that are exposed through the OPC UA DAQ.
These metrics can help us identify the source of errors, bottlenecks, or performance issues, and to generate insight into the operation of our Processes. 
We use the monitoring tool Prometheus to scrape the C2MON OPC UA DAQ for interesting metrics. 

Let's start Grafana and Prometheus:

````bash
docker-compose up -d grafana prometheus
````

Prometheus is preconfigured by `./prometheus/prometheus.yml` to scrape the OPC UA DAQ Process. 
Grafana is provisioned with Elasticsearch and Prometheus as data sources, and comes with a default sample dashboard. 
Explore the included dashboard for relevant metrics and insighs collected through Prometheus and Elasticsearch to get some insight into the inner workings of the DAQ.

## Wrapping up

This step by step tutorial shows how to easily configure C2MON to read from an OPC UA server as a data source. 