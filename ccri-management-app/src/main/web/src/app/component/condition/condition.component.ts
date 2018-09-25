import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ConditionDataSource} from "../../data-source/condition-data-source";
import {FhirService} from "../../service/fhir.service";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {BundleService} from "../../service/bundle.service";
import {EncounterDialogComponent} from "../../dialog/encounter-dialog/encounter-dialog.component";

@Component({
  selector: 'app-condition',
  templateUrl: './condition.component.html',
  styleUrls: ['./condition.component.css']
})
export class ConditionComponent implements OnInit {

  @Input() conditions : fhir.Condition[];

  @Output() condition = new EventEmitter<any>();

  @Input() patientId : string;

  @Input() useBundle :boolean = false;

  dataSource : ConditionDataSource;

  displayedColumns = ['asserted','onset', 'code','codelink','category','categorylink', 'clinicalstatus','verificationstatus','asserterLink','contextLink', 'resource'];

  constructor(private linksService : LinksService,
              public bundleService : BundleService,
              public dialog: MatDialog,
              public fhirService : FhirService) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new ConditionDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new ConditionDataSource(this.fhirService, undefined, this.conditions);
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

  showPractitioner(condition :fhir.Condition) {
    let practitioners = [];


      this.bundleService.getResource(condition.asserter.reference).subscribe((practitioner) => {
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

  showEncounter(condition : fhir.Condition) {

    let contexts = [];

    this.bundleService.getResource(condition.context.reference).subscribe((encounter) => {
        if (encounter != undefined && encounter.resourceType === "Encounter") {
          contexts.push(<fhir.Encounter> encounter);

          const dialogConfig = new MatDialogConfig();

          dialogConfig.disableClose = true;
          dialogConfig.autoFocus = true;
          // dialogConfig.width="800px";
          dialogConfig.data = {
            id: 1,
            encounters : contexts,
            useBundle : this.useBundle
          };
          let resourceDialog: MatDialogRef<EncounterDialogComponent> = this.dialog.open(EncounterDialogComponent, dialogConfig);
        }
      }
    );

  }


}
