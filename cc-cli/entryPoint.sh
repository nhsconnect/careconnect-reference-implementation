#!/bin/sh

echo EntryPoint

set -xe

: "${FHIR_SERVER?Need FHIR Endpoint}"
: "${POSTGRES_JDBC?Need Postgres JDBC Url}"
: "${POSTGRES_USERNAME?Need Postgres username}"
: "${POSTGRES_PASSWORD?Need Postgres password}"

java cc-cli codesystem -u ${POSTGRES_USERNAME} -p ${POSTGRES_PASSWORD} -j ${POSTGRES_JDBC}

java cc-cli upload-ods -t ${FHIR_SERVER}

java cc-cli upload-examples -t : ${FHIR_SERVER} -a

exec "$@"
