import {CanActivate, Router} from "@angular/router";
import {AuthService} from "./auth.service";
import {Injectable} from "@angular/core";
import {FhirService} from "./fhir.service";
import {Oauth2Service} from "./oauth2.service";

@Injectable()
export class AuthGuardOauth2  implements CanActivate {


  constructor(public authService: AuthService,public router: Router, private fhirService: FhirService, private oauth2 : Oauth2Service) {

  }
  canActivate() {

    // If not SMART on FHIR or OAuth2 then quit
    console.log('guard');


    if (this.oauth2.isAuthenticated()) return true;

    console.log('Unable to activate route' );
      this.router.navigate(['login']);
    return false;
  }

}
