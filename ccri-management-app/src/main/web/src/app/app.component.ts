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
    route: '../ccri-auth/',
    title: 'OAuth2 Server',
  }
    , {
      icon: 'local_hospital',
      route: '../document-viewer/',
      title: 'FHIR Document Viewer',
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


  constructor(public media: TdMediaService, public fhirSrv: FhirService) {

      this.fhirSrv.getConformanceChange().subscribe(capabilityStatement =>
      {
          this.navmenu = [];
          for(let node of capabilityStatement.rest) {
              //console.log('mode ' + node.mode);
              for (let resource of node.resource) {
                 // console.log(resource.type);
                  let count= 0;
                  if (resource.extension !== undefined) {

                      for (let extension of resource.extension) {
                          if (extension.url.endsWith('resourceCount')) {
                              count = extension.valueDecimal;

                          }
                      }
                  }
                  this.navmenu.push({
                      icon: 'looks_one',
                      route: '/'+resource.type,
                      title: resource.type,
                      count: count

                  })
              }
          }
      });

      this.fhirSrv.getConformance();

  }


    title = 'ccri-app';
}
