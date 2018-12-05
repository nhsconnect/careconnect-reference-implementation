import {Component, Inject, Input, OnInit} from '@angular/core';
import integer = fhir.integer;
import {EprService} from "../../service/epr.service";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

declare var $: any;

@Component({
  selector: 'app-resource-viewer',
  templateUrl: './resource-dialog.component.html',
  styleUrls: ['./resource-dialog.component.css']
})
export class ResourceDialogComponent implements OnInit {


  //https://stackoverflow.com/questions/44987260/how-to-add-jstree-to-angular-2-application-using-typescript-with-types-jstree


  constructor(
    public dialogRef: MatDialogRef<ResourceDialogComponent>,
    public patientEPRService : EprService,
    @Inject(MAT_DIALOG_DATA) data) {
    this.resource = data.resource;
  }

  @Input()
  resource = undefined;


  ngOnInit() {
    console.log('Init Called TREE');

    this.patientEPRService.getResourceChangeEvent().subscribe(
      resource => {
        this.resource = resource;
        /*
        this.buildNodes();

        $('#docTreeView').jstree('destroy');

        $('#docTreeView').jstree({
          'core' : {
            'data' : this.treeData
          }
        });
        */
      }
    )

  }







  entityMap = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': '&quot;',
    "'": '&#39;',
    "/": '&#x2F;'
  };

  escapeHtml(source: string) {
    return String(source).replace(/[&<>"'\/]/g, s => this.entityMap[s]);
  }


 }


