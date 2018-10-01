import {Component, Inject, Input, OnInit} from '@angular/core';

import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

declare var $: any;

@Component({
  selector: 'app-encounter-dialog',
  templateUrl: './encounter-dialog.component.html',
  styleUrls: ['./encounter-dialog.component.css']
})
export class EncounterDialogComponent implements OnInit {


  //https://stackoverflow.com/questions/44987260/how-to-add-jstree-to-angular-2-application-using-typescript-with-types-jstree


  constructor(
    public dialogRef: MatDialogRef<EncounterDialogComponent>,

    @Inject(MAT_DIALOG_DATA) data) {
    this.encounters = data.encounters;
    this.encounterId = data.encounterId;
    this.useBundle = data.useBundle;
  }

  @Input()
  encounters : fhir.Encounter[];

  @Input()
  encounterId : string;

  useBundle : boolean;


  ngOnInit() {



  }
 }


