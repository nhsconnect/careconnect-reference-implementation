import {EventEmitter, Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  message : String;

  messageEvent :EventEmitter<any> = new EventEmitter();

  constructor() { }

  addMessage(message : String) {
    console.log('bang');
    this.message = message;
    this.messageEvent.emit(message);
  }

  getMessageEvent() {
    return this.messageEvent;
  }
}
