import {ErrorHandler, EventEmitter, Injectable} from '@angular/core';
import {MessageService} from "./service/message.service";
import {HttpErrorResponse} from "@angular/common/http";


@Injectable()
export class ErrorsHandler implements ErrorHandler {

    constructor(private messageService : MessageService
    )  {
    }


    handleError(error: Error | HttpErrorResponse) {
        // Do whatever you like with the error (send it to the server?)
        // And log it to the console
       // this.errorEvent.emit(error);
        if (error instanceof HttpErrorResponse) {
            let httpErorr : HttpErrorResponse = <HttpErrorResponse> error;
            console.log('http error =  '+httpErorr.status);
            this.messageService.addMessage(error);
        } else {
            console.error('It happens: ', error);
            this.messageService.addMessage(error);
        }
    }



}
