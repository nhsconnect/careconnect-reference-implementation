import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";



@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {


    routes: Object[] = [
      {
        icon: 'search',
        route: '/',
        title: 'FHIR Explorer',
      }
      ,{
        icon: 'add_circle_outline',
        route: '/ed',
        title: 'Emergency Triage',
      }
      ,{
        icon: 'local_hospital',
        route: '/ed/caseload',
        title: 'Ambulance Caseload',
      }
      ,{
        icon: 'dashboard',
        route: '/ed/capacity',
        title: 'Emergency Planning',
      }

    ];
    routesExt : Object[] = [{
        icon: 'lock',
        route: 'https://data.developer.nhs.uk/ccri-auth/',
        title: 'OAuth2 (SMART on FHIR) Server',
    }
        , {
            icon: 'note',
            route: 'https://data.developer.nhs.uk/document-viewer/',
            title: 'FHIR Document Viewer',
        }
    ];

    navmenu: Object[] = [];

    title : string ='Garforth Sector - Yorkshire Ambulance Service';



    constructor( private router : Router) { }

  ngOnInit() {


  }

    onClick(route) {
        this.router.navigateByUrl(route);
    }


}
