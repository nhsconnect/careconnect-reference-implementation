In this directory

mvn install 

docker build . -t ccri-fhir

docker tag ccri-fhir thorlogic/ccri-fhir

docker push thorlogic/ccri-fhir


docker run -d -p 8185:8183 ccri-fhir

