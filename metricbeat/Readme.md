## Runnning Metric Beats

Need to run on the Host rather than inside a Docker container since it requires access to the /var/run/docker.sock file on the host machine which is only available to root.

This is basically precluded unless the metricbeats service is run as 'root' (not a great idea) or as a member of a user group on the container which has the same GID as the 'docker' group on the host.  This is another less than ideal solution.


### Installation

1. Install metricbeats 
1. Download client key for Elastic Search.  We are currently using the sebp/elk Docker image to provide LogStash, Elasticsearch & Kibana docker containers (see https://hub.docker.com/r/sebp/elk/).  The client certificate used to authenticate clients can be downloaded from their source code (see https://github.com/spujadas/elk-docker) and our MetricBeat config files expect the client certificate to be copied to /etc/pki/tls/certs/logstash-beats.crt

### Command Line

1. Initialisation
	```bash
	./metricbeat -E output.elasticsearch.username=elasticsearch -E output.elasticsearch.password=changeme -c metricbeat.yml
	```

Note:  The Metricbeat logs are sent directly to Elasticsearch & don't go via LogStash.


### Configuration file: metricbeat.yml
```yaml
metricbeat.modules:
- module: docker
  metricsets: ["container", "cpu", "diskio", "healthcheck", "info", "memory", "network"]
  hosts: ["unix:///var/run/docker.sock"]
  enabled: true
  period: 10s

dashboard:
  enabled: true

output:
  elasticsearch:
    hosts: ['localhost:9200']
    username: elasticsearch
    password: changeme
```