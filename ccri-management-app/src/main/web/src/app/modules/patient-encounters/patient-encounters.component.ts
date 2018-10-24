import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {EprService} from "../../service/epr.service";

@Component({
  selector: 'app-patient-encounters',
  templateUrl: './patient-encounters.component.html',
  styleUrls: ['./patient-encounters.component.css']
})
export class PatientEncountersComponent implements OnInit {

    encounters : fhir.Encounter[] = [];

    encounter : fhir.Encounter = undefined;

    constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

        this.fhirSrv.get('/Encounter?patient='+patientid).subscribe(
            bundle => {
                if (bundle.entry !== undefined) {
                    for (let entry of bundle.entry) {

                        switch (entry.resource.resourceType) {

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

    onResourceSelected(encounter : fhir.Encounter) {
        this.encounter = encounter;
        this.router.navigate([this.encounter.id], {relativeTo: this.route });
    }

}
