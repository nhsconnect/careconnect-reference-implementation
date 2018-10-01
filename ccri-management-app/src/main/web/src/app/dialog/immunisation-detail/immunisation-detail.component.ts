import {Component, Inject, Input, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {OrganisationDialogComponent} from "../organisation-dialog/organisation-dialog.component";
import {BundleService} from "../../service/bundle.service";
import {PractitionerDialogComponent} from "../practitioner-dialog/practitioner-dialog.component";

@Component({
  selector: 'app-immunisation-detail',
  templateUrl: './immunisation-detail.component.html',
  styleUrls: ['./immunisation-detail.component.css']
})
export class ImmunisationDetailComponent implements OnInit {

    organisations : fhir.Organization[];

    practitioners : fhir.Practitioner[];

  constructor( public dialogRef: MatDialogRef<ImmunisationDetailComponent>,
               public bundleService : BundleService,
               public dialog: MatDialog,
               @Inject(MAT_DIALOG_DATA) data) {
    this.immunisation = data.immunisation;
  }

    @Input()
    immunisation : fhir.Immunization;

  ngOnInit() {
  }

    showOrganisation(immunisastion : fhir.Immunization) {
        this.organisations = [];

        this.bundleService.getResource(immunisastion.manufacturer.reference).subscribe((organisation) => {

            if (organisation != undefined && organisation.resourceType === "Organization") {

                this.organisations.push(<fhir.Organization> organisation);

                const dialogConfig = new MatDialogConfig();

                dialogConfig.disableClose = true;
                dialogConfig.autoFocus = true;

                dialogConfig.data = {
                    id: 1,
                    organisations: this.organisations
                };
                let resourceDialog: MatDialogRef<OrganisationDialogComponent> = this.dialog.open(OrganisationDialogComponent, dialogConfig);

            }
        });

    }
    showPractitioner(immunisation : fhir.Immunization) {
        this.practitioners = [];

        for (let practitionerReference of immunisation.practitioner) {
            this.bundleService.getResource(practitionerReference.actor.reference).subscribe((practitioner) => {
                    if (practitioner != undefined && practitioner.resourceType === "Practitioner") {
                        this.practitioners.push(<fhir.Practitioner> practitioner);

                        const dialogConfig = new MatDialogConfig();

                        dialogConfig.disableClose = true;
                        dialogConfig.autoFocus = true;

                        dialogConfig.data = {
                            id: 1,
                            practitioners : this.practitioners
                        };
                        let resourceDialog : MatDialogRef<PractitionerDialogComponent> = this.dialog.open( PractitionerDialogComponent, dialogConfig);
                    }
                }
            );
        }
    }



}
