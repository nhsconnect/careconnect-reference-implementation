import {Component, Inject, Input, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {OrganisationDialogComponent} from "../organisation-dialog/organisation-dialog.component";
import {PractitionerDialogComponent} from "../practitioner-dialog/practitioner-dialog.component";
import {MedicationDialogComponent} from "../medication-dialog/medication-dialog.component";
import {LinksService} from "../../service/links.service";
import {FhirService} from "../../service/fhir.service";
import {BundleService} from "../../service/bundle.service";

@Component({
  selector: 'app-medication-dispense-detail',
  templateUrl: './medication-dispense-detail.component.html',
  styleUrls: ['./medication-dispense-detail.component.css']
})
export class MedicationDispenseDetailComponent implements OnInit {

    practitioners : fhir.Practitioner[];

    organisations : fhir.Organization[];

    meds : fhir.Medication[] = [];

    selectedMeds : fhir.Medication[];

    constructor(
                 private bundleService : BundleService,
                 private linksService : LinksService,
                 private fhirService : FhirService,
                 public dialog: MatDialog,
                 @Inject(MAT_DIALOG_DATA) data) {
        this.medicationDispense = data.medicationDispense;
    }

    @Input()
    medicationDispense : fhir.MedicationDispense;

  ngOnInit() {
  }

    getCodeSystem(system : string) : string {
        return this.linksService.getCodeSystem(system);
    }

    getDMDLink(code : fhir.Coding) {
        window.open(this.linksService.getDMDLink(code), "_blank");
    }
    getSNOMEDLink(code : fhir.Coding) {
        window.open(this.linksService.getSNOMEDLink(code), "_blank");

    }
    isSNOMED(system: string) : boolean {
        return this.linksService.isSNOMED(system);
    }

    onClick(medicationDispense : fhir.MedicationDispense) {


        console.log("Clicked - " + medicationDispense.id);
        this.selectedMeds = [];

        if (this.bundleService.getBundle() != undefined) {

            if (medicationDispense.medicationReference != null) {
                console.log("medicationReference - " + medicationDispense.medicationReference.reference);
                this.bundleService.getResource(medicationDispense.medicationReference.reference).subscribe(
                    (medtemp) => {
                        if (medtemp != undefined && medtemp.resourceType === "Medication") {
                            console.log('meds list ' + medtemp.id);
                            this.selectedMeds.push(<fhir.Medication> medtemp);

                            const dialogConfig = new MatDialogConfig();

                            dialogConfig.disableClose = true;
                            dialogConfig.autoFocus = true;
                            dialogConfig.data = {
                                id: 1,
                                medications: this.selectedMeds
                            };
                            let resourceDialog: MatDialogRef<MedicationDialogComponent> = this.dialog.open(MedicationDialogComponent, dialogConfig);
                        }
                    }
                )
            }
        } else {
            let reference = medicationDispense.medicationReference.reference;
            console.log(reference);
            let refArray: string[] = reference.split('/');
            if (refArray.length>1) {
                this.fhirService.get('/Medication'+refArray[refArray.length - 1]).subscribe(data => {
                        if (data != undefined) {
                            this.meds.push(<fhir.Medication>data);
                            this.selectedMeds.push(<fhir.Medication>data);
                        }
                    },
                    error1 => {
                    },
                    () => {
                        console.log("Content = ");
                        const dialogConfig = new MatDialogConfig();

                        dialogConfig.disableClose = true;
                        dialogConfig.autoFocus = true;
                        dialogConfig.data = {
                            id: 1,
                            medications: this.selectedMeds
                        };
                        let resourceDialog : MatDialogRef<MedicationDialogComponent> = this.dialog.open( MedicationDialogComponent, dialogConfig);
                    }
                );
            }
        }
    }

    showOrganisation(medicationDispense : fhir.MedicationDispense) {
        this.organisations = [];
        for (let performer of medicationDispense.performer) {
            if (performer.onBehalfOf != undefined) {
                let organisationReference = performer.onBehalfOf;
                this.bundleService.getResource(organisationReference.reference).subscribe((organisation) => {

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
        }
    }




    showPractitioner(medicationDispense : fhir.MedicationDispense) {
        this.practitioners = [];

        for (let performer of medicationDispense.performer) {
            if (performer.actor != undefined) {
                let practitionerReference = performer.actor;
                this.bundleService.getResource(practitionerReference.reference).subscribe((practitioner) => {
                        if (practitioner != undefined && practitioner.resourceType === "Practitioner") {
                            this.practitioners.push(<fhir.Practitioner> practitioner);

                            const dialogConfig = new MatDialogConfig();

                            dialogConfig.disableClose = true;
                            dialogConfig.autoFocus = true;

                            dialogConfig.data = {
                                id: 1,
                                practitioners: this.practitioners
                            };
                            let resourceDialog: MatDialogRef<PractitionerDialogComponent> = this.dialog.open(PractitionerDialogComponent, dialogConfig);
                        }
                    }
                );
            }
        }
    }
}
