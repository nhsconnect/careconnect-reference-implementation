In this directory

mvn install 

docker build . -t ccri-smartonfhir

docker tag ccri-smartonfhir thorlogic/ccri-smartonfhir

docker push thorlogic/ccri-smartonfhir


docker run -d -p 8186:8184 ccri-smartonfhir

