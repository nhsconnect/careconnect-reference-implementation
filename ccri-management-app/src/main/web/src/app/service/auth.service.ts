import {EventEmitter, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {User} from "../model/user";
import {environment} from "../../environments/environment";
import {CookieService} from "ngx-cookie";
import {Oauth2Service} from "./oauth2.service";
import {FhirService} from "./fhir.service";
import {Oauth2token} from "../model/oauth2token";


@Injectable()
export class AuthService {
  set User(value: User) {
    this._User = value;
  }


  private semaphore : boolean = false;

  private _User :User = undefined;

  private UserEvent : EventEmitter<User> = new EventEmitter();

    private authoriseUri: string;

    private tokenUri: string;

    private registerUri: string;

    private smartToken : Oauth2token;

    oauthTokenChange : EventEmitter<Oauth2token> = new EventEmitter();


  public auth : boolean = false;



  constructor(
             private router: Router,
             private oauth2 : Oauth2Service,
             private _cookieService:CookieService,
             private fhirService : FhirService


              ) {

    this.updateUser();

  }


  setLocalUser(User : User) {
    if (User != undefined) console.log('User set ' + User.email + ' ' + User.userName );
    this._User = User;
    this.UserEvent.emit(this._User);
  }

  getAccessToken() {
    if (this._User == undefined) {
      this.updateUser();
    } else {
      console.log("User not undefined");
    }
    return this.oauth2.getToken();
  }




    getLogonServer() {

        let loginUrl :string = 'http://127.0.0.1:4200/document-viewer';
        // if (loginUrl.indexOf('LOGIN_') != -1) loginUrl = environment.login;
        return loginUrl;

    }

    getCookie() {

        // This should also include a check for expired cookie, return undefined if it is.
        return this._cookieService.get('ccri-token');
    }



    getUserEventEmitter() {
        return this.UserEvent;
  }

    updateUser() {


      let basicUser = new User();

      basicUser.cat_access_token = this.oauth2.getToken();

      this.setLocalUser(basicUser);
    }
    getCookieDomain() {

        let cookieDomain :string = 'CAT_COOKIE_DOMAIN';
        if (cookieDomain.indexOf('CAT_') != -1) cookieDomain = environment.oauth2.cookie_domain;
        return cookieDomain;

    }
    setCookie() {
        console.log('cookie domain: '+this.getCookieDomain());
       if (this._cookieService.get('hspc-token') !== undefined) {
           this._cookieService.put('ccri-token', this._cookieService.get('hspc-token'), {
               domain: this.getCookieDomain(),
               path: '/',

               expires: new Date((new Date()).getTime() + 3 * 60000)
           });
       } else {
           this._cookieService.put('ccri-token', localStorage.getItem('ccri-jwt'), {
               domain: this.getCookieDomain(),
               path: '/',
               expires: new Date((new Date()).getTime() + 3 * 60000)
           });
       }
    }


    authoriseOAuth2() : void  {

        console.log('authoriseOAuth2');
        this.fhirService.getResource('/metadata').subscribe(
            conformance  => {

                console.log('conformance response');

                for (let rest of conformance.rest) {
                    for (let extension of rest.security.extension) {

                        if (extension.url == "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris") {

                            for (let smartextension of extension.extension) {

                                switch (smartextension.url) {
                                    case "authorize" : {
                                        this.authoriseUri = smartextension.valueUri;
                                        break;
                                    }
                                    case "register" : {
                                        this.registerUri = smartextension.valueUri;
                                        break;
                                    }
                                    case "token" : {
                                        this.tokenUri = smartextension.valueUri;
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }

            },
            error1 => {},
            () => {

                console.log('call performAuthorise PUT ME BACK');
               // this.performAuthorise(environment.oauth2.client_id, this.getCatClientSecret());

                return this.authoriseUri;
            }
        )
    }



}
