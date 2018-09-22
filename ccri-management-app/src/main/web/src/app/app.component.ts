import {Component, ViewContainerRef} from '@angular/core';
import {IAlertConfig, IConfirmConfig, TdDialogService, TdMediaService} from "@covalent/core";
import {FhirService} from "./service/fhir.service";
import {Router} from "@angular/router";
import {ErrorsHandler} from "./error-handler";
import {MessageService} from "./service/message.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  routes: Object[] = [ {
    icon: 'lock',
    route: 'https://data.developer.nhs.uk/ccri-auth/',
    title: 'OAuth2 (SMART on FHIR) Server',
  }
    , {
      icon: 'local_hospital',
      route: 'https://data.developer.nhs.uk/document-viewer/',
      title: 'FHIR Document Viewer',
    }
  ];

  serverMenu : Object[] = [
      {
      icon: 'swap_horiz',
      route: 'https://data.developer.nhs.uk/ccri-fhir/STU3',
      title: 'Care Connect RI'
  },
      {
          icon: 'swap_horiz',
          route: 'https://data.developer.nhs.uk/ccri-smartonfhir/STU3',
          title: 'Care Connect RI (Secure)'
      },
      {
          icon: 'swap_horiz',
          route: 'https://directory.spineservices.nhs.uk/STU3',
          title: 'FHIR ODS API'
      },
      {
          icon: 'swap_horiz',
          route: 'https://fhir.hl7.org.uk/STU3',
          title: 'HL7 UK FHIR Reference'
      },
      {
          icon: 'swap_horiz',
          route: 'https://fhir.nhs.uk/STU3',
          title: 'NHS Digital FHIR Reference'
      },
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

  title : string ='Care Connect Reference Implemenation';

  constructor(public media: TdMediaService,
              public fhirSrv: FhirService,
              private router : Router,
              private messageService : MessageService,
              private _dialogService: TdDialogService,
              private _viewContainerRef: ViewContainerRef
  ) {

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

      this.messageService.getMessageEvent().subscribe(
          error => {

              let alertConfig : IAlertConfig = {
                  message : error.message};
              alertConfig.disableClose =  false; // defaults to false
              alertConfig.viewContainerRef = this._viewContainerRef;
              alertConfig.title = 'Alert'; //OPTIONAL, hides if not provided
              alertConfig.closeButton = 'Ok';
              alertConfig.width = '400px'; //OPTIONAL, defaults to 400px
              this._dialogService.openConfirm(alertConfig).afterClosed().subscribe((accept: boolean) => {

              } );
      }

      )

      this.fhirSrv.getConformance();

  }

  swapServer(menuItem : any ) {
      this.fhirSrv.setFHIRServerBase(menuItem.route);
      this.title = menuItem.title;
      this.router.navigateByUrl('/');
      this.fhirSrv.getConformance();
  }

}
