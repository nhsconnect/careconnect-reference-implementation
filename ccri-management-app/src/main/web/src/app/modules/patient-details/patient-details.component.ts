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

    gpallergies : fhir.AllergyIntolerance[] = [];
    gpMedicationStatement : fhir.MedicationStatement[]= [];
    gpMedicationRequest : fhir.MedicationRequest[] = [];
    gpMedication : fhir.Medication[] = [];


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
      this.gpallergies = [];
      this.gpMedicationStatement = [];
      this.gpMedicationRequest  = [];

      this.fhirSrv.postGPC(nhsNumber).subscribe( bundle => {
            console.log(bundle);
          if (bundle.entry !== undefined) {
                for (let entry of bundle.entry) {
                   // console.log(entry.resource.resourceType);
                    switch (entry.resource.resourceType) {
                        case 'AllergyIntolerance':
                            this.gpallergies.push(<fhir.AllergyIntolerance> entry.resource);
                            break;
                        case 'MedicationRequest':
                            this.gpMedicationRequest.push(<fhir.MedicationRequest> entry.resource);
                            break;
                        case 'MedicationStatement':
                            this.gpMedicationStatement.push(<fhir.MedicationStatement> entry.resource);
                            break;
                        case 'Medication':
                            this.gpMedication.push(<fhir.Medication> entry.resource);
                            break;

                    }
                }
          }
          for (let pres of this.gpMedicationRequest) {
              let meds = pres.medicationReference.reference.split('/');
              for (let med of this.gpMedication) {

                  if (meds[1] == med.id) {
                     // console.log(med);
                      pres.medicationReference.display = med.code.coding[0].display;
                      if (med.code.coding[0].display === undefined || med.code.coding[0].display == '') {
                          pres.medicationReference.display = med.code.coding[0].extension[0].extension[1].valueString;
                      }
                  }
              }
          }
          for (let pres of this.gpMedicationStatement) {
              let meds = pres.medicationReference.reference.split('/');
              for (let med of this.gpMedication) {
                  if (meds[1] == med.id) {
                     // console.log(med);
                      pres.medicationReference.display = med.code.coding[0].display;
                      if (med.code.coding[0].display === undefined || med.code.coding[0].display == '') {
                          pres.medicationReference.display = med.code.coding[0].extension[0].extension[1].valueString;
                      }
                  }
              }
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
