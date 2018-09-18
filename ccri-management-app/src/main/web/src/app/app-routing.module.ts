import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {MainComponent} from "./component/main/main.component";
import {ConformanceComponent} from "./component/conformance/conformance.component";
import {ResourceComponent} from "./component/resource/resource.component";

const routes: Routes = [
  {  path: '', component: MainComponent },
  {  path: 'capabilitystatement', component: ConformanceComponent },
  {  path: 'resource/:id', component: ResourceComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [
    RouterModule
  ]
})

export class AppRoutingModule {}


