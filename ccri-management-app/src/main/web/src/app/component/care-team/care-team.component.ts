import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CareTeamDataSource} from '../../data-source/care-team-data-source';
import {LinksService} from '../../service/links.service';
import {BundleService} from '../../service/bundle.service';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {FhirService} from '../../service/fhir.service';
import {ResourceDialogComponent} from '../../dialog/resource-dialog/resource-dialog.component';
import {PractitionerDialogComponent} from '../../dialog/practitioner-dialog/practitioner-dialog.component';

@Component({
  selector: 'app-care-team',
  templateUrl: './care-team.component.html',
  styleUrls: ['./care-team.component.css']
})
export class CareTeamComponent implements OnInit {

  @Input() careTeams: fhir.CareTeam[];

  @Output() careTeam = new EventEmitter<any>();

  @Input() patientId: string;

  @Input() useBundle = false;

  dataSource: CareTeamDataSource;

  displayedColumns = [ 'name', 'notes', 'members', 'authorLink', 'status', 'resource'];

  constructor(private linksService: LinksService,
              public bundleService: BundleService,
              public dialog: MatDialog,
              public fhirService: FhirService) { }

  ngOnInit() {
    if (this.patientId !== undefined) {
      this.dataSource = new CareTeamDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new CareTeamDataSource(this.fhirService, undefined, this.careTeams);
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

  showPractitioner(reference: fhir.Reference) {
    const practitioners = [];
    if (reference !== undefined) {
        this.bundleService.getResource(reference.reference).subscribe((practitioner) => {
            if (practitioner !== undefined && practitioner.resourceType === 'Practitioner') {
                practitioners.push(<fhir.Practitioner>practitioner);

                const dialogConfig = new MatDialogConfig();

                dialogConfig.disableClose = true;
                dialogConfig.autoFocus = true;
                // dialogConfig.width="800px";
                dialogConfig.data = {
                    id: 1,
                    practitioners: practitioners,
                    useBundle: this.useBundle
                };
                const resourceDialog: MatDialogRef<PractitionerDialogComponent> =
                    this.dialog.open(PractitionerDialogComponent, dialogConfig);
            }
        });
    }
  }

  view(careTeam: fhir.CareTeam) {
    this.careTeam.emit(careTeam);
  }
}
