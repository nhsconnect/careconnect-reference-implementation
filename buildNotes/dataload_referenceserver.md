---
title: CCRI Dataload
keywords: deployment
tags: [deployment]
sidebar: overview_sidebar
permalink: ccri_dataload.html
summary: "Populating data in the reference implementation"
---


## 1. Code Import ##

Export using (note use of the folder specified by secure-file priv setting)

```
select * into outfile '/mysql_exp/tempDescription.txt' from careconnect.tempDescription
```

Copy files from local machine to development box:

```
scp tempConcept.txt dev01@purple.testlab.nhs.uk:~/
scp tempDescription.txt dev01@purple.testlab.nhs.uk:~/
scp tempSimple.txt dev01@purple.testlab.nhs.uk:~/
scp tempRelationship.txt dev01@purple.testlab.nhs.uk:~/
```

Move from server to docker container. The folder is defined in the @@secure-privs variable in mysql.

```
sudo docker cp tempDescription.txt ccrisql:/var/lib/mysql-files/tempDescription.txt
sudo docker cp tempConcept.txt ccrisql:/var/lib/mysql-files/tempConcept.txt
sudo docker cp tempRelationship.txt ccrisql:/var/lib/mysql-files/tempRelationship.txt
sudo docker cp tempSimple.txt ccrisql:/var/lib/mysql-files/tempSimple.txt
```

Log into the ccrisql (docker exec -it ccrisql /bin/bash).
Import using this to log into mysql (run this from \var\lib\mysql-files folder)

```
mysql -uroot -pmypassword -h127.0.0.1 --port=3306 careconnect
```

Create tables (see SNOMEDCTJob.kjb in the CCRI git)

then

```
LOAD DATA local INFILE 'tempConcept.txt' into table tempConcept;
LOAD DATA local INFILE 'tempDescription.txt' into table tempDescription;
LOAD DATA local INFILE 'tempSimple.txt' into table tempSimple;
LOAD DATA local INFILE 'tempRelationship.txt' into table tempRelationship;
```

Now run the scripts in the SNOMEDCTJob.kjb (in order and ignore the import stages)

GRANT FILE ON *.* to 'fhirjpa'@'%';

## . Load in ODS Data  ##

First upload the cli tool

Windows (install putty first)

```
pscp -scp cc-cli.jar dev01@purple.testlab.nhs.uk:~/
```

Linux/Mac

```
scp cc-cli.jar dev01@purple.testlab.nhs.uk:~/
```

With cc_cli.jar run the following command:

```
java -jar cc-cli.jar "upload-ods" -t http://localhost:8080/careconnect-ri/STU3
```

## 3. Load in Observation Data  ##

With cc_cli.jar run the following command:

```
java -jar cc-cli.jar "upload-examples" -t http://localhost:8080/careconnect-ri/STU3 -a
```
