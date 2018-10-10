import {Component, OnInit, ViewChild} from '@angular/core';



@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {


    routes: Object[] = [{
        icon: 'home',
        route: '.',
        title: 'ED Dashboard',
    }, {
        icon: 'library_books',
        route: '.',
        title: 'Documentation',
    }
    ];

    navmenu: Object[] = [];

    title : string ='Elmet Emergency Dashboard - Ridings Ambulance Service';



    constructor() { }

  ngOnInit() {


  }


}
