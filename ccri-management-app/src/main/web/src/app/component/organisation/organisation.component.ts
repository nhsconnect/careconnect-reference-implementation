import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";

import {OrganisationDataSource} from "../../data-source/organisation-data-source";
import {FhirService} from "../../service/fhir.service";
import {BundleService} from "../../service/bundle.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-organisation',
  templateUrl: './organisation.component.html',
  styleUrls: ['./organisation.component.css']
})
export class OrganisationComponent implements OnInit {

  @Input() organisations : fhir.Organization[];

  @Input() organisationsObservable : Observable<fhir.Organization[]>;

  @Input() showResourceLink : boolean = true;

  @Input() useObservable : boolean = false;

  @Output() organisation = new EventEmitter<any>();

  dataSource : OrganisationDataSource;

  displayedColumns = ['organisation', 'identifier', 'contact', 'resource'];

  constructor(public dialog: MatDialog,
              public fhirService : FhirService,
              public bundleService: BundleService
              ) { }

  ngOnInit() {
    if (!this.showResourceLink) {
      this.displayedColumns = ['select','organisation', 'identifier', 'contact'];
    }
    if (this.useObservable) {
      this.dataSource = new OrganisationDataSource(this.fhirService,  undefined, this.organisationsObservable, this.useObservable);
    } else {
      if (this.organisations != undefined) {
        this.dataSource =  new OrganisationDataSource(this.fhirService, this.organisations, undefined, this.useObservable);
      }
    }
  }

  getIdentifier(identifier : fhir.Identifier) : String {
    let name : String = identifier.system
    if (identifier.system.indexOf('ods-organization-code') != -1) {

      name = 'ODS Code';
    }
    return name;
  }

  selectOrganisation(organisation : fhir.Organization) {
    this.organisation.emit(organisation);
  }


  select(resource) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      id: 1,
      resource: resource
    };
    let resourceDialog : MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
  }

}
