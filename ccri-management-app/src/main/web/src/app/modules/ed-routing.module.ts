import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {EdDashboardComponent} from "./ed-dashboard/ed-dashboard.component";
import {EdEncounterListComponent} from "./ed-encounter-list/ed-encounter-list.component";
import {PatientMainComponent} from "./patient-main/patient-main.component";
import {TriageComponent} from "./triage/triage.component";
import {CapacityComponent} from "./capacity/capacity.component";
import {PatientSummaryComponent} from "./patient-summary/patient-summary.component";
import {PatientVitalSignsComponent} from "./patient-vital-signs/patient-vital-signs.component";
import {PatientEncountersComponent} from "./patient-encounters/patient-encounters.component";
import {PatientDocumentsComponent} from "./patient-documents/patient-documents.component";
import {PatientEncounterDetailComponent} from "./patient-encounter-detail/patient-encounter-detail.component";
import {PatientProcedureComponent} from "./patient-procedure/patient-procedure.component";



const edRoutes: Routes = [
    {
    path: 'ed',  component: EdDashboardComponent,
    children : [
        {  path: '', component: TriageComponent },
        {  path: 'caseload', component: EdEncounterListComponent },
      {  path: 'capacity', component: CapacityComponent },
        {  path: 'patient/:patientid', component: PatientMainComponent,
           children : [
               { path: '', component: PatientSummaryComponent },
               { path: 'summary', component: PatientSummaryComponent },
               { path: 'observation', component: PatientVitalSignsComponent },
               { path: 'encounter', component: PatientEncountersComponent },
               { path: 'encounter/:encounterid', component: PatientEncounterDetailComponent },
               { path: 'document', component: PatientDocumentsComponent },
               { path: 'procedure', component: PatientProcedureComponent },
           ]}
    ]
}
];

@NgModule({

      imports: [
    RouterModule.forChild(edRoutes)
],
    exports: [
    RouterModule
]
  ,
  declarations: []
})
export class EdRoutingModule { }
