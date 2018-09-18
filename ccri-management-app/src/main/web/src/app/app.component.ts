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
  navmenu: Object[] = [{
    icon: 'looks_one',
    route: '.',
    title: 'Appointments',
    description: 'Item description',
  }, {
    icon: 'looks_two',
    route: '.',
    title: 'Second item',
    description: 'Item description',
  }, {
    icon: 'looks_3',
    route: '.',
    title: 'Third item',
    description: 'Item description',
  }, {
    icon: 'looks_4',
    route: '.',
    title: 'Fourth item',
    description: 'Item description',
  }, {
    icon: 'looks_5',
    route: '.',
    title: 'Fifth item',
    description: 'Item description',
  },
  ];


  constructor(public media: TdMediaService, public fhirService : FhirService) {

    /*
    this.fhirService.getConformance().then( (result) => {
        console.log("Result = " + result);
      }
    );
    */
   // const api: FHIR.SMART.Api = this.fhirService.smart.api;
  }

  title = 'ccri-app';
}
