#!/bin/bash

set -euo pipefail

beat=$1
es_url=http://elasticsearch:changeme@elk:9200

# Load the sample dashboards for the Beat.
# REF: https://www.elastic.co/guide/en/beats/metricbeat/master/metricbeat-sample-dashboards.html
until ${beat} -e -setup \
        -E setup.kibana.host=elk \
        -E setup.kibana.username=kibana \
        -E setup.kibana.password=changeme
do
    sleep 2
    echo Retrying...
done
