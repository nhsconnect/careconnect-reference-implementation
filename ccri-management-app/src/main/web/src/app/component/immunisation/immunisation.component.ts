import {Component, Input, OnInit} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ProcedureDataSource} from "../../data-source/procedure-data-source";
import {ImmunizationDataSource} from "../../data-source/immunization-data-source";
import {FhirService} from "../../service/fhir.service";
import {OrganisationDialogComponent} from "../../dialog/organisation-dialog/organisation-dialog.component";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {BundleService} from "../../service/bundle.service";
import {ImmunisationDetailComponent} from "../../dialog/immunisation-detail/immunisation-detail.component";

@Component({
  selector: 'app-immunisation',
  templateUrl: './immunisation.component.html',
  styleUrls: ['./immunisation.component.css']
})
export class ImmunisationComponent implements OnInit {

  @Input() immunisations : fhir.Immunization[];

  @Input() patientId : string;

  dataSource : ImmunizationDataSource;

  practitioners : fhir.Practitioner[];

  organisations : fhir.Organization[];

  displayedColumns = ['procedure', 'code','codelink','indication','indicationlink','dose','status','date', 'detail', 'resource'];

  constructor(private linksService : LinksService,
              public dialog: MatDialog,
              public fhirService : FhirService,
              public bundleService : BundleService) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new ImmunizationDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new ImmunizationDataSource(this.fhirService, undefined, this.immunisations);
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

    showOrganisation(immunisastion : fhir.Immunization) {
        this.organisations = [];

        this.bundleService.getResource(immunisastion.manufacturer.reference).subscribe( (organisation) => {

            if (organisation != undefined && organisation.resourceType === "Organization") {

                this.organisations.push(<fhir.Organization> organisation);

                const dialogConfig = new MatDialogConfig();

                dialogConfig.disableClose = true;
                dialogConfig.autoFocus = true;

                dialogConfig.data = {
                    id: 1,
                    organisations : this.organisations
                };
                let resourceDialog : MatDialogRef<OrganisationDialogComponent> = this.dialog.open( OrganisationDialogComponent, dialogConfig);

            }
        });
    }


    more(immunisation : fhir.Immunization) {

        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;

        dialogConfig.data = {
            id: 1,
            immunisation: immunisation
        };
        let resourceDialog: MatDialogRef<ImmunisationDetailComponent> = this.dialog.open(ImmunisationDetailComponent, dialogConfig);

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
