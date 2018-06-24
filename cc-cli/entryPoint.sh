#!/bin/sh

echo EntryPoint

echo Local ls

ls

ehco Root ls

ls /

set -xe

: "${FHIR_SERVER?Need FHIR Endpoint}"
: "${POSTGRES_JDBC?Need Postgres JDBC Url}"
: "${POSTGRES_USERNAME?Need Postgres username}"
: "${POSTGRES_PASSWORD?Need Postgres password}"

java -jar cc-cli.jar codesystem -u ${POSTGRES_USERNAME} -p ${POSTGRES_PASSWORD} -j ${POSTGRES_JDBC}

java -jar cc-cli.jar upload-ods -t ${FHIR_SERVER}

java -jar cc-cli.jar upload-examples -t ${FHIR_SERVER} -a

exec "$@"
