import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class FhirService {


  private baseUrl : string = 'https://data.developer.nhs.uk/ccri-fhir/STU3';

  //  private baseUrl : string = 'http://127.0.0.1:8183/ccri-fhir/STU3';


    // public smart: SMARTClient;

  public conformance : fhir.CapabilityStatement;

  conformanceChange : EventEmitter<any> = new EventEmitter();

  constructor( private http: HttpClient) {


    /*
    const clientSettings = {
      client_id: 'diabetes',
      // Adding the scopes launch or launch/patient depending upon the SMART on FHIR Launch sequence
      scope: 'user/*.read launch openid profile',
      redirect_uri: 'http://localhost:4200/redirect',
      state: '12312'
    };

    console.log('Fhir Service Construct');
    const oauth2Configuration = {
      client: clientSettings,
      server: "http://127.0.0.1:8183/ccri-fhir/STU3"
    };
    */
    // The authorize method of the SMART on FHIR JS client, will take care of completing the OAuth2.0 Workflow



  }

  public getConformanceChange() {
    return this.conformanceChange;
  }

  public getFHIRServerBase() {
    return this.baseUrl;
  }

    public setFHIRServerBase(server : string) {
        this.baseUrl = server;
    }

    public getConformance() {
    //  console.log('called CapabilityStatement');
      this.http.get<any>(this.baseUrl+'/metadata',{ 'headers' : {}}).subscribe(capabilityStatement =>
      {
          this.conformance = capabilityStatement;

          this.conformanceChange.emit(capabilityStatement);
      });
  }

  public getResults(url : string) : Observable<fhir.Bundle> {
    return this.http.get<any>(url,{ 'headers' : {}});
  }
}
