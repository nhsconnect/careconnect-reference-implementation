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
import {BinaryComponent} from "../component/binary/binary/binary.component";
import {PatientTimeSeriesComponent} from "./patient-time-series/patient-time-series.component";
import {PatientImmunisationComponent} from "./patient-immunisation/patient-immunisation.component";
import {PatientMedicationComponent} from "./patient-medication/patient-medication.component";
import {AuthGuard} from "../service/auth-guard";



const edRoutes: Routes = [
    {
    path: 'ed',  component: EdDashboardComponent,
    children : [
        {  path: '', canActivate: [AuthGuard], component: TriageComponent },
        {  path: 'caseload',canActivate: [AuthGuard], component: EdEncounterListComponent },
      {  path: 'capacity',canActivate: [AuthGuard], component: CapacityComponent },
        {  path: 'patient/:patientid', canActivate: [AuthGuard], component: PatientMainComponent,
           children : [
               { path: '', component: PatientSummaryComponent },
               { path: 'summary', component: PatientSummaryComponent },
               { path: 'immunisation', component: PatientImmunisationComponent },
               { path: 'medication', component: PatientMedicationComponent },
               { path: 'timeline', component: PatientTimeSeriesComponent },
               { path: 'observation', component: PatientVitalSignsComponent },
               { path: 'encounter', component: PatientEncountersComponent },
               { path: 'encounter/:encounterid', component: PatientEncounterDetailComponent },
               { path: 'document', component: PatientDocumentsComponent },
               { path: 'procedure', component: PatientProcedureComponent },
               { path: 'document/:docid', component: BinaryComponent },
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
