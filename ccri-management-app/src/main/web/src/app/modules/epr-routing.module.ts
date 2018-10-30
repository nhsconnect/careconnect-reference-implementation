import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {MainComponent} from "./explorer/main/main.component";
import {ConformanceComponent} from "./explorer/conformance/conformance.component";
import {ResourceComponent} from "./explorer/resource/resource.component";
import {ExplorerMainComponent} from "./explorer/explorer-main/explorer-main.component";
import {BinaryComponent} from "../component/binary/binary/binary.component";
import {AuthGuard} from "../service/auth-guard";



const eprRoutes: Routes = [
    {
    path: 'exp',  component: ExplorerMainComponent,
    children : [
    {  path: '', component: MainComponent },
    {  path: 'capabilitystatement', component: ConformanceComponent },
    {  path: 'resource/:resourceType', canActivate: [AuthGuard], component: ResourceComponent },
        {
            path: 'binary/:docid',
            canActivate: [AuthGuard],
            component: BinaryComponent
        },
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
