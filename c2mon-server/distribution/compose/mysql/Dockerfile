ARG IMAGE_BASE=mysql:5.7
FROM ${IMAGE_BASE}

ARG C2MON_SERVER_DIR=https://gitlab.cern.ch/c2mon/c2mon/raw/master/c2mon-server

ENV MYSQL_RANDOM_ROOT_PASSWORD yes
ENV MYSQL_DATABASE c2mon
ENV MYSQL_POPULATE_DATABASE true

ADD ${C2MON_SERVER_DIR}/c2mon-server-history/src/main/resources/sql/history-schema-mysql.sql /tmp
ADD ${C2MON_SERVER_DIR}/c2mon-server-configuration/src/main/resources/sql/config-schema-mysql.sql /tmp
ADD ${C2MON_SERVER_DIR}/c2mon-server-cachedbaccess/src/main/resources/sql/cache-schema-mysql.sql /tmp

RUN echo "use c2mon;\n" >> /docker-entrypoint-initdb.d/c2mon.sql

RUN cat /tmp/history-schema-mysql.sql /tmp/config-schema-mysql.sql /tmp/cache-schema-mysql.sql >> /docker-entrypoint-initdb.d/c2mon.sql
