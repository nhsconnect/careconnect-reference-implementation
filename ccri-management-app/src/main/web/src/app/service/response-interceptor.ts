import {Injectable, Injector} from "@angular/core";
import {HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {HttpEvent, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs/internal/Observable';
import {tap} from "rxjs/operators";
import {MessageService} from "./message.service";
import {Oauth2Service} from "./oauth2.service";
import {FhirService} from "./fhir.service";
import {AuthService} from "./auth.service";


  @Injectable()
  export class ResponseInterceptor implements HttpInterceptor {

    constructor(private messageService : MessageService,
                private oauth2 : Oauth2Service,
                public fhirService: FhirService,
                public authService : AuthService
    )  {
    }
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {


      if ((request.url.indexOf(this.fhirService.getBaseUrl()) !== -1) && (request.url.indexOf('metadata') == -1 ) && this.fhirService.oauth2Required()) {
        //console.log('Does token need refreshing ' + !this.oauth2.isAuthenticated());
        if (request.method == "PUT" || request.method == "POST") {
          request = request.clone({
            setHeaders: {
              Authorization: `Bearer ${this.oauth2.getToken()}`,
              Prefer: 'return=representation'
            }
          });
        } else {
          request = request.clone({
            setHeaders: {
              Authorization: `Bearer ${this.oauth2.getToken()}`
            }
          });
        }
      }
      return next.handle(request).pipe(
        tap(event => {
          if (event instanceof HttpResponse) {

           // console.log(" all looks good");
            // http response status code
           // console.log(event.status);
          }
        }, error => {
         // console.log(request);
          if (error.status == 401) {
            this.messageService.addMessage('401 UNAUTHORIZED - The request has not been applied because it lacks valid authentication credentials for the target resource.');
          } else if (error.status == 0) {
            this.messageService.addMessage('Server unavailable or Request Blocked (CORS)');
          } else {
            console.error('response intercept status :'+ error.status);
            console.error('response intercept message :'+ error.message);
          }

        })
      )
        ;
    }

}
