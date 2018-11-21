import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {FhirService} from "../../service/fhir.service";
import {BundleService} from "../../service/bundle.service";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {MedicationDialogComponent} from "../../dialog/medication-dialog/medication-dialog.component";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {OrganisationDialogComponent} from "../../dialog/organisation-dialog/organisation-dialog.component";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {MedicationAdministrationDataSource} from "../../data-source/medication-administration-data-source";

@Component({
  selector: 'app-medication-administration',
  templateUrl: './medication-administration.component.html',
  styleUrls: ['./medication-administration.component.css']
})
export class MedicationAdministrationComponent implements OnInit {

    @Input() medicationAdministrations : fhir.MedicationAdministration[];

    meds : fhir.Medication[] = [];

    @Output() medicationAdministration = new EventEmitter<any>();

    @Input() patientId : string;

    dataSource : MedicationAdministrationDataSource;

    displayedColumns = ['medication', 'medicationlink','dose','route','quantity', 'status','effective','practitioner','organisation', 'visit', 'resource'];

    practitioners : fhir.Practitioner[];

    organisations : fhir.Organization[];

    selectedMeds : fhir.Medication[];

    @Output() context = new EventEmitter<any>();

    constructor(private linksService : LinksService,
                private fhirService : FhirService,
                private bundleService : BundleService,
                public dialog: MatDialog) { }

    ngOnInit() {
        if (this.patientId != undefined) {
            this.dataSource = new MedicationAdministrationDataSource(this.fhirService, this.patientId, []);
        } else {
            this.dataSource = new MedicationAdministrationDataSource(this.fhirService, undefined, this.medicationAdministrations);
        }
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

    onClick(medicationAdministration : fhir.MedicationAdministration) {


        console.log("Clicked - " + medicationAdministration.id);
        this.selectedMeds = [];

        if (this.bundleService.getBundle() != undefined) {

            if (medicationAdministration.medicationReference != null) {
                console.log("medicationReference - " + medicationAdministration.medicationReference.reference);
                this.bundleService.getResource(medicationAdministration.medicationReference.reference).subscribe(
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
            let reference = medicationAdministration.medicationReference.reference;
            console.log(reference);
            let refArray: string[] = reference.split('/');
            if (refArray.length>1) {
                this.fhirService.get('/Medication/'+(refArray[refArray.length - 1])).subscribe(data => {
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

    showOrganisation(medicationAdministration : fhir.MedicationAdministration) {
        this.organisations = [];
        for (let performer of medicationAdministration.performer) {
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



    showPractitioner(medicationAdministration : fhir.MedicationAdministration) {
        this.practitioners = [];

        for (let performer of medicationAdministration.performer) {
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
    viewEncounter(request : fhir.MedicationAdministration) {
        if (request.context !== undefined) {
            this.context.emit(request.context);
        }
    }

}
