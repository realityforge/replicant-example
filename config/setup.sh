#!/bin/bash

R=`(asadmin list-domains | grep -q 'tyrell ') && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin create-domain tyrell
fi

R=`(asadmin list-domains | grep -q 'tyrell running') && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin start-domain tyrell
fi

R=`(asadmin list-libraries | grep -q postgresql-9.1-901.jdbc4.jar) && echo yes`
if [ "$R" != 'yes' ]; then
  asadmin add-library ~/.m2/repository/postgresql/postgresql/9.1-901.jdbc4/postgresql-9.1-901.jdbc4.jar
  asadmin restart-domain tyrell
fi

asadmin delete-jdbc-resource jdbc/Tyrell
asadmin delete-jdbc-connection-pool TyrellPool

asadmin create-jdbc-connection-pool\
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource\
  --restype javax.sql.DataSource\
  --isconnectvalidatereq=true\
  --validationmethod auto-commit\
  --ping true\
  --description "Tyrell Connection Pool"\
  --property "ServerName=127.0.0.1:User=${USER}:Password=letmein:PortNumber=5432:DatabaseName=${USER}_TYRELL_DEV" TyrellPool
asadmin create-jdbc-resource --connectionpoolid TyrellPool jdbc/Tyrell

asadmin set domain.resources.jdbc-connection-pool.TyrellPool.property.JDBC30DataSource=true
