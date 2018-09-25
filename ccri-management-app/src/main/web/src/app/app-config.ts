import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {FhirService} from "./service/fhir.service";

@Injectable()
export class AppConfig {

    constructor(private http: HttpClient, private fhirServer : FhirService) {}

    load() {
        console.log('hello App' + document.baseURI);
        this.http.get<any>(document.baseURI+'camel/config/http').subscribe( result => {
            console.log(result);
                this.fhirServer.setFHIRServerBase(result.fhirServer);
        },
            () => {
            console.log('No server deteted');
            this.fhirServer.setFHIRServerBase('http://data.developer-test.nhs.uk/ccri-fhir/STU3');
            })
    }



}
