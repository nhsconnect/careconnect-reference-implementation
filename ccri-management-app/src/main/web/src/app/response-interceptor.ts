import {Injectable, Injector} from "@angular/core";
import {HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {HttpEvent, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs/internal/Observable';
import {tap} from "rxjs/operators";
import {MessageService} from "./service/message.service";


  @Injectable()
  export class ResponseInterceptor implements HttpInterceptor {

    constructor(private messageService : MessageService
    )  {
    }
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      //console.info('req.headers =', req.headers, ';');
      return next.handle(req).pipe(
        tap(event => {
          if (event instanceof HttpResponse) {

           // console.log(" all looks good");
            // http response status code
           // console.log(event.status);
          }
        }, error => {

          if (error.status == 401) {
            this.messageService.addMessage('401 UNAUTHORIZED - The request has not been applied because it lacks valid authentication credentials for the target resource.');
          } else if (error.status == 0) {
            this.messageService.addMessage('Server unavailable or Request Blocked (CORS)');
          }
          else {
            console.error('response intercept status :'+ error.status);
            console.error('response intercept message :'+ error.message);
          }

        })
      )
        ;
    }



}
