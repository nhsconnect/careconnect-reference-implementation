import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ConditionDataSource} from "../../data-source/condition-data-source";
import {ProcedureDataSource} from "../../data-source/procedure-data-source";
import {FhirService} from "../../service/fhir.service";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {EncounterDialogComponent} from "../../dialog/encounter-dialog/encounter-dialog.component";
import {BundleService} from "../../service/bundle.service";

@Component({
  selector: 'app-procedure',
  templateUrl: './procedure.component.html',
  styleUrls: ['./procedure.component.css']
})
export class ProcedureComponent implements OnInit {

  @Input() procedures : fhir.Procedure[];

  @Output() procedure = new EventEmitter<any>();

  @Input() patientId : string;

  @Input() useBundle :boolean = false;

  dataSource : ProcedureDataSource;

  displayedColumns = ['performed', 'code','codelink','status', 'bodysite', 'complication','contextLink', 'resource'];

  constructor(private linksService : LinksService,
              public bundleService : BundleService,
              public dialog: MatDialog,
              public fhirService : FhirService) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new ProcedureDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new ProcedureDataSource(this.fhirService, undefined, this.procedures);
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

  showEncounter(procedure : fhir.Procedure) {
    let contexts = [];


    this.bundleService.getResource(procedure.context.reference).subscribe((encounter) => {
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
