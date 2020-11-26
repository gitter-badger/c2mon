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
* An [OPC UA server](https://github.com/Azure-Samples/iot-edge-opc-plc) as a data source
* The [C2MON OPC UA DAQ](https://github.com/c2mon/c2mon-daq-opcua) to monitor our OPC UA Server
* [Grafana](https://grafana.com/) for visualization
* [Prometheus](https://prometheus.io/) for monitoring

For the sake of simplicity and clarity we are not taking advantage of C2MON's full capabilities of redundancy or integration with Elasticsearch. 
Instead we employ a selection of services from the rich infrastructure of C2MON client and data acquisition applications. 

All services can conveniently be defined in a [Docker Compose](https://docs.docker.com/compose/) file as follows:

```yaml
       version: "3.8"
       services:
       
         c2mon:
           image: cern/c2mon:1.9.3-SNAPSHOT
           ports:
             - "9001:9001"
           environment:
             - C2MON_SERVER_ELASTICSEARCH_ENABLED=false
             - C2MON_SERVER_ELASTICSEARCH_EMBEDDED=false
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

         mq:
           image: gitlab-registry.cern.ch/c2mon/c2mon/mq:activemq-5.15.2-c2mon-1.9.11-SNAPSHOT
           ports:
             - "61616:61616"
             - "61614:61614"
             - "1883:1883"
             - "8086:8086"
             - "8161:8161"

         db:
           image: gitlab-registry.cern.ch/c2mon/c2mon/mysql:activemq-5.15.2-c2mon-1.9.11-SNAPSHOT
           ports:
             - "3306:3306"
           environment:
             - MYSQL_ALLOW_EMPTY_PASSWORD="yes"
       
         prometheus:
           image: prom/prometheus:latest
           ports:
             - "9090:9090"
           volumes:
             - ~/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
           command: --config.file=/etc/prometheus/prometheus.yml
           
         grafana:
           image: gitlab-registry.cern.ch/c2mon/c2mon/grafana:grafana-6.1.2-c2mon-1.9.11-SNAPSHOT
           ports:
             - "3000:3000"
           environment:
             - GF_AUTH_ANONYMOUS_ENABLED=true
             - GF_AUTH_ANONYMOUS_ORG_NAME=Main Org.
             - GF_AUTH_ANONYMOUS_ORG_ROLE=Editor
       
         edge-server:
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
             - "_JAVA_OPTIONS=-Dc2mon.daq.name=P_OPCUA -Dc2mon.daq.jms.mode=single -Dc2mon.daq.jms.url=tcp://mq:61616 -Dc2mon.client.jms.url=tcp://mq:61616 -Dc2mon.daq.opcua.trustAllServers=true -Dc2mon.daq.opcua.certifierPriority.noSecurity=3 -Dc2mon.daq.opcua.portSubstitutionMode=NONE -Dc2mon.daq.opcua.hostSubstitutionMode=SUBSTITUTE_LOCAL"
             - LOG_PATH=/c2mon-daq-opcua-1.9.11-SNAPSHOT/tmp
             - SPRING_JMX_ENABLED=true
             - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*,jolokia,prometheus
             - MANAGEMENT_SERVER_PORT=8912
             - C2MON_DAQ_JMS_MODE=single

```

The C2MON server requires ActiveMQ and MySQL to run. 
Let's start these services for now:

```bash
docker-compose up mq db c2mon
```

The output "C2MON server is now initialised" indicates a successful startup.

## Configuring C2MON via the Dynamic Configuration module

In order to acquire data from the C2MON server, we need to configure DataTags.
We will use the interactive [C2MON Client Configuration Shell]({{ site.baseurl }}{% link docs/user-guide/dynamic-configuration-shell.md %}) to configure our DataTags for convenience.
The Shell allows us to easily create and configure DataTags on-the-fly.
Use cases requiring more fine-grained configuration can be addressed through the Java-based Client API for subscribing to tags which is described in detail in [Client API]({{ site.baseurl }}{% link docs/user-guide/configuration-api.md %}).

We are using an industrial IoT sample [OPC UA server](https://github.com/Azure-Samples/iot-edge-opc-plc) by Mirosoft as a data source.
The server offers different nodes generating random data and anomalies which can be explored using any OPC UA browser.


Let's start the shell and run the following commands:

```bash
opc.tcp://power-supply-sim:4841?itemName=simSY4527.Board00.Chan000.IMon?tagName=IMon-EQ00&dataType=java.lang.Float&setNamespace=2;opc.tcp://power-supply-sim:4841?itemName=simSY4527.Board00.Chan000.VMon?tagName=VMon-EQ00&dataType=java.lang.Float&setNamespace=2  

```


## Metrics visualization and monitoring


Prometheus  is a monitoring tools which we will use to scrape the C2MON OPC UA DAQ for interesting metrics. 
These metrics can help us identify the source of errors, bottlenecks, or performance issues. 
We will later proceed to visualize the metrics in the analytics and monitoring platform Grafana to generate insight into the state of our data acquisition processes.
Prometheus is configured via a configuration file which specifies monitoring behavior and targets. 
More information regarding available configuration options for Prometheus can be found in the official [documentation](https://prometheus.io/docs/prometheus/latest/configuration/configuration/).

The configuration file for this example should be saved in the location `~/prometheus/prometheus.yml` and contain the OPC UA DAQ as a target:

```yaml
scrape_configs:
  - job_name: 'spring-actuator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['daq-opcua:8912']
```