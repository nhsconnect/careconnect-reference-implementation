import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {MainComponent} from "./main/main.component";
import {ConformanceComponent} from "./conformance/conformance.component";
import {ResourceComponent} from "./resource/resource.component";
import {ExplorerMainComponent} from "./explorer-main/explorer-main.component";
import {BinaryComponent} from "../component/binary/binary/binary.component";
import {EdDashboardComponent} from "./ed-dashboard/ed-dashboard.component";
import {EdEncounterListComponent} from "./ed-encounter-list/ed-encounter-list.component";



const edRoutes: Routes = [
    {
    path: 'ed',  component: EdDashboardComponent,
    children : [
      {  path: '', component: EdEncounterListComponent }
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
