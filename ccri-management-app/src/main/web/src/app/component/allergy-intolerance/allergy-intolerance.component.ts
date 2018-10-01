import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {FhirService} from "../../service/fhir.service";
import {AllergyIntoleranceDataSource} from "../../data-source/allergy-data-source";

@Component({
  selector: 'app-allergy-intolerance',
  templateUrl: './allergy-intolerance.component.html',
  styleUrls: ['./allergy-intolerance.component.css']
})
export class AllergyIntoleranceComponent implements OnInit {

  @Input() allergies : fhir.AllergyIntolerance[];

  @Output() allergy = new EventEmitter<any>();

  @Input() patientId : string;

  dataSource : AllergyIntoleranceDataSource;

  displayedColumns = ['asserted','onset', 'code','codelink','reaction', 'clinicalstatus','verificationstatus', 'resource'];

  constructor(private linksService : LinksService,
              public dialog: MatDialog,
            public fhirService : FhirService
  ) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new AllergyIntoleranceDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new AllergyIntoleranceDataSource(this.fhirService, undefined, this.allergies);
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
