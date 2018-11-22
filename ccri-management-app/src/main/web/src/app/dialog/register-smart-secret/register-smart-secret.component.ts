import {Component, Inject, Input, OnInit, ViewContainerRef} from '@angular/core';
import integer = fhir.integer;
import {EprService} from "../../service/epr.service";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {FhirService} from '../../service/fhir.service';
import {IAlertConfig, TdDialogService} from "@covalent/core";

declare var $: any;

@Component({
  selector: 'app-register-smart-secret',
  templateUrl: './register-smart-secret.component.html',
  styleUrls: ['./register-smart-secret.component.css']
})
export class RegisterSmartSecretComponent implements OnInit {



  constructor(
    public dialogRef: MatDialogRef<RegisterSmartSecretComponent>,
    @Inject(MAT_DIALOG_DATA) data) {
    this.response = data.response;
}

  @Input()
  response;

    close() {
        this.dialogRef.close();
    }
  ngOnInit() {
  }

 }


