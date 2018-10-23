import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {EprService} from "../../service/epr.service";
import {MatChip} from "@angular/material";

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.css']
})
export class PatientDetailsComponent implements OnInit {


    documents : fhir.DocumentReference[];
    nrlsdocuments : fhir.DocumentReference[];
    encounters : fhir.Encounter[];
    patient : fhir.Patient = undefined;

    gpallergies : fhir.AllergyIntolerance[] = [];
    gpMedicationStatement : fhir.MedicationStatement[]= [];
    gpMedicationRequest : fhir.MedicationRequest[] = [];
    gpMedication : fhir.Medication[] = [];
    gpPatient : fhir.Patient[] = [];
    gpOrganisation : fhir.Organization[] = [];
    gpPractitioner : fhir.Practitioner[] =[];


    lhcreEncounters : fhir.Encounter[];
    lhcreAllergies : fhir.AllergyIntolerance[] = [];
    lhcreMedicationStatement : fhir.MedicationStatement[]= [];
    lhcreConditions : fhir.Condition[];

    yascolor = 'info';
    acutecolor = 'info';
    gpcolor = 'info';
    nrlscolor = 'info';
    @ViewChild('gpchip') gpchip : MatChip;


    observations : fhir.Observation[];

  constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

  ngOnInit() {

      let patientid = this.route.snapshot.paramMap.get('patientid');
    this.eprService.setTitle('Health Information Exchange Portal');
      this.acutecolor = 'info';
      this.yascolor = 'info';
      this.clearDown();
      this.fhirSrv.get('/Patient?_id='+patientid+'&_revinclude=Condition:patient&_revinclude=AllergyIntolerance:patient&_revinclude=MedicationStatement:patient&_count=100').subscribe(
          bundle => {
              if (bundle.entry !== undefined) {
                  for (let entry of bundle.entry) {
                      switch (entry.resource.resourceType) {
                          case 'Patient':
                              let patient: fhir.Patient = <fhir.Patient> entry.resource;
                              this.patient = patient;
                              break;
                          case 'Condition':
                              this.lhcreConditions.push(<fhir.Condition> entry.resource);
                              break;
                          case 'AllergyIntolerance':
                              this.lhcreAllergies.push(<fhir.AllergyIntolerance> entry.resource);
                              break;
                          case 'MedicationStatement':
                              this.lhcreMedicationStatement.push(<fhir.MedicationStatement> entry.resource);
                              break;
                      }
                      /*
                      if (entry.resource.resourceType == 'Encounter') {
                          this.lhcreEncounters.push(<fhir.Encounter> entry.resource);
                      }
                      */
                  }
              }
            this.acutecolor = 'primary';
            this.yascolor = 'primary';
          }
          ,()=> {
            this.acutecolor = 'warn';
            this.yascolor = 'warn';
        }
          , ()=> {
              for(let identifier of this.patient.identifier) {
                  if (identifier.system === 'https://fhir.nhs.uk/Id/nhs-number') {
                      this.getGPData(identifier.value);
                      this.getNRLSData(identifier.value);
                  }
              }
          }
      );
      this.fhirSrv.get('/Patient?_id='+patientid+'&_revinclude=Observation:patient&_revinclude=DocumentReference:patient&_revinclude=Encounter:patient&_count=100').subscribe(
          bundle => {
              if (bundle.entry !== undefined) {
                  for (let entry of bundle.entry) {

                      switch (entry.resource.resourceType) {

                          case 'Observation':
                              let observation: fhir.Observation = <fhir.Observation> entry.resource;
                              this.observations.push(observation);
                              break;
                          case 'DocumentReference':
                              let document: fhir.DocumentReference = <fhir.DocumentReference> entry.resource;
                              this.documents.push(document);
                              break;
                          case 'Encounter':
                              let encounter: fhir.Encounter = <fhir.Encounter> entry.resource;
                              this.encounters.push(encounter);
                              break;
                      }

                  }
              }

          }
      );


  }

  getGPData(nhsNumber : string) {
      this.gpallergies = [];
      this.gpMedicationStatement = [];
      this.gpMedicationRequest  = [];
      this.gpPatient  = [];
      this.gpPractitioner  = [];
      this.gpOrganisation = [];
      this.gpcolor = 'info';


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
                        case 'Patient':
                            this.gpPatient.push(<fhir.Patient> entry.resource);
                            break;
                        case 'Practitioner':
                            this.gpPractitioner.push(<fhir.Practitioner> entry.resource);
                            break;
                        case 'Organization':
                            this.gpOrganisation.push(<fhir.Organization> entry.resource);
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
          this.gpcolor= 'primary';
      },()=>
      {
        console.log('failed to retrieve data from GP Connect');
        this.gpcolor = 'warn';
      });

  }

    getNRLSData(nhsNumber : string) {
        this.nrlscolor = 'info';
        this.fhirSrv.getNRLS('/DocumentReference?subject=https%3A%2F%2Fdemographics.spineservices.nhs.uk%2FSTU3%2FPatient%2F'+nhsNumber).subscribe( bundle => {
           if (bundle.entry !== undefined) {
               for (let entry of bundle.entry) {
                   let document: fhir.DocumentReference = <fhir.DocumentReference> entry.resource;
                   this.nrlsdocuments.push(document);

               }
           }
           this.nrlscolor='primary';
        },
          ()=> {
          console.log('failed to retrieve data from NRLS');
          this.gpcolor = 'warn';
          })
    }

    clearDown() {

        this.observations=[];
        this.encounters=[];
        this.patient=undefined;

        this.documents=[];
        this.nrlsdocuments = [];

        this.lhcreEncounters =[];
        this.lhcreAllergies =[];
        this.lhcreMedicationStatement =[];
        this.lhcreConditions =[];
    }

    getFirstName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";
        // Move to address
        let name = "";
        if (patient.name[0].given != undefined && patient.name[0].given.length>0) name += ", "+ patient.name[0].given[0];

        if (patient.name[0].prefix != undefined && patient.name[0].prefix.length>0) name += " (" + patient.name[0].prefix[0] +")" ;
        return name;

    }

    getNHSIdentifier(patient : fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.identifier == undefined || patient.identifier.length == 0)
            return "";
        // Move to address
        var NHSNumber :String = "";
        for (var f=0;f<patient.identifier.length;f++) {
            if (patient.identifier[f].system.includes("nhs-number") )
                NHSNumber = patient.identifier[f].value;
        }
        return NHSNumber;

    }

    getLastName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";

        let name = "";
        if (patient.name[0].family != undefined) name += patient.name[0].family.toUpperCase();
        return name;

    }

}
