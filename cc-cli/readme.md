ODS Import

java -jar cc-cli.jar "upload-ods" -t http://localhost/careconnect-ri/STU3


docker build . -t thorlogic/ccri-dataload

-- docker tag ccri-document thorlogic/ccri-document

docker push thorlogic/ccri-dataload