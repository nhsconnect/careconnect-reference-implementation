import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-patient-medication',
  templateUrl: './patient-medication.component.html',
  styleUrls: ['./patient-medication.component.css']
})
export class PatientMedicationComponent implements OnInit {

    medicationRequests : fhir.MedicationRequest[] = [];

    gpMedicationRequest : fhir.MedicationRequest[] = [];

    gpMedication : fhir.Medication[] = [];

    medicationRequest : fhir.MedicationRequest = undefined;

    medicationStatements : fhir.MedicationStatement[] = [];

    gpMedicationStatement : fhir.MedicationStatement[] = [];

    medicationStatement : fhir.MedicationStatement = undefined;

    medicationDispenses : fhir.MedicationDispense[] = [];

    medicationDispense : fhir.MedicationDispense = undefined;

    patient : fhir.Patient = undefined;

  acutecolor = 'info';
  gpcolor = 'info';

    constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

      this.eprService.getGPCStatusChangeEvent().subscribe( colour => {
        this.gpcolor = colour;
      });

      this.eprService.getAcuteStatusChangeEvent().subscribe( colour => {
        this.acutecolor = colour;
      });


        this.fhirSrv.get('/MedicationRequest?patient='+patientid).subscribe(
            bundle => {
                if (bundle.entry !== undefined) {
                    for (let entry of bundle.entry) {

                        switch (entry.resource.resourceType) {

                            case 'MedicationRequest':
                                let medicationRequest: fhir.MedicationRequest = <fhir.MedicationRequest> entry.resource;
                                this.medicationRequests.push(medicationRequest);
                                break;
                        }

                    }
                }

            }
        );

        this.fhirSrv.get('/MedicationStatement?patient='+patientid).subscribe(
            bundle => {
                if (bundle.entry !== undefined) {
                    for (let entry of bundle.entry) {

                        switch (entry.resource.resourceType) {

                            case 'MedicationStatement':
                                let medicationStatement: fhir.MedicationStatement = <fhir.MedicationStatement> entry.resource;
                                this.medicationStatements.push(medicationStatement);
                                break;
                        }

                    }
                }
              this.eprService.acuteConnectStatusEmitter.emit('primary');
            },
          ()=> {
            this.eprService.acuteConnectStatusEmitter.emit('warn');
          }
        );


        this.fhirSrv.get('/MedicationDispense?patient='+patientid).subscribe(
            bundle => {
                if (bundle.entry !== undefined) {
                    for (let entry of bundle.entry) {

                        switch (entry.resource.resourceType) {

                            case 'MedicationDispense':
                                let medicationDispense: fhir.MedicationDispense = <fhir.MedicationDispense> entry.resource;
                                this.medicationDispenses.push(medicationDispense);
                                break;
                        }

                    }
                }

            }
        );

      this.fhirSrv.getResource('/Patient/'+patientid).subscribe(
        patient => {
          this.patient = patient;
        }
        ,()=> {

        }
        , ()=> {
          for(let identifier of this.patient.identifier) {
            if (identifier.system === 'https://fhir.nhs.uk/Id/nhs-number') {
              this.getGPData(identifier.value);

            }
          }
        }
      );


    }

  getGPData(nhsNumber : string) {

    this.gpMedicationStatement = [];
    this.gpMedicationRequest  = [];

    this.eprService.gpConnectStatusEmitter.emit('info');


    this.fhirSrv.postGPC(nhsNumber).subscribe( bundle => {
      console.log(bundle);
      if (bundle.entry !== undefined) {
        for (let entry of bundle.entry) {
          // console.log(entry.resource.resourceType);
          switch (entry.resource.resourceType) {

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
      this.eprService.gpConnectStatusEmitter.emit('primary');


    },()=>
    {
      console.log('failed to retrieve data from GP Connect');
      this.eprService.gpConnectStatusEmitter.emit('warn');

    });

  }

}
