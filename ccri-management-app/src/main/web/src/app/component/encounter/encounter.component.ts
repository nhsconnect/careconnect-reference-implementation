import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";

import {FhirService} from "../../service/fhir.service";
import {EncounterDataSource} from "../../data-source/encounter-data-source";
import {LocationDialogComponent} from "../../dialog/location-dialog/location-dialog.component";
import {OrganisationDialogComponent} from "../../dialog/organisation-dialog/organisation-dialog.component";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {BundleService} from "../../service/bundle.service";
import {MedicationDialogComponent} from "../../dialog/medication-dialog/medication-dialog.component";

@Component({
  selector: 'app-encounter',
  templateUrl: './encounter.component.html',
  styleUrls: ['./encounter.component.css']
})
export class EncounterComponent implements OnInit {

  @Input() encounters : fhir.Encounter[];

  locations : fhir.Location[];

  @Input() showDetail : boolean = false;

  @Input() patient : fhir.Patient;

  @Output() encounter = new EventEmitter<any>();

  selectedEncounter : fhir.Encounter;

  @Input() patientId : string;

  @Input() useBundle :boolean = false;

  dataSource : EncounterDataSource;

  displayedColumns = ['start','end', 'type','typelink','provider','providerLink','participant','participantLink', 'locationLink','resource'];

  constructor(private linksService : LinksService,
    public bundleService : BundleService,
    public dialog: MatDialog,
    public fhirService : FhirService) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new EncounterDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new EncounterDataSource(this.fhirService, undefined, this.encounters);
    }
  }
  getCodeSystem(system : string) : string {
    return this.linksService.getCodeSystem(system);
  }

  isSNOMED(system: string) : boolean {
    return this.linksService.isSNOMED(system);
  }


  getSNOMEDLink(code : fhir.Coding) {
    if (this.linksService.isSNOMED(code.system)) {
      window.open(this.linksService.getSNOMEDLink(code), "_blank");
    }
  }

  showLocation(encounter : fhir.Encounter) {


    this.locations = [];

    if (this.bundleService.getBundle() != undefined) {
        for (let reference of encounter.location) {
          console.log(reference.location.reference);
          this.bundleService.getResource(reference.location.reference).subscribe(
            (resource) => {

              if (resource != undefined && resource.resourceType === "Location") {
                console.log("Location " + reference.location.reference);
                this.locations.push(<fhir.Location> resource);
              }

              const dialogConfig = new MatDialogConfig();
              dialogConfig.disableClose = true;
              dialogConfig.autoFocus = true;
              dialogConfig.data = {
                id: 1,
                locations: this.locations
              };
              let resourceDialog: MatDialogRef<LocationDialogComponent> = this.dialog.open(LocationDialogComponent, dialogConfig);
            }
          );
        }
    } else {
      for (let reference of encounter.location) {
        console.log(reference);
        let refArray: string[] = reference.location.reference.split('/');
        if (refArray.length>1) {
          this.fhirService.getResource('/Encounter/'+reference.location.reference).subscribe(data => {
              if (data != undefined) {
                this.locations.push(<fhir.Location>data);

              }
              const dialogConfig = new MatDialogConfig();
              dialogConfig.disableClose = true;
              dialogConfig.autoFocus = true;
              dialogConfig.data = {
                id: 1,
                locations: this.locations
              };
              let resourceDialog: MatDialogRef<LocationDialogComponent> = this.dialog.open(LocationDialogComponent, dialogConfig);
            },
            error1 => {
            },
            () => {

            }
          );
        }
      }
    }

  }

  showOrganisation(encounter : fhir.Encounter) {
    let organisations = [];

    this.bundleService.getResource(encounter.serviceProvider.reference).subscribe((organisation) => {

      if (organisation != undefined && organisation.resourceType === "Organization") {

        organisations.push(<fhir.Organization> organisation);

        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;
        // dialogConfig.width="800px";
        dialogConfig.data = {
          id: 1,
          organisations: organisations
        };
        let resourceDialog: MatDialogRef<OrganisationDialogComponent> = this.dialog.open(OrganisationDialogComponent, dialogConfig);

      }
    });
  }

  showPractitioner(encounter :fhir.Encounter) {
    let practitioners = [];

    for (let practitionerReference of encounter.participant) {
      this.bundleService.getResource(practitionerReference.individual.reference).subscribe((practitioner) => {
          if (practitioner != undefined && practitioner.resourceType === "Practitioner") {
            practitioners.push(<fhir.Practitioner> practitioner);

            const dialogConfig = new MatDialogConfig();

            dialogConfig.disableClose = true;
            dialogConfig.autoFocus = true;
            // dialogConfig.width="800px";
            dialogConfig.data = {
              id: 1,
              practitioners: practitioners,
              useBundle : this.useBundle
            };
            let resourceDialog: MatDialogRef<PractitionerDialogComponent> = this.dialog.open(PractitionerDialogComponent, dialogConfig);
          }
        }
      );
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
}
