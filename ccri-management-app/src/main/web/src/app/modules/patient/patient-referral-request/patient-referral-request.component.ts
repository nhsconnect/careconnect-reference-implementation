import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-patient-referral-request',
  templateUrl: './patient-referral-request.component.html',
  styleUrls: ['./patient-referral-request.component.css']
})
export class PatientReferralRequestComponent implements OnInit {

    referrals: fhir.ReferralRequest[] = [];

    resource: fhir.Bundle;

    constructor(private router : Router, private fhirSrv: FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

        this.fhirSrv.get('/ReferralRequest?patient='+patientid).subscribe(
            bundle => {
                this.resource = bundle;
                this.getResources();

            }
        );
    }

    clearDown() {
        this.referrals = [];
    }

    getResources() {
        let bundle = this.resource;
        if (bundle.entry !== undefined) {
            for (let entry of bundle.entry) {

                switch (entry.resource.resourceType) {

                    case 'ReferralRequest':
                        let referralRequest: fhir.ReferralRequest = <fhir.ReferralRequest> entry.resource;
                        this.referrals.push(referralRequest);
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
