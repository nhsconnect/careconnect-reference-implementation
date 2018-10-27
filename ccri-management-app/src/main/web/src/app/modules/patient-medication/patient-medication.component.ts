import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {EprService} from "../../service/epr.service";

@Component({
  selector: 'app-patient-medication',
  templateUrl: './patient-medication.component.html',
  styleUrls: ['./patient-medication.component.css']
})
export class PatientMedicationComponent implements OnInit {

    medicationRequests : fhir.MedicationRequest[] = [];

    medicationRequest : fhir.MedicationRequest = undefined;

    medicationStatements : fhir.MedicationStatement[] = [];

    medicationStatement : fhir.MedicationStatement = undefined;

    medicationDispenses : fhir.MedicationDispense[] = [];

    medicationDispense : fhir.MedicationDispense = undefined;

    constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

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
    }

}
