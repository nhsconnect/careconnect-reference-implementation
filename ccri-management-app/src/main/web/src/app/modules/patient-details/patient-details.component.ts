import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.css']
})
export class PatientDetailsComponent implements OnInit {


    documents : fhir.DocumentReference[];
    nrlsdocuments : fhir.DocumentReference[];
    encounters : fhir.Encounter[];

    observations : fhir.Observation[];

  constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute) { }

  ngOnInit() {

      let patientid = this.route.snapshot.paramMap.get('patientid');
      this.clearDown();
      this.fhirSrv.get('/Observation?patient='+patientid).subscribe(
          bundle => {
              if (bundle.entry !== undefined) {
                  for (let entry of bundle.entry) {
                      let observation: fhir.Observation = <fhir.Observation> entry.resource;
                      this.observations.push(observation);

                  }
              }

          }
      );
      this.fhirSrv.get('/DocumentReference?patient='+patientid).subscribe(
          bundle => {

              if (bundle.entry !== undefined) {
                  for (let entry of bundle.entry) {
                      let document: fhir.DocumentReference = <fhir.DocumentReference> entry.resource;
                      this.documents.push(document);

                  }
              }

          }
      );
      this.fhirSrv.get('/Encounter?patient='+patientid).subscribe(
          bundle => {
              if (bundle.entry !== undefined) {
                  for (let entry of bundle.entry) {
                      let encounter: fhir.Encounter = <fhir.Encounter> entry.resource;
                      this.encounters.push(encounter);

                  }
              }

          }
      );
      this.fhirSrv.getResource('/Patient/'+patientid).subscribe(
          patient => {
              for(let identifier of patient.identifier) {
                  if (identifier.system === 'https://fhir.nhs.uk/Id/nhs-number') {
                      this.getGPData(identifier.value);
                      this.getNRLSData(identifier.value);
                  }
              }

          }
      );
  }

  getGPData(nhsNumber : string) {
      this.fhirSrv.postGPC(nhsNumber).subscribe( bundle => {
            console.log(bundle);
          if (bundle.entry !== undefined) {

          }
      });

  }

    getNRLSData(nhsNumber : string) {
        this.fhirSrv.getNRLS('/DocumentReference?subject=https%3A%2F%2Fdemographics.spineservices.nhs.uk%2FSTU3%2FPatient%2F'+nhsNumber).subscribe( bundle => {
           if (bundle.entry !== undefined) {
               for (let entry of bundle.entry) {
                   let document: fhir.DocumentReference = <fhir.DocumentReference> entry.resource;
                   this.nrlsdocuments.push(document);

               }
           }
        })
    }

    clearDown() {

        this.observations=[];
        this.encounters=[];

        this.documents=[];
        this.nrlsdocuments = [];
    }

}
