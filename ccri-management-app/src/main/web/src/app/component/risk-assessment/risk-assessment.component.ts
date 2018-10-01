import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AllergyIntoleranceDataSource} from "../../data-source/allergy-data-source";
import {RiskAssessmentDataSource} from "../../data-source/risk-assessment-data-source";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {LinksService} from "../../service/links.service";
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-risk-assessment',
  templateUrl: './risk-assessment.component.html',
  styleUrls: ['./risk-assessment.component.css']
})
export class RiskAssessmentComponent implements OnInit {

    @Input() risks : fhir.RiskAssessment[];

    @Output() risk = new EventEmitter<any>();

    @Input() patientId : string;

    dataSource : RiskAssessmentDataSource;

    displayedColumns = ['occurence', 'code','codelink', 'status', 'prediction','resource'];
  constructor(private linksService : LinksService,
              public dialog: MatDialog,
              public fhirService : FhirService) { }

  ngOnInit() {
      if (this.patientId != undefined) {
          this.dataSource = new RiskAssessmentDataSource(this.fhirService, this.patientId, []);
      } else {
          this.dataSource = new RiskAssessmentDataSource(this.fhirService, undefined, this.risks);
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
