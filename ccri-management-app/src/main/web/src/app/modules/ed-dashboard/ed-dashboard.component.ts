import {Component, OnInit, ViewChild} from '@angular/core';



@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {


    routes: Object[] = [{
        icon: 'home',
        route: '/',
        title: 'FHIR Explorer',
    }
        ,
        {
            icon: 'local_hospital',
            route: '/ed',
            title: 'ED Dashboard Demo',
        }
        ,{
            icon: 'lock',
            route: 'https://data.developer.nhs.uk/ccri-auth/',
            title: 'OAuth2 (SMART on FHIR) Server',
        }
        , {
            icon: 'file_copy',
            route: 'https://data.developer.nhs.uk/document-viewer/',
            title: 'FHIR Document Viewer',
        }
    ];

    navmenu: Object[] = [];

    title : string ='Garforth Sector - Yorkshire Ambulance Service';



    constructor() { }

  ngOnInit() {


  }


}
