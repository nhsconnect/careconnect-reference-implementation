import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CarePlanDataSource} from "../../data-source/care-plan-data-source";
import {LinksService} from "../../service/links.service";
import {BundleService} from "../../service/bundle.service";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {FhirService} from "../../service/fhir.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";

@Component({
  selector: 'app-care-plan',
  templateUrl: './care-plan.component.html',
  styleUrls: ['./care-plan.component.css']
})
export class CarePlanComponent implements OnInit {

    @Input() carePlans : fhir.CarePlan[];

    @Output() carePlan = new EventEmitter<any>();

    @Input() patientId : string;

    @Input() useBundle :boolean = false;

    dataSource : CarePlanDataSource;

    displayedColumns = ['id', 'resource'];

    constructor(private linksService : LinksService,
                public bundleService : BundleService,
                public dialog: MatDialog,
                public fhirService : FhirService) { }

    ngOnInit() {
        if (this.patientId != undefined) {
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
        let resourceDialog : MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
    }
}
