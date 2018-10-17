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
                this.fhirServer.setRootUrl(result.fhirServer);
                this.fhirServer.setGPCNRLSUrl(document.baseURI);
        },
            () => {
            console.log('No server deteted');
           // this.fhirServer.setRootUrl('http://127.0.0.1:8183/ccri-fhir/STU3');
            })
    }



}
