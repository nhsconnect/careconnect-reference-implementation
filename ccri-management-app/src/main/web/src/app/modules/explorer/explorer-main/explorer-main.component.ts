import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {FhirService, Formats} from '../../../service/fhir.service';
import {IAlertConfig, TdDialogService, TdMediaService} from '@covalent/core';
import {Router} from '@angular/router';
import {MessageService} from '../../../service/message.service';
import {MatIconRegistry} from '@angular/material';
import {DomSanitizer} from '@angular/platform-browser';
import {EprService} from '../../../service/epr.service';
import {AuthService} from '../../../service/auth.service';
import {Oauth2Service} from '../../../service/oauth2.service';

@Component({
  selector: 'app-explorer-main',
  templateUrl: './explorer-main.component.html',
  styleUrls: ['./explorer-main.component.css']
})
export class ExplorerMainComponent implements OnInit {

    public outputFormat: Formats = Formats.JsonFormatted;


  routes: Object[] = [
  ];

  oauth2routes: Object[] = [
  ];

  routesExt: Object[] = [
  ];

    serverMenu: any[] = [
        {
            icon: 'swap_horiz',
            route: 'https://data.developer.nhs.uk/ccri-fhir/STU3',
            title: 'Care Connect RI'
        } ,
        {
            icon: 'swap_horiz',
            route: 'https://data.developer.nhs.uk/ccri-smartonfhir/STU3',
            title: 'Care Connect RI (Secure)'
        }
        , {
            icon: 'swap_horiz',
            route: '/ccri/camel/fhir/ods',
            title: 'FHIR ODS API'
        }
        , {
            icon: 'swap_horiz',
            route: 'https://data.developer.nhs.uk/nrls-ri',
            title: 'National Record Locator Service'
        }

/*        ,{
            icon: 'swap_horiz',
            route: '/ccri/camel/fhir/gpc',
            title: 'GP Connect'
        } */

        , {
            icon: 'swap_horiz',
            route: 'https://fhir.hl7.org.uk/STU3',
            title: 'HL7 UK FHIR Reference'
        }
        , {
            icon: 'swap_horiz',
            route: 'https://fhir.nhs.uk/STU3',
            title: 'NHS Digital FHIR Reference'
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

    title = 'Care Connect Reference Implementation';

  constructor(public media: TdMediaService,
              public fhirSrv: FhirService,
              private router: Router,
              private messageService: MessageService,
              private _dialogService: TdDialogService,
              private _viewContainerRef: ViewContainerRef,
              private matIconRegistry: MatIconRegistry,
              private domSanitizer: DomSanitizer,
              private eprService: EprService,
              public authService: AuthService,
              private oauth2: Oauth2Service) { }

  ngOnInit() {

      // Work around for local systems
    this.routes = this.eprService.routes;
    this.routesExt = this.eprService.routesExt;
    this.oauth2routes = this.eprService.oauth2routes;

      if (document.baseURI.includes('4203')) {
          for (const menu of this.serverMenu) {
              if (menu.route.includes('/ccri/camel')) {
                  menu.route = 'http://127.0.0.1:8187' + menu.route;
              }
          }
      }
      this.matIconRegistry.addSvgIcon(
          'github',
          this.domSanitizer.bypassSecurityTrustResourceUrl('assets/github.svg'));

    this.fhirSrv.getRootUrlChange().subscribe(url => {
        this.serverMenu[0] = {
            icon: 'swap_horiz',
            route: url,
            title: 'Care Connect RI'
        };
        this.fhirSrv.getConformance();
    });

    this.fhirSrv.getConformanceChange().subscribe(capabilityStatement => {
        this.navmenu = [];
        if (capabilityStatement !== undefined) {
            for (const node of capabilityStatement.rest) {
                // console.log('mode ' + node.mode);
                for (const resource of node.resource) {
                    // console.log(resource.type);
                    let count = 0;
                    if (resource.extension !== undefined) {

                        for (const extension of resource.extension) {
                            if (extension.url.endsWith('resourceCount')) {
                                count = extension.valueDecimal;

                            }
                        }
                    }
                    this.navmenu.push({
                        icon: 'looks_one',
                        route: '/' + resource.type,
                        title: resource.type,
                        count: count

                    });
                }
            }
        }
    });

    this.messageService.getMessageEvent().subscribe(
        error => {
            if (this.router.url.includes('exp')) {
                const alertConfig: IAlertConfig = {
                    message: error
                };
                alertConfig.disableClose = false; // defaults to false
                alertConfig.viewContainerRef = this._viewContainerRef;
                alertConfig.title = 'Alert'; // OPTIONAL, hides if not provided

                alertConfig.width = '400px'; // OPTIONAL, defaults to 400px
                this._dialogService.openConfirm(alertConfig).afterClosed().subscribe((accept: boolean) => {

                });
            } else {
                console.log('not my baby');
            }

        }

      );

    this.fhirSrv.getConformance();

  }

  format(format: string) {

      switch (format) {
          case 'jsonf' :
              this.outputFormat = Formats.JsonFormatted;

              break;
          case 'json' :
              this.outputFormat = Formats.Json;
              break;
          case 'xml' :
              this.outputFormat = Formats.Xml;
              break;
          case 'epr' :
              this.outputFormat = Formats.EprView;
              break;
      }
      this.fhirSrv.setOutputFormat(this.outputFormat);
  }

  swapServer(menuItem: any ) {

      console.log(menuItem);

      // let server: string = menuItem.route;
      this.fhirSrv.setFHIRServerBase(menuItem.route);
      if (menuItem.route.includes('smartonfhir') && !this.oauth2.isAuthenticated() ) {
        // force login
          console.log('Login required detected');
          this.router.navigateByUrl('/login');

      } else {
          if (menuItem.route.includes('ccrifhir')) {
              // force logoff
              this.oauth2.removeToken();
          } else {

              this.title = menuItem.title;

              console.log('Forcing naviagation to root');
              this.router.navigateByUrl('/');
              this.fhirSrv.getConformance();
          }
      }
  }

  onLogin() {
      this.authService.setBaseUrlOAuth2();
      this.router.navigateByUrl('/login');
  }
  onClick(route) {
      this.router.navigateByUrl(route);
  }

}
