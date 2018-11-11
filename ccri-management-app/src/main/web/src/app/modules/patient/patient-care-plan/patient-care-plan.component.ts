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


  flags : fhir.Flag[] = [];

  conditions : fhir.Condition[] = [];

  observations : fhir.Observation[] = [];

  constructor(private route: ActivatedRoute,
    private fhirService : FhirService) { }


  ngOnInit() {


      this.patientid = this.route.snapshot.paramMap.get('patientid');
      this.planid = this.route.snapshot.paramMap.get('planid');


      this.forms = [];
      this.fhirService.get('/Flag?patient='+this.patientid).subscribe( bundle => {
        if (bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            switch (entry.resource.resourceType) {
              case 'Flag' :
                this.flags.push(<fhir.Flag> entry.resource);
                break;
            }
          }
        }
      });

    this.fhirService.get('/Observation?patient='+this.patientid+'&code=761869008').subscribe( bundle => {
      if (bundle.entry != undefined) {
        for (let entry of bundle.entry) {
          switch (entry.resource.resourceType) {
            case 'Observation' :
              this.observations.push(<fhir.Observation> entry.resource);
              break;
          }
        }
      }
    });


    this.fhirService.get('/CarePlan?_id='+this.planid+'&_include=CarePlan:condition&_include=CarePlan:supporting-information&_count=50').subscribe(bundle=> {

          if (bundle.entry != undefined) {
              for (let entry of bundle.entry) {
                  switch(entry.resource.resourceType) {
                      case 'CarePlan' :
                        this.careplan = <fhir.CarePlan> entry.resource;
                        break;
                      case 'QuestionnaireResponse' :
                        this.forms.push(<fhir.QuestionnaireResponse> entry.resource);
                        break;

                      case 'Condition' :
                          this.conditions.push(<fhir.Condition> entry.resource);
                          break;
                  }
              }
          }
      });
  }

}
