import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from '../../service/links.service';
import {BundleService} from '../../service/bundle.service';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {FhirService} from '../../service/fhir.service';
import {ResourceDialogComponent} from '../../dialog/resource-dialog/resource-dialog.component';
import {FlagDataSource} from '../../data-source/flag-data-source';

@Component({
  selector: 'app-flag',
  templateUrl: './flag.component.html',
  styleUrls: ['./flag.component.css']
})
export class FlagComponent implements OnInit {

  @Input() flags: fhir.Flag[];

  @Output() flag = new EventEmitter<any>();

  @Input() patientId: string;

  @Input() useBundle = false;

  dataSource: FlagDataSource;

  displayedColumns = ['alert', 'status', 'resource'];

  constructor(private linksService: LinksService,
              public bundleService: BundleService,
              public dialog: MatDialog,
              public fhirService: FhirService) { }

  ngOnInit() {
    if (this.patientId !== undefined) {
      this.dataSource = new FlagDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new FlagDataSource(this.fhirService, undefined, this.flags);
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


}
