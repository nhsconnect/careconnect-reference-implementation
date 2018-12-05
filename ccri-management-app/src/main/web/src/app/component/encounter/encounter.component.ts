import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from '../../service/links.service';
import {ResourceDialogComponent} from '../../dialog/resource-dialog/resource-dialog.component';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';

import {FhirService} from '../../service/fhir.service';
import {EncounterDataSource} from '../../data-source/encounter-data-source';
import {LocationDialogComponent} from '../../dialog/location-dialog/location-dialog.component';
import {OrganisationDialogComponent} from '../../dialog/organisation-dialog/organisation-dialog.component';
import {PractitionerDialogComponent} from '../../dialog/practitioner-dialog/practitioner-dialog.component';
import {BundleService} from '../../service/bundle.service';


@Component({
  selector: 'app-encounter',
  templateUrl: './encounter.component.html',
  styleUrls: ['./encounter.component.css']
})
export class EncounterComponent implements OnInit {

  @Input() encounters: fhir.Encounter[];

  locations: fhir.Location[];

  @Input() showDetail = false;

  @Input() patient: fhir.Patient;

  @Output() encounter = new EventEmitter<any>();

  selectedEncounter: fhir.Encounter;

  @Input() patientId: string;

  @Input() useBundle = false;

  dataSource: EncounterDataSource;

  displayedColumns = ['select', 'start', 'end', 'status', 'type', 'typelink', 'service', 'provider', 'providerLink',
    'participant', 'participantLink', 'location', 'locationLink', 'resource'];

  constructor(private linksService: LinksService,
    public bundleService: BundleService,
    public dialog: MatDialog,
    public fhirService: FhirService) { }

  ngOnInit() {
    if (this.patientId !== undefined) {
      this.dataSource = new EncounterDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new EncounterDataSource(this.fhirService, undefined, this.encounters);
    }
  }
  getCodeSystem(system: string): string {
    return this.linksService.getCodeSystem(system);
  }

  isSNOMED(system: string): boolean {
    return this.linksService.isSNOMED(system);
  }


  getSNOMEDLink(code: fhir.Coding) {
    if (this.linksService.isSNOMED(code.system)) {
      window.open(this.linksService.getSNOMEDLink(code), '_blank');
    }
  }

  showLocation(encounter: fhir.Encounter) {


    this.locations = [];

    if (this.bundleService.getBundle() !== undefined) {
        for (const reference of encounter.location) {
          console.log(reference.location.reference);
          this.bundleService.getResource(reference.location.reference).subscribe(
            (resource) => {

              if (resource !== undefined && resource.resourceType === 'Location') {
                console.log('Location ' + reference.location.reference);
                this.locations.push(<fhir.Location> resource);
              }

              const dialogConfig = new MatDialogConfig();
              dialogConfig.disableClose = true;
              dialogConfig.autoFocus = true;
              dialogConfig.data = {
                id: 1,
                locations: this.locations
              };
              const resourceDialog: MatDialogRef<LocationDialogComponent> = this.dialog.open(LocationDialogComponent, dialogConfig);
            }
          );
        }
    } else {
      let count = encounter.location.length;
      for (const reference of encounter.location) {
        console.log(reference);

        const refArray: string[] = reference.location.reference.split('/');

        if (refArray.length > 1) {
          this.fhirService.getResource('/' + reference.location.reference).subscribe(data => {
              if (data !== undefined) {
                this.locations.push(<fhir.Location>data);

              }
              count--;
              if (count === 0) {
                const dialogConfig = new MatDialogConfig();
                dialogConfig.disableClose = true;
                dialogConfig.autoFocus = true;
                dialogConfig.data = {
                  id: 1,
                  locations: this.locations
                };
                const resourceDialog: MatDialogRef<LocationDialogComponent> = this.dialog.open(LocationDialogComponent, dialogConfig);
              }
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

  showOrganisation(encounter: fhir.Encounter) {
    const organisations = [];

    this.bundleService.getResource(encounter.serviceProvider.reference).subscribe((organisation) => {

      if (organisation !== undefined && organisation.resourceType === 'Organization') {

        organisations.push(<fhir.Organization> organisation);

        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;
        // dialogConfig.width="800px";
        dialogConfig.data = {
          id: 1,
          organisations: organisations
        };
        const resourceDialog: MatDialogRef<OrganisationDialogComponent> = this.dialog.open(OrganisationDialogComponent, dialogConfig);

      }
    });
  }

  showPractitioner(encounter: fhir.Encounter) {
    const practitioners = [];

    for (const practitionerReference of encounter.participant) {
      this.bundleService.getResource(practitionerReference.individual.reference).subscribe((practitioner) => {
          if (practitioner !== undefined && practitioner.resourceType === 'Practitioner') {
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
            const resourceDialog: MatDialogRef<PractitionerDialogComponent> = this.dialog.open(PractitionerDialogComponent, dialogConfig);
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
    const resourceDialog: MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
  }

    selectEncounter(encounter: fhir.Encounter) {
        this.encounter.emit(encounter);
    }
}
