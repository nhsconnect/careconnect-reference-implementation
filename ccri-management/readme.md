

docker build . -t ccri-management

docker tag ccri-management thorlogic/ccri-management


docker push thorlogic/ccri-management



Command for retrieving certs from ODS

openssl s_client -showcerts -connect server.edu:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >mycertfile.pem

Convert to der

openssl x509 -outform der -in mycertfile.pem -out odscertificate.der

Import into keystore.jks

keytool -import -alias ods -keystore keystore.jks -file odscertificate.der