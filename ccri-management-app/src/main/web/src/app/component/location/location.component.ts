import {Component, Input, OnInit} from '@angular/core';
import {LocationDataSource} from "../../data-source/location-data-source";
import {FhirService} from "../../service/fhir.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {LinksService} from "../../service/links.service";


@Component({
  selector: 'app-location',
  templateUrl: './location.component.html',
  styleUrls: ['./location.component.css']
})
export class LocationComponent implements OnInit {

  @Input() locations : fhir.Location[];

  dataSource : LocationDataSource;

  displayedColumns = ['location',  'resource'];

  constructor(public fhirService : FhirService,
              private linksService : LinksService,
              public dialog: MatDialog) { }

  ngOnInit() {
    this.dataSource = new LocationDataSource(this.fhirService,  this.locations);
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
