import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PractitionerRoleDataSource} from "../../data-source/practitioner-role-data-source";
import {FhirService} from "../../service/fhir.service";
import {BundleService} from "../../service/bundle.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {OrganisationDialogComponent} from "../../dialog/organisation-dialog/organisation-dialog.component";

@Component({
  selector: 'app-practitioner-role',
  templateUrl: './practitioner-role.component.html',
  styleUrls: ['./practitioner-role.component.css']
})
export class PractitionerRoleComponent implements OnInit {

  @Input() practitioner : fhir.Practitioner;

  @Input() useBundle : boolean = false ;

  @Input() roles : fhir.PractitionerRole[];

  @Output() practitionerRole = new EventEmitter<any>();
  constructor(
    public fhirService : FhirService,
    public bundleService : BundleService,
    public dialog: MatDialog
  ) { }


  dataSource : PractitionerRoleDataSource;

  displayedColumns = ['role', 'specialty', 'organisation','organisationLink','resource'];

  ngOnInit() {
    this.dataSource = new PractitionerRoleDataSource(this.fhirService, this.bundleService, this.practitioner, this.useBundle);
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


  showOrganisation(role : fhir.PractitionerRole) {
    let organisations = [];

    this.bundleService.getResource(role.organization.reference).subscribe( (organisation) => {

      if (organisation != undefined && organisation.resourceType === "Organization") {

        organisations.push(<fhir.Organization> organisation);

        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;

        dialogConfig.data = {
          id: 1,
          organisations : organisations
        };
        let resourceDialog : MatDialogRef<OrganisationDialogComponent> = this.dialog.open( OrganisationDialogComponent, dialogConfig);

      }
    });
  }

}
