import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {GoalDataSource} from "../../data-source/goal-data-source";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {LinksService} from "../../service/links.service";
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-goal',
  templateUrl: './goal.component.html',
  styleUrls: ['./goal.component.css']
})
export class GoalComponent implements OnInit {

    @Input() goals : fhir.Goal[];

    @Output() goal = new EventEmitter<any>();

    @Input() patientId : string;

    dataSource : GoalDataSource;

    displayedColumns = ['asserted','onset', 'code','codelink','reaction', 'clinicalstatus','verificationstatus', 'resource'];

  constructor(private linksService : LinksService,
              public dialog: MatDialog,
              public fhirService : FhirService) { }

  ngOnInit() {
      if (this.patientId != undefined) {
          this.dataSource = new GoalDataSource(this.fhirService, this.patientId, []);
      } else {
          this.dataSource = new GoalDataSource(this.fhirService, undefined, this.goals);
      }
  }
    getCodeSystem(system : string) : string {
        return this.linksService.getCodeSystem(system);
    }

    getSNOMEDLink(code : fhir.Coding) {
        if (this.linksService.isSNOMED(code.system)) {
            window.open(this.linksService.getSNOMEDLink(code), "_blank");
        }
    }

    isSNOMED(system: string) : boolean {
        return this.linksService.isSNOMED(system);
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
