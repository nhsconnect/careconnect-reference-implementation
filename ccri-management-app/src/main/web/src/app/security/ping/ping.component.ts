import { Component, OnInit } from '@angular/core';
import {AuthService} from '../../service/auth.service';
import {FhirService} from '../../service/fhir.service';
import {Router} from "@angular/router";





@Component({
  selector: 'app-ping',
  templateUrl: './ping.component.html',
  styleUrls: ['./ping.component.css']
})
export class PingComponent implements OnInit {


  constructor(private authService: AuthService,
              private router: Router,
              private  fhirService: FhirService
    ) {
  }


  ngOnInit() {
    // Perform a resource access to check access token.
    this.fhirService.get('/Patient?_id=1').subscribe( data => {
      this.router.navigate(['']);
    });

  }






}
