import { Component, OnInit } from '@angular/core';
import {FhirService} from '../../../service/fhir.service';
import {ActivatedRoute} from '@angular/router';
import {MatDialog, MatDialogConfig, MatDialogRef, MatTableDataSource} from '@angular/material';
import {ResourceDialogComponent} from '../../../dialog/resource-dialog/resource-dialog.component';

@Component({
  selector: 'app-patient-care-plan',
  templateUrl: './patient-care-plan.component.html',
  styleUrls: ['./patient-care-plan.component.css']
})
export class PatientCarePlanComponent implements OnInit {

  planid;

  patientid;


  careplan: fhir.CarePlan = undefined;

  careTeam: fhir.CareTeam[] = [];

  forms: fhir.QuestionnaireResponse[] = [];

    prognosis: fhir.ClinicalImpression[] = [];

  activity: MatTableDataSource<any> = undefined;

  displayedColumns: string[] = ['activity', 'description', 'status'];


  flags: fhir.Flag[] = [];

  conditions: fhir.Condition[] = [];

  observations: fhir.Observation[] = [];

  constructor(private route: ActivatedRoute,
              private fhirService: FhirService,
              public dialog: MatDialog) { }


  ngOnInit() {


      this.patientid = this.route.snapshot.paramMap.get('patientid');
      this.planid = this.route.snapshot.paramMap.get('planid');


      this.forms = [];
      this.careTeam = [];
      this.prognosis = [];
      this.fhirService.get('/Flag?patient=' + this.patientid).subscribe( bundle => {
        if (bundle.entry !== undefined) {
          for (const entry of bundle.entry) {
            switch (entry.resource.resourceType) {
              case 'Flag' :
                this.flags.push(<fhir.Flag> entry.resource);
                break;
            }
          }
        }
      });

    this.fhirService.get('/Observation?patient=' + this.patientid + '&code=761869008').subscribe( bundle => {
      if (bundle.entry !== undefined) {
        for (const entry of bundle.entry) {
          switch (entry.resource.resourceType) {
            case 'Observation' :
              this.observations.push(<fhir.Observation> entry.resource);
              break;
          }
        }
      }
    });


    this.fhirService.get('/CarePlan?_id=' + this.planid +
      '&_include=CarePlan:care-team&_include=CarePlan:supporting-information&_count=50').subscribe(bundle => {

          if (bundle.entry !== undefined) {
              for (const entry of bundle.entry) {
                  switch (entry.resource.resourceType) {
                      case 'CarePlan' :
                        this.careplan = <fhir.CarePlan> entry.resource;
                        console.log(this.careplan.activity);
                        this.activity = new MatTableDataSource<any> (this.careplan.activity);
                        break;
                      case 'QuestionnaireResponse' :
                        this.forms.push(<fhir.QuestionnaireResponse> entry.resource);
                        break;
                      case 'ClinicalImpression' :
                          this.prognosis.push(<fhir.ClinicalImpression> entry.resource);
                          break;

                      case 'Condition' :
                          this.conditions.push(<fhir.Condition> entry.resource);
                          break;
                    case 'CareTeam' :
                      this.careTeam.push(<fhir.CareTeam> entry.resource);
                      break;
                  }
              }
          }
      });
  }
  select(resource) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      id: 1,
      resource: resource
    };
    const resourceDialog: MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
  }
}
