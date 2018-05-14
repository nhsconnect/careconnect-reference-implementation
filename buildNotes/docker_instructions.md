---
title: Docker Image
keywords: deployment
tags: [deployment]
sidebar: overview_sidebar
permalink: ccri_docker.html
summary: "How to run a local copy of Care Connect Reference Implementation"
---


## 1. Install Docker + Kitematic ##
Docker install of Community edition
https://docs.docker.com/engine/installation/
Create a Docker account and login to the account to use Kitematic

Kitematic install
https://kitematic.com/

## 2. Run Docker ##

Using command line pull the images:

```
docker pull thorlogic/ccri
```
```
docker pull mysql
```

Then start mysql image [Note: this exposes 43306 on the local, so mysql workbench can connect on this port]

```
docker run -detach --name=ccrisql --env="MYSQL_ROOT_PASSWORD=mypassword" --env="MYSQL_DATABASE=careconnect" --env="MYSQL_USER=fhirjpa" --env="MYSQL_PASSWORD=fhirjpa" -p 43306:3306 mysql --character-set-server=utf8 --collation-server=utf8_bin --innodb_lock_wait_timeout=300 --transaction-isolation=READ-UNCOMMITTED
```

Within Kitematic the image should be visible. Screen shot of settings below:

<p style="text-align:center;"><img src="images/deploy/MYSQLConfig.png" alt="Docker Config" title="Docker Config" style="width:75%"></p>
<br><br>

Now start the Care Connect Reference Implementation

```
docker run --detach --name=ccri -p 8080:8080 -e datasource.username=fhirjpa -e datasource.password=fhirjpa -e datasource.host=//ccrisql -e datasource.driver=com.mysql.jdbc.Driver -e datasource.path=3306/careconnect?autoReconnect=true -e datasource.vendor=mysql -e datasource.showSql=true -e datasource.showDdl=true -e datasource.cleardown.cron="0 19 21 * * *" -e datasource.dialect=org.hibernate.dialect.MySQL57Dialect --link ccrisql thorlogic/ccri
```

Test install with curl command
```
curl -X GET -v -H 'Accept: application/xml+fhir' 'http://localhost:8080/careconnect-ri/STU3/Patient/1'
```

<p style="text-align:center;"><img src="images/deploy/CCRIConfig.png" alt="Docker Config" title="Docker Config" style="width:75%"></p>
<br><br>

Now start the Care Connect Reference Gateway

```
sudo docker run --detach --name=ccrigateway -p 80:80 -e datasource.ui.serverBase=http://194.189.27.193/careconnect/STU3 --link ccri thorlogic/ccrigateway 
```

Care Connect Reference Implementation should be availabe at http://127.0.0.1/careconnect/






## 3. Putting Docker Image to DockerHub ##

For Administraton only

In the ccri-fhirserver project in the ccri github project

```
docker build -t ccri .
```

```
docker login
```

```
docker tag ccri thorlogic/ccri
```

```
docker push thorlogic/ccri
```

To view tomcat logs, open a shell to ccri container

```
docker exec -it ccri /bin/bash
```

Navigate into logs folder and run tail command on the log file. This is usually catalina.out, if not use the file for the current day.

```
cd logs
tail -f catalina.out
```

To remove old log files 

sudo find /var/lib/docker/volumes/ccriserver_tomcat-log-volume/_data -type f -mtime +7 -name '*.txt' -execdir rm -- '{}' \; 
sudo find /var/lib/docker/volumes/ccriserver_gatewayssl-log-volume/_data -type f -mtime +7 -name '*.txt' -execdir rm -- '{}' \;
sudo find /var/lib/docker/volumes/ccriserver_gateway-log-volume/_data -type f -mtime +7 -name '*.txt' -execdir rm -- '{}' \;

sudo find /var/lib/docker/volumes/ccriserver_tomcat-log-volume/_data -type f -mtime +7 -name '*.log' -execdir rm -- '{}' \; 
sudo find /var/lib/docker/volumes/ccriserver_gatewayssl-log-volume/_data -type f -mtime +7 -name '*.log' -execdir rm -- '{}' \;
sudo find /var/lib/docker/volumes/ccriserver_gateway-log-volume/_data -type f -mtime +7 -name '*.log' -execdir rm -- '{}' \;
