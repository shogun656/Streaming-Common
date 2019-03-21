#!/bin/bash

CERT_PASSWORD=kafkaPass

# Generate Keystore
keytool -keystore keystore.jks -alias localhost -validity 365 -genkey -keyalg RSA -storepass $CERT_PASSWORD \
          -keypass $CERT_PASSWORD -noprompt -dname "CN=kafka.docker.ssl, OU=None, O=None, L=Detroit, S=Michigan, C=US"

# Generate CA and attach it to client's Truststore
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -passout pass:$CERT_PASSWORD \
           -subj "/C=US/S=Michigan/L=Detroit/O=None/OU=None/CN=kafka.docker.ssl"
keytool -keystore truststore.jks -alias CARoot -import -file ca-cert -storepass $CERT_PASSWORD -noprompt
#rm ca-cert
#keytool -keystore truststore.jks -export -alias CARoot -rfc -file ca-cert -storepass $CERT_PASSWORD -noprompt

# Sign the certificate and import them to the keystore
keytool -keystore keystore.jks -alias localhost -certreq -file cert-file -storepass $CERT_PASSWORD -noprompt
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:$CERT_PASSWORD
keytool -keystore keystore.jks -alias CARoot -import -file ca-cert -storepass $CERT_PASSWORD -noprompt
keytool -keystore keystore.jks -alias localhost -import -file cert-signed -storepass $CERT_PASSWORD -noprompt

# Extract the keys into pem files
keytool -exportcert -alias localhost -keystore keystore.jks -storepass $CERT_PASSWORD -rfc -file ck_cert
keytool -v -importkeystore -srckeystore keystore.jks -storepass $CERT_PASSWORD -srcstorepass $CERT_PASSWORD \
            -keypass $CERT_PASSWORD -srcalias localhost -destkeystore cert_and_key.p12 -deststoretype PKCS12
openssl pkcs12 -in cert_and_key.p12 -nocerts -nodes -passin pass:$CERT_PASSWORD >> ck_private_key
sed -i -e 1,4d ck_private_key
keytool -exportcert -alias CARoot -keystore keystore.jks -storepass $CERT_PASSWORD -rfc -file ck_ca

# zip up the files
rm ck_private_key-e
rm kafkaCerts.zip
zip kafkaCerts.zip ck* keystore.jks truststore.jks

# Clean up files we don't need
rm -r ca* cert* ck* keystore.jks truststore.jks

