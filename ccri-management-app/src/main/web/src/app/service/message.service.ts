import {EventEmitter, Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  message : Error;

  messageEvent :EventEmitter<any> = new EventEmitter();

  constructor() { }

  addMessage(message : Error) {
    console.log('bang');
    this.message = message;
    this.messageEvent.emit(message);
  }

  getMessageEvent() {
    return this.messageEvent;
  }
}
