import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {EdDashboardComponent} from "./modules/ed-dashboard/ed-dashboard.component";

const routes: Routes = [
  {  path: '', redirectTo: 'exp', pathMatch: 'full' },
  {  path: 'ed', component: EdDashboardComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [
    RouterModule
  ]
})

export class AppRoutingModule {}


