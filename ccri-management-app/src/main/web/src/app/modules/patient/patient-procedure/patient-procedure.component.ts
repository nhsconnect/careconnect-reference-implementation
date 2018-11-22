import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-patient-procedure',
  templateUrl: './patient-procedure.component.html',
  styleUrls: ['./patient-procedure.component.css']
})
export class PatientProcedureComponent implements OnInit {

    procedures: fhir.Procedure[] = [];

    resource: fhir.Bundle;

    constructor(private router : Router, private fhirSrv: FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

        this.fhirSrv.get('/Procedure?patient='+patientid).subscribe(
            bundle => {
                this.resource = bundle;
                this.getResources();

            }
        );
    }

    clearDown() {
        this.procedures = [];
    }

    getResources() {
        let bundle = this.resource;
        if (bundle.entry !== undefined) {
            for (let entry of bundle.entry) {

                switch (entry.resource.resourceType) {

                    case 'Procedure':
                        let procedure: fhir.Procedure = <fhir.Procedure> entry.resource;
                        this.procedures.push(procedure);
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

    selectEncounter(encounter: fhir.Reference) {

    let str = encounter.reference.split('/');
    console.log(this.route.root);
    this.router.navigate(['..','encounter',str[1]] , { relativeTo : this.route});

  }

}
