import {Component, Inject, Input, OnInit} from '@angular/core';

import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

declare var $: any;

@Component({
  selector: 'app-issue-dialog',
  templateUrl: './issue-dialog.component.html',
  styleUrls: ['./issue-dialog.component.css']
})
export class IssueDialogComponent implements OnInit {


  constructor(
    public dialogRef: MatDialogRef<IssueDialogComponent>,

    @Inject(MAT_DIALOG_DATA) data) {
    this.operationOutcome = data.operationOutcome;
  }

  @Input()
  operationOutcome: fhir.OperationOutcome;


  ngOnInit() {



  }
 }


