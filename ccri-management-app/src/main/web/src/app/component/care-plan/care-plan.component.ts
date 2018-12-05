import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CarePlanDataSource} from '../../data-source/care-plan-data-source';
import {LinksService} from '../../service/links.service';
import {BundleService} from '../../service/bundle.service';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {FhirService} from '../../service/fhir.service';
import {ResourceDialogComponent} from '../../dialog/resource-dialog/resource-dialog.component';
import {PractitionerDialogComponent} from '../../dialog/practitioner-dialog/practitioner-dialog.component';

@Component({
  selector: 'app-care-plan',
  templateUrl: './care-plan.component.html',
  styleUrls: ['./care-plan.component.css']
})
export class CarePlanComponent implements OnInit {

    @Input() carePlans: fhir.CarePlan[];

    @Output() carePlan = new EventEmitter<any>();

    @Input() patientId: string;

    @Input() useBundle = false;

    dataSource: CarePlanDataSource;

    displayedColumns = ['select', 'start', 'end', 'category', 'status', 'intent', 'authorLink', 'resource'];

    constructor(private linksService: LinksService,
                public bundleService: BundleService,
                public dialog: MatDialog,
                public fhirService: FhirService) { }

    ngOnInit() {
        if (this.patientId !== undefined) {
            this.dataSource = new CarePlanDataSource(this.fhirService, this.patientId, []);
        } else {
            this.dataSource = new CarePlanDataSource(this.fhirService, undefined, this.carePlans);
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

  showPractitioner(careplan: fhir.CarePlan) {
    const practitioners = [];

    for (const reference of careplan.author) {
      this.bundleService.getResource(reference.reference).subscribe((practitioner) => {
          if (practitioner !== undefined && practitioner.resourceType === 'Practitioner') {
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
            const resourceDialog: MatDialogRef<PractitionerDialogComponent> = this.dialog.open(PractitionerDialogComponent, dialogConfig);
          }
        }
      );
    }
  }

    view(carePlan: fhir.CarePlan) {
        this.carePlan.emit(carePlan);
    }
}
