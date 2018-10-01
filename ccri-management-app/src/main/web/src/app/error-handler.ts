import {ErrorHandler, EventEmitter, Injectable, Injector} from '@angular/core';

import {HttpErrorResponse} from "@angular/common/http";


@Injectable()
export class ErrorsHandler implements ErrorHandler {

    constructor(private injector: Injector,
    )  {
    }


    handleError(error: Error | HttpErrorResponse) {

        if (error instanceof HttpErrorResponse) {
          if (!navigator.onLine) {
            // Handle offline error
          } else {
            // Handle Http Error (error.status === 403, 404...)
            if (error.status == 401) {
              console.log('Need to refresh access token');
            } else {
              console.log('http error: '+error.status);
            }
          }
        } else {
            console.error('It happens: ', error);

        }
    }



}
