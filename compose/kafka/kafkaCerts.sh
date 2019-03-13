#!/bin/bash
set -e
echo 'Getting Kafka certs from S3...'

echo >> ./kafka/config/server.properties
echo "ssl.truststore.location=/tmp/truststore.jks"  >> ./kafka/config/server.properties
echo "ssl.truststore.password=$SSL_PASSWORD"  >> ./kafka/config/server.properties
echo "ssl.keystore.location=/tmp/keystore.jks"  >> ./kafka/config/server.properties
echo "ssl.keystore.password=$SSL_PASSWORD"  >> ./kafka/config/server.properties
echo "ssl.key.password=$SSL_PASSWORD" >> ./kafka/config/server.properties