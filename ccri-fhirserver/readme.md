In this directory

mvn install 

docker build . -t ccri-fhirserver

docker tag ccri-fhirserver thorlogic/ccri-fhirserver

docker push thorlogic/ccri-fhirserver


docker run -d -p 8186:8184 ccri-ccri-fhirserver

