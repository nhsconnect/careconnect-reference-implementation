import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {FhirService} from "./service/fhir.service";
import {AuthService} from "./service/auth.service";

@Injectable()
export class AppConfig {

    constructor(private http: HttpClient, private fhirServer : FhirService, private authService : AuthService) {}

    load() {
        // console.log('hello App' + document.baseURI);
        // only run if not localhost
        if (!document.baseURI.includes('localhost')) {
            this.http.get<any>(document.baseURI + 'camel/config/http').subscribe(result => {
                    console.log(result);
                    this.fhirServer.setRootUrl(result.fhirServer);
                    if (this.authService.isLoggedOn()) {
                       this.authService.setBaseUrlOAuth2();
                    }
                    this.fhirServer.setGPCNRLSUrl(document.baseURI);
                },
                () => {
                    console.log('No server deteted');
                    // this.fhirServer.setRootUrl('http://127.0.0.1:8183/ccri-fhir/STU3');
                })
        }
    }



}
