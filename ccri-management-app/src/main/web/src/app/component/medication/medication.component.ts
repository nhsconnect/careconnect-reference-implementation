import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {MedicationDataSource} from "../../data-source/medication-data-source";
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-medication',
  templateUrl: './medication.component.html',
  styleUrls: ['./medication.component.css']
})
export class MedicationComponent implements OnInit {

  @Input() medications : fhir.Medication[];
  constructor(private linksService : LinksService,
              public dialog: MatDialog,
              public fhirService : FhirService) { }

  @Output() medication = new EventEmitter<any>();

  dataSource : MedicationDataSource;

  displayedColumns = ['medication', 'medicationlink','medicationlinkDMD', 'resource'];

  ngOnInit() {
    this.dataSource = new MedicationDataSource(this.fhirService,  this.medications);
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
