import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from '../../service/fhir.service';
import {AuthService} from "../../service/auth.service";
import {Oauth2Service} from "../../service/oauth2.service";

@Component({
  selector: 'app-callback',
  templateUrl: './callback.component.html',
  styleUrls: ['./callback.component.css']
})
export class CallbackComponent implements OnInit {

  private authCode :string ;

  subOAuth2 : any;

  subPermission : any;

  constructor(private activatedRoute: ActivatedRoute
    ,private  auth : AuthService
    ,private router: Router) { }

  ngOnInit() {
    this.authCode = this.activatedRoute.snapshot.queryParams['code'];
    this.auth.setBaseUrlOAuth2();

    if (this.authCode !==undefined) {

      this.subOAuth2 = this.auth.getOAuthChangeEmitter()
        .subscribe(item => {
          console.log('Callback Access Token callback ran');
          this.router.navigateByUrl('').then( ()=> {
            //console.log('Navigate by Url');
          });
          // Potentially a loop but need to record the access token

        });

      this.auth.performGetAccessToken(this.authCode);
    }
  }

}
