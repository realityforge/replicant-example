#!/bin/bash

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
if [ -f "${CURRENT_DIR}/local.sh" ]; then
  echo "Running local customization script '${CURRENT_DIR}/local.sh'."
  . "${CURRENT_DIR}/local.sh"
fi

STOP_DOMAIN=false
CREATED_DOMAIN=false

R=`(asadmin list-domains | grep -q 'tyrell ') && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin create-domain --user admin --nopassword tyrell
  CREATED_DOMAIN=true
fi

R=`(asadmin list-domains | grep -q 'tyrell running') && echo yes`
if [ "$R" != 'yes' ]; then
  STOP_DOMAIN=true
  asadmin start-domain tyrell
  if [ "$CREATED_DOMAIN" == 'true' ]; then
    asadmin set configs.config.server-config.java-config.debug-enabled=false
    asadmin set configs.config.server-config.java-config.debug-options=-agentlib:jdwp=transport=dt_socket,address=43228,server=n,suspend=y
    asadmin delete-jvm-options -XX\\:MaxPermSize=192m
    asadmin delete-jvm-options -Xmx512m
    asadmin create-jvm-options -XX\\:MaxPermSize=400m
    asadmin create-jvm-options -Xmx1500m
    asadmin create-jvm-options -Dcom.sun.enterprise.tools.admingui.NO_NETWORK=true
    asadmin restart-domain tyrell
  fi
fi

R=`(asadmin list-libraries | grep -q postgresql-9.1-901.jdbc4.jar) && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin add-library ~/.m2/repository/postgresql/postgresql/9.1-901.jdbc4/postgresql-9.1-901.jdbc4.jar
  asadmin restart-domain tyrell
fi

asadmin delete-jdbc-resource tyrell/jdbc/Tyrell
asadmin delete-jdbc-connection-pool tyrell/jdbc/TyrellConnectionPool

asadmin create-jdbc-connection-pool\
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource\
  --restype javax.sql.DataSource\
  --isconnectvalidatereq=true\
  --validationmethod auto-commit\
  --ping true\
  --description "Tyrell Connection Pool"\
  --property "ServerName=127.0.0.1:User=${USER}:Password=letmein:PortNumber=5432:DatabaseName=${USER}_TYRELL_DEV" tyrell/jdbc/TyrellConnectionPool
asadmin create-jdbc-resource --connectionpoolid tyrell/jdbc/TyrellConnectionPool tyrell/jdbc/Tyrell

asadmin set domain.resources.jdbc-connection-pool.TyrellPool.property.JDBC30DataSource=true

asadmin set-log-levels javax.enterprise.resource.resourceadapter.com.sun.gjc.spi=WARNING

if [ "$STOP_DOMAIN" == 'true' ]; then
  asadmin stop-domain tyrell
fi
