import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {MainComponent} from "./modules/main/main.component";
import {ConformanceComponent} from "./modules/conformance/conformance.component";
import {ResourceComponent} from "./modules/resource/resource.component";

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


