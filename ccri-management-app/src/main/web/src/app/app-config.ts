import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {FhirService} from "./service/fhir.service";
import {AuthService} from "./service/auth.service";
import {environment} from "../environments/environment";
import {root} from "rxjs/internal-compatibility";

@Injectable()
export class AppConfig {

    constructor(private http: HttpClient, private fhirService : FhirService) {}

    load() {
        // console.log('hello App' + document.baseURI);
        // only run if not localhost
        if (!document.baseURI.includes('localhost')) {
            this.http.get<any>(document.baseURI + 'camel/config/http').subscribe(result => {
                  console.log(result);

                  const access_token = localStorage.getItem('access_token_' + environment.oauth2.client_id);
                  let rootUrl : string = result.fhirServer;
                  /*
                  if (access_token === "" || access_token === null) {
                    //
                  } else {
                    if (result.fhirServer.includes('8183/ccri-fhir')) {
                      let newbaseUrl : string = 'https://data.developer-test.nhs.uk/ccri-smartonfhir/STU3';
                      console.log('swapping to smartonfhir instance: '+newbaseUrl);
                      rootUrl = newbaseUrl;
                    }
                    else {
                      if (result.fhirServer.includes('ccri-fhir')) {
                        let newbaseUrl : string = this.fhirService.getBaseUrl().replace('ccri-fhir','ccri-smartonfhir');
                        console.log('swapping to smartonfhir instance: '+ newbaseUrl);
                        rootUrl = newbaseUrl;
                      }
                    }
                  }
                  */
                  this.fhirService.setRootUrl(rootUrl);
                  this.fhirService.setGPCNRLSUrl(document.baseURI);
              },
                () => {
                    console.log('No server deteted');
                    // this.fhirServer.setRootUrl('http://127.0.0.1:8183/ccri-fhir/STU3');
                })
        }
    }



}
