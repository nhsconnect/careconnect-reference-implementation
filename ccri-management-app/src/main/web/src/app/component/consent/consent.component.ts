import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ConsentDataSource} from "../../data-source/consent-data-source";
import {LinksService} from "../../service/links.service";
import {BundleService} from "../../service/bundle.service";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {FhirService} from "../../service/fhir.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";

@Component({
  selector: 'app-consent',
  templateUrl: './consent.component.html',
  styleUrls: ['./consent.component.css']
})
export class ConsentComponent implements OnInit {

    @Input() consents : fhir.Consent[];

    @Output() consent = new EventEmitter<any>();

    @Input() patientId : string;

    @Input() useBundle :boolean = false;

    dataSource : ConsentDataSource;

    displayedColumns = ['id', 'resource'];

    constructor(private linksService : LinksService,
                public bundleService : BundleService,
                public dialog: MatDialog,
                public fhirService : FhirService) { }

    ngOnInit() {
        if (this.patientId != undefined) {
            this.dataSource = new ConsentDataSource(this.fhirService, this.patientId, []);
        } else {
            this.dataSource = new ConsentDataSource(this.fhirService, undefined, this.consents);
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
