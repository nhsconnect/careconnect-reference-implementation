import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {MainComponent} from "./main/main.component";
import {ConformanceComponent} from "./conformance/conformance.component";
import {ResourceComponent} from "./resource/resource.component";
import {ExplorerMainComponent} from "./explorer-main/explorer-main.component";



const eprRoutes: Routes = [
    {
    path: 'exp',  component: ExplorerMainComponent,
    children : [
    {  path: '', component: MainComponent },
    {  path: 'capabilitystatement', component: ConformanceComponent },
    {  path: 'resource/:resourceType', component: ResourceComponent }
    ]
}
];

@NgModule({

      imports: [
    RouterModule.forChild(eprRoutes)
],
    exports: [
    RouterModule
]
  ,
  declarations: []
})
export class EprRoutingModule { }
