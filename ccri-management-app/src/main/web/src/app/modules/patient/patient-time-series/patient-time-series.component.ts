import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../../service/fhir.service";
import {ActivatedRoute, Router} from "@angular/router";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-patient-time-series',
  templateUrl: './patient-time-series.component.html',
  styleUrls: ['./patient-time-series.component.css']
})
export class PatientTimeSeriesComponent implements OnInit {

  conditions: fhir.Condition[] ;
  encounters: fhir.Encounter[] ;
  procedures: fhir.Procedure[] ;

  ready = false;

  constructor(private router : Router, private fhirSrv: FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

  ngOnInit() {



      let patientid = this.route.snapshot.paramMap.get('patientid');

      console.log(patientid);

      this.clearDown();
      this.ready= false;
      this.fhirSrv.get('/Patient?_id='+patientid+'&_revinclude=Condition:patient&_revinclude=Encounter:patient&_revinclude=Procedure:patient&_count=100').subscribe(
          bundle => {
              if (bundle.entry !== undefined) {

                  for (let entry of bundle.entry) {
                      switch (entry.resource.resourceType) {

                          case 'Condition':
                              this.conditions.push(<fhir.Condition> entry.resource);
                              break;
                          case 'Encounter':
                              this.encounters.push(<fhir.Encounter> entry.resource);
                              break;
                          case 'Procedure':
                              this.procedures.push(<fhir.Procedure> entry.resource);
                              break;
                      }

                  }
              }

          }
          ,()=> {

          }
          , ()=> {
this.ready = true;
          }
      );

  }

  selectEncounter(encounterId: string) {
      console.log(encounterId);
      this.router.navigate([ '../encounter/'+encounterId], {relativeTo: this.route });
  }

    clearDown() {
        this.encounters=[];
        this.procedures =[];
        this.conditions =[];
    }

}
