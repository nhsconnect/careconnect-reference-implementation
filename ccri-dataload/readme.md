ODS Import

java -jar cc-cli.jar "upload-ods" -t http://localhost/careconnect-ri/STU3


Validate

javac target/cc-cli.jar validate -n /Development/QRISK-ME.json


** Docker + SQL **

docker exec -it ccrisql /bin/bash

psql -d careconnect -U fhirjpa



docker build . -t thorlogic/ccri-dataload

-- docker tag ccri-document thorlogic/ccri-document

docker push thorlogic/ccri-dataload