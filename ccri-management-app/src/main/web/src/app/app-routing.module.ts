import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {EdDashboardComponent} from "./modules/ed-dashboard/ed-dashboard.component";
import {MapComponent} from "./component/map/map.component";

const routes: Routes = [
  {  path: '', redirectTo: 'exp', pathMatch: 'full' },

    {  path: 'error', redirectTo: 'exp'
    }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes, { paramsInheritanceStrategy: 'always'}) ],
  exports: [
    RouterModule
  ]
})

export class AppRoutingModule {}


