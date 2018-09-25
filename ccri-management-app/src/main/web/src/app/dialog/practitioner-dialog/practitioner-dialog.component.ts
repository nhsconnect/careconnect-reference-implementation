import {Component, Inject, Input, OnInit} from '@angular/core';

import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

declare var $: any;

@Component({
  selector: 'app-practitioner-dialog',
  templateUrl: './practitioner-dialog.component.html',
  styleUrls: ['./practitioner-dialog.component.css']
})
export class PractitionerDialogComponent implements OnInit {


  //https://stackoverflow.com/questions/44987260/how-to-add-jstree-to-angular-2-application-using-typescript-with-types-jstree


  constructor(
    public dialogRef: MatDialogRef<PractitionerDialogComponent>,

    @Inject(MAT_DIALOG_DATA) data) {
    this.practitioners = data.practitioners;
    this.practitionerId = data.practitionerId;
    this.useBundle = data.useBundle;
  }

  @Input()
  practitioners : fhir.Practitioner[];

  @Input()
  practitionerId : string;

  useBundle : boolean;


  ngOnInit() {



  }
 }


