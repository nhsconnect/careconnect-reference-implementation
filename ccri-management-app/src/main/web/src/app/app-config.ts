import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FhirService} from './service/fhir.service';
import {environment} from '../environments/environment';

@Injectable()
export class AppConfig {

    constructor(private http: HttpClient, private fhirService: FhirService) {}

    load() {
        // console.log('hello App' + document.baseURI);
        // only run if not localhost
        console.log('baseURI = ' + document.baseURI);

        if (!document.baseURI.includes('localhost')) {
            console.log('calling config endpoint: ' + document.baseURI + 'camel/config/http');
            this.http.get<any>(document.baseURI + 'camel/config/http').subscribe(result => {
                  console.log('app config fhirServer retrieved.');
                  console.log(result);
                  /*
                  const access_token = localStorage.getItem('access_token_' + environment.oauth2.client_id);
                  if (access_token === "" || access_token === null) {
                    //
                  } else {
                    if (result.fhirServer.includes('8183/ccri-fhir')) {
                      let newbaseUrl: string = 'https://data.developer-test.nhs.uk/ccri-smartonfhir/STU3';
                      console.log('swapping to smartonfhir instance: '+newbaseUrl);
                      rootUrl = newbaseUrl;
                    }
                    else {
                      if (result.fhirServer.includes('ccri-fhir')) {
                        let newbaseUrl: string = this.fhirService.getBaseUrl().replace('ccri-fhir','ccri-smartonfhir');
                        console.log('swapping to smartonfhir instance: '+ newbaseUrl);
                        rootUrl = newbaseUrl;
                      }
                    }
                  }
                  */
                  const rootUrl: string = result.fhirServer;
                  this.fhirService.setRootUrl(rootUrl);
                  this.fhirService.setGPCNRLSUrl(document.baseURI);
              },
                error => {
                    console.log(error);
                    console.log('No server detected');
                    // this.fhirServer.setRootUrl('http://127.0.0.1:8183/ccri-fhir/STU3');
                });
        }
    }



}
