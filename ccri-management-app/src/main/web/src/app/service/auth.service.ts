import {EventEmitter, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {User} from "../model/user";
import {environment} from "../../environments/environment";
import {CookieService} from "ngx-cookie";
import {Oauth2Service} from "./oauth2.service";
import {FhirService} from "./fhir.service";
import {Oauth2token} from "../model/oauth2token";
import {EprService} from "./epr.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {AuthConfig, OAuthService} from "angular-oauth2-oidc";


@Injectable()
export class AuthService {
  set User(value: User) {
    this._User = value;
  }


  private semaphore: boolean = false;

  private _User :User = undefined;

  private UserEvent : EventEmitter<User> = new EventEmitter();

    private authoriseUri: string;

    private tokenUri: string;

    private registerUri: string;

    private smartToken : Oauth2token;

    oauthTokenChange : EventEmitter<Oauth2token> = new EventEmitter();

   public auth: boolean = false;



  constructor(
             private router: Router,
             private oauth2 : Oauth2Service,
             private oauth2service : OAuthService,
             private _cookieService:CookieService,
             private fhirService: FhirService,
             private http : HttpClient


              ) {

    this.updateUser();

  }
  getOAuthChangeEmitter() {
    return this.oauthTokenChange;
  }

  setLocalUser(User : User) {
    if (User !== undefined) console.log('User set ' + User.email + ' ' + User.userName );
    this._User = User;
    this.UserEvent.emit(this._User);
  }

  isLoggedOn(): boolean {
    return this.oauth2.isAuthenticated();
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
        if (document.baseURI.includes('data.developer-test.nhs.uk')) return 'https://data.developer-test.nhs.uk/document-viewer';
        if (document.baseURI.includes('data.developer.nhs.uk')) return 'https://data.developer.nhs.uk/document-viewer';

        let loginUrl :string = 'http://localhost:4200';
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

  getCatClientSecret() {
    // This is a marker for entryPoint.sh to replace
    let secret :string = 'SMART_OAUTH2_CLIENT_SECRET';
    if (secret.indexOf('SECRET') != -1) secret = environment.oauth2.client_secret;
    return secret;
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

  performGetAccessToken(authCode :string ) {


    let bearerToken = 'Basic '+btoa(environment.oauth2.client_id+":"+this.getCatClientSecret());
    let headers = new HttpHeaders( {'Authorization' : bearerToken});
    headers= headers.append('Content-Type','application/x-www-form-urlencoded');

    const url = localStorage.getItem("tokenUri");

    let body = new URLSearchParams();
    body.set('grant_type', 'authorization_code');
    body.set('code', authCode);
    body.set('redirect_uri',document.baseURI+'/callback');


    this.fhirService.postAny(url,body.toString(),  headers  ).subscribe( response => {
        // console.log(response);
        this.smartToken = response;
        console.log('OAuth2Token : '+response);
        this.auth = true;
        this.oauth2.setToken( this.smartToken.access_token);

        this.oauth2.setScope(this.smartToken.scope);

        this.updateUser();
      }
      , (error: any) => {
        console.log(error);
      }
      ,() => {
        // Emit event

        this.oauthTokenChange.emit(this.smartToken);

      }
    );
  }

  setBaseUrlOAuth2() {
    if (this.fhirService.getBaseUrl().includes('8183/ccri-fhir')) {
      let newbaseUrl: string = 'https://data.developer-test.nhs.uk/ccri-smartonfhir/STU3';
      console.log('swapping to smartonfhir instance: '+newbaseUrl);
      this.fhirService.setRootUrl(newbaseUrl);
    }
    else {
      if (this.fhirService.getBaseUrl().includes('ccri-fhir')) {
        let newbaseUrl: string = this.fhirService.getBaseUrl().replace('ccri-fhir','ccri-smartonfhir');
        console.log('swapping to smartonfhir instance: '+ newbaseUrl);
        this.fhirService.setRootUrl(newbaseUrl);
      }
    }

  }

  logonOAuth2() : void {

    // https://www.npmjs.com/package/angular-oauth2-oidc-codeflow

    this.oauth2service.clientId = environment.oauth2.client_id;

    this.oauth2service.tokenEndpoint = this.tokenUri;


  }

    authoriseOAuth2() : void  {

        console.log('authoriseOAuth2');
        this.setBaseUrlOAuth2();

        this.fhirService.getConformance();

        this.fhirService.getConformanceChange().subscribe( conformance => {
          for (let rest of conformance.rest) {
            if (rest.security !== undefined) {
              for (let extension of rest.security.extension) {

                if (extension.url == "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris") {

                  for (let smartextension of extension.extension) {

                    switch (smartextension.url) {
                      case "authorize" : {
                        this.authoriseUri = smartextension.valueUri;
                        localStorage.setItem("authoriseUri", this.authoriseUri);
                        break;
                      }
                      case "register" : {
                        this.registerUri = smartextension.valueUri;
                        localStorage.setItem("registerUri", this.registerUri);
                        break;
                      }
                      case "token" : {
                        this.tokenUri = smartextension.valueUri;
                          localStorage.setItem("tokenUri", this.tokenUri);

                        break;
                      }
                    }

                  }
                }
              }
            }
          }
          console.log('done conformance retrieval');
            console.log('call performAuthorise');
            this.performAuthorise(environment.oauth2.client_id, this.getCatClientSecret());
        },
          error1 => {},
          () => {
            // Check here for client id - need to store in database
            // If no registration then register client
            // Dynamic registration not present at the mo but   this.performRegister();


            return this.authoriseUri;
          });
    }




  performAuthorise(clientId: string, clientSecret :string){



    if (this.authoriseUri === undefined) localStorage.getItem("authoriseUri");
    if (this.tokenUri === undefined) localStorage.getItem("tokenUri");
    if (this.registerUri === undefined) localStorage.getItem("registerUri");


    if (this.authoriseUri !== undefined) {

        if (this.oauth2.getToken() !== undefined) {
            // access token is present so forgo access token retrieval

            this.updateUser();
            // Check token expiry
            if (!this.oauth2.isAuthenticated()) {
                const url = this.authoriseUri + '?client_id=' + clientId + '&response_type=code&redirect_uri=' + document.baseURI + '/callback&aud=https://test.careconnect.nhs.uk';
                // Perform redirect to
                window.location.href = url;
            }
            // if token is ok perform a PING (if above code is working we may remove this)
            this.router.navigateByUrl('ping');
        } else {

            const url = this.authoriseUri + '?client_id=' + clientId + '&response_type=code&redirect_uri=' + document.baseURI + '/callback&aud=https://test.careconnect.nhs.uk';
            // Perform redirect to
            window.location.href = url;
        }
    }

  }



  getClients() {

    this.setCookie();

    if (this.registerUri === undefined) {
      this.registerUri = localStorage.getItem("registerUri");
    }
    let url = this.registerUri.replace('register','');
    url = url + 'api/clients';
    console.log('url = '+url);

    let bearerToken = 'Basic '+btoa(environment.oauth2.client_id+":"+this.getCatClientSecret());

    let headers = new HttpHeaders({'Authorization': bearerToken });
    headers= headers.append('Content-Type','application/json');
    headers = headers.append('Accept','application/json');


    return this.http.get(url, {'headers' : headers }  );
  }

  launchSMART(appId: string, contextId: string, patientId: string) :Observable<any> {

    // Calls OAuth2 Server to register launch context for SMART App.

    // https://healthservices.atlassian.net/wiki/spaces/HSPC/pages/119734296/Registering+a+Launch+Context

    let bearerToken = 'Basic '+btoa(environment.oauth2.client_id+":"+this.getCatClientSecret());

    const url = localStorage.getItem("tokenUri").replace('token', '') + 'Launch';
    let payload = JSON.stringify({launch_id: contextId, parameters: []});

    let headers = new HttpHeaders({'Authorization': bearerToken });
    headers= headers.append('Content-Type','application/json');

    console.log(payload);
    return this.http.post<any>(url,"{ launch_id : '"+contextId+"', parameters : { username : 'Get Details From Keycloak', patient : '"+patientId+"' }  }", {'headers': headers});
  }

  performRegisterSMARTApp(clientName: string,
                          clientURI: string,
                          redirect: string[],
                          logo: string,
                          supplier: string
  ): Observable<any> {
    if (this.registerUri === undefined) {
      this.registerUri = localStorage.getItem("registerUri");
    }
    const url = this.registerUri;
    console.log('url = '+url);

    let bearerToken = 'Basic '+btoa(environment.oauth2.client_id+":"+this.getCatClientSecret());

    let headers = new HttpHeaders({'Authorization': bearerToken });
    headers= headers.append('Content-Type','application/json');
    headers = headers.append('Accept','application/json');

    if (supplier === undefined) {
      supplier = '';
    }


    let payload = JSON.stringify({
      client_name : clientName ,
      redirect_uris : redirect,
      client_uri : clientURI,
      grant_types: ["authorization_code"],
      scope: "user/*.read user/*.read profile launch launch/patient",
      token_endpoint_auth_method: 'none',
      logo_uri: logo,
      software_id: supplier
    });

    console.log(payload);

    return this.http.post(url,payload,{ 'headers' : headers }  );
  }
}
