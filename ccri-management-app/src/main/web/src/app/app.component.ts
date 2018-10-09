import {Component, ViewContainerRef} from '@angular/core';
import {IAlertConfig, IConfirmConfig, TdDialogService, TdMediaService} from "@covalent/core";
import {FhirService, Formats} from "./service/fhir.service";
import {Router} from "@angular/router";
import {ErrorsHandler} from "./error-handler";
import {MessageService} from "./service/message.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {



  constructor(
  ) {

  }

}
