import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {EprService} from "../../service/epr.service";



@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {


    routes: Object[] = [
    ];

    routesExt : Object[] = [
    ];

    navmenu: Object[] = [];

    title : string ='Garforth Sector - Yorkshire Ambulance Service';



    constructor( private router : Router, private eprService : EprService) { }

  ngOnInit() {

    this.routes = this.eprService.routes;
    this.routesExt = this.eprService.routesExt;

  this.eprService.getTitleChange().subscribe( title => {
    this.title = title;
  })

  }

    onClick(route) {
        this.router.navigateByUrl(route);
    }


}
