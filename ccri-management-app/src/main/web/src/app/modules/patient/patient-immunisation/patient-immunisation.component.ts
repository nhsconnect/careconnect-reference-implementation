import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-patient-immunisation',
  templateUrl: './patient-immunisation.component.html',
  styleUrls: ['./patient-immunisation.component.css']
})
export class PatientImmunisationComponent implements OnInit {

    immunisations: fhir.Immunization[] = [];

    immunisation: fhir.Immunization = undefined;

    resource: fhir.Bundle;

    constructor(private router : Router, private fhirSrv: FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

        this.fhirSrv.get('/Immunization?patient='+patientid).subscribe(
            bundle => {
                this.resource = bundle;
                this.getResources();

            }
        );
    }

    clearDown() {
        this.immunisations = [];
    }

    getResources() {
        let bundle = this.resource;
        if (bundle.entry !== undefined) {
            for (let entry of bundle.entry) {

                switch (entry.resource.resourceType) {

                    case 'Immunization':
                        let immunisation: fhir.Immunization = <fhir.Immunization> entry.resource;
                        this.immunisations.push(immunisation);
                        break;
                }

            }
        }

    }

    onMore(linkUrl: string) {

        this.clearDown();
        this.fhirSrv.getResults(linkUrl).subscribe(bundle => {

                this.resource = bundle;
                this.getResources();

            },
            () => {
                //this.progressBar = false;
            })
    }
}
