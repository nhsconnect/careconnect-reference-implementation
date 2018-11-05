import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../../service/fhir.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-patient-care-plan',
  templateUrl: './patient-care-plan.component.html',
  styleUrls: ['./patient-care-plan.component.css']
})
export class PatientCarePlanComponent implements OnInit {

  planid;

  patientid;


  careplan : fhir.CarePlan = undefined;

  forms : fhir.QuestionnaireResponse[] = [];

  constructor(private route: ActivatedRoute,
    private fhirService : FhirService) { }


  ngOnInit() {


      this.patientid = this.route.snapshot.paramMap.get('patientid');
      this.planid = this.route.snapshot.paramMap.get('planid');


      this.forms = [];
      this.fhirService.get('/CarePlan?_id='+this.planid+'&_include=*&_revinclude=*&_count=50').subscribe(bundle=> {

          if (bundle.entry != undefined) {
              for (let entry of bundle.entry) {
                  switch(entry.resource.resourceType) {
                      case 'CarePlan' :
                        this.careplan = <fhir.CarePlan> entry.resource;
                        break;
                      case 'QuestionnaireResponse' :
                        this.forms.push(<fhir.QuestionnaireResponse> entry.resource);
                        break;
                  }
              }
          }
      });
  }

}
