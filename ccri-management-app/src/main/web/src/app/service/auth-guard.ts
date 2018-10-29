import {CanActivate, Router} from "@angular/router";
import {AuthService} from "./auth.service";
import {Injectable} from "@angular/core";

@Injectable()
export class AuthGuard  implements CanActivate {


  constructor(public authService: AuthService,public router: Router) {

  }
  canActivate() {

    if (this.authService.getCookie() !== undefined) {
      // no need to process keycloak, cookie present
      return true;
    }
    if (this.authService.getAccessToken() !== undefined) {
      return true;
    }

    console.log('Unable to activate route' );
      this.router.navigate(['login']);
    return false;
  }

}
