import { Component } from '@angular/core';
import {TdMediaService} from "@covalent/core";
import {FhirService} from "./service/fhir.service";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  routes: Object[] = [{
    icon: 'home',
    route: '/',
    title: 'Home',
  }, {
    icon: 'lock',
    route: '.',
    title: 'OAuth2 CCRI',
  }
    , {
      icon: 'local_hospital',
      route: '.',
      title: 'A2SI',
    }
  ];
  usermenu: Object[] = [{
    icon: 'swap_horiz',
    route: '.',
    title: 'Switch account',
  }, {
    icon: 'tune',
    route: '.',
    title: 'Account settings',
  }, {
    icon: 'exit_to_app',
    route: '.',
    title: 'Sign out',
  },
  ];
  navmenu: Object[] = [];


  constructor(public media: TdMediaService, public fhirService : FhirService) {
      this.fhirService.getConformance().subscribe(capabilityStatement =>
      {

          for(let node of capabilityStatement.rest) {
              console.log('mode ' + node.mode);
              for (let resource of node.resource) {
                console.log(resource.type);
                this.navmenu.push({
                    icon: 'looks_one',
                    route: '/'+resource.type,
                    title: resource.type,
                    count: 0

                })
              }
          }
      })

  }


  title = 'ccri-app';
}
