import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ClinicalImpressionDataSource} from "../../data-source/clinical-impression-data-source";
import {LinksService} from "../../service/links.service";
import {BundleService} from "../../service/bundle.service";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {FhirService} from "../../service/fhir.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";

@Component({
  selector: 'app-clinical-impression',
  templateUrl: './clinical-impression.component.html',
  styleUrls: ['./clinical-impression.component.css']
})
export class ClinicalImpressionComponent implements OnInit {

    @Input() clinicalImpressions : fhir.ClinicalImpression[];

    @Output() clinicalImpression = new EventEmitter<any>();

    @Input() patientId : string;

    @Input() useBundle :boolean = false;

    dataSource : ClinicalImpressionDataSource;

    displayedColumns = ['id', 'resource'];

    constructor(private linksService : LinksService,
                public bundleService : BundleService,
                public dialog: MatDialog,
                public fhirService : FhirService) { }

    ngOnInit() {
        if (this.patientId != undefined) {
            this.dataSource = new ClinicalImpressionDataSource(this.fhirService, this.patientId, []);
        } else {
            this.dataSource = new ClinicalImpressionDataSource(this.fhirService, undefined, this.clinicalImpressions);
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
