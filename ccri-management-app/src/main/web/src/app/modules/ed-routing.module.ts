import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {EdDashboardComponent} from "./ed-dashboard/ed-dashboard.component";
import {EdEncounterListComponent} from "./ed-encounter-list/ed-encounter-list.component";
import {PatientDetailsComponent} from "./patient-details/patient-details.component";
import {TriageComponent} from "./triage/triage.component";



const edRoutes: Routes = [
    {
    path: 'ed',  component: EdDashboardComponent,
    children : [
        {  path: '', component: TriageComponent },
        {  path: 'caseload', component: EdEncounterListComponent },
        {  path: 'patient/:patientid', component: PatientDetailsComponent }
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
