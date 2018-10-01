import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";

import {PractitionerDataSource} from "../../data-source/practitioner-data-source";
import {FhirService} from "../../service/fhir.service";
import {BundleService} from "../../service/bundle.service";

import {PractitionerRoleDialogComponent} from "../../dialog/practitioner-role-dialog/practitioner-role-dialog.component";
import {Observable} from "rxjs";

@Component({
  selector: 'app-practitioner',
  templateUrl: './practitioner.component.html',
  styleUrls: ['./practitioner.component.css']
})
export class PractitionerComponent implements OnInit {

  @Input() practitioners : fhir.Practitioner[];

  @Input() practitionersObservable :Observable<fhir.Practitioner[]>;

  @Input() useObservable : boolean = false;

  @Input() useBundle : boolean = false;

  @Input() showResourceLink : boolean = true;

  roles : fhir.PractitionerRole[] = [];

  @Output() practitioner = new EventEmitter<any>();

  constructor(public dialog: MatDialog,
              public fhirService : FhirService,
              public bundleService : BundleService ) { }

  dataSource : PractitionerDataSource;

  displayedColumns = ['practitioner', 'identifier', 'contact','roles', 'resource'];

  ngOnInit() {
    console.log('Use Bundle = ' +this.useBundle);

    if (!this.showResourceLink) {
      this.displayedColumns = ['select','practitioner', 'identifier','roles', 'contact'];
    }
    if (this.useObservable) {
      this.dataSource = new PractitionerDataSource(this.fhirService,undefined, this.practitionersObservable,this.useObservable);
    } else {
      this.dataSource = new PractitionerDataSource(this.fhirService,  this.practitioners,undefined, this.useObservable);

    }
  }



  getLastName(practitioner : fhir.Practitioner) : String {
    if (practitioner == undefined) return "";
    if (practitioner.name == undefined || practitioner.name.length == 0)
      return "";

    let name = "";
    if (practitioner.name[0].family != undefined) name += practitioner.name[0].family.toUpperCase();
    return name;

  }
  getIdentifier(identifier : fhir.Identifier) : String {
    let name : String = identifier.system
    if (identifier.system == 'https://fhir.nhs.uk/Id/sds-user-id') {
      name = 'SDS User Id';
    } else {identifier.system == 'https://fhir.nhs.uk/Id/local-practitioner-identifier'} {
      name = 'Local Id';
    }
    return name;
  }
  getFirstName(practitioner : fhir.Practitioner) : String {
    if (practitioner == undefined) return "";
    if (practitioner.name == undefined || practitioner.name.length == 0)
      return "";
    // Move to address
    let name = "";
    if (practitioner.name[0].given != undefined && practitioner.name[0].given.length>0) name += ", "+ practitioner.name[0].given[0];

    if (practitioner.name[0].prefix != undefined && practitioner.name[0].prefix.length>0) name += " (" + practitioner.name[0].prefix[0] +")" ;
    return name;

  }

  selectPractitioner(practitioner : fhir.Practitioner) {
    this.practitioner.emit(practitioner);
  }

  showRoles(practitioner : fhir.Practitioner) {

   // console.log('Calling roles dialog for Practitioner '+ practitioner.id + ' useBundle = '+this.useBundle);

    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      id: 1,
      practitioner: practitioner,
      useBundle : this.useBundle
    };
    let resourceDialog: MatDialogRef<PractitionerRoleDialogComponent> = this.dialog.open(PractitionerRoleDialogComponent, dialogConfig);
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
