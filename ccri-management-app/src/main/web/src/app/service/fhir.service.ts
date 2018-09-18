import { Injectable } from '@angular/core';
import SMARTClient = FHIR.SMART.SMARTClient;


@Injectable({
  providedIn: 'root'
})
export class FhirService {

  public smart: SMARTClient;

  constructor() {

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
    // The authorize method of the SMART on FHIR JS client, will take care of completing the OAuth2.0 Workflow

  }
}
