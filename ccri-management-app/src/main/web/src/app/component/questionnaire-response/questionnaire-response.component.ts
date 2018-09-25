import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {QuestionnaireResponseDataSource} from "../../data-source/form-data-source";
import {LinksService} from "../../service/links.service";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {FhirService} from "../../service/fhir.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {BundleService} from "../../service/bundle.service";

@Component({
  selector: 'app-questionnaire-response',
  templateUrl: './questionnaire-response.component.html',
  styleUrls: ['./questionnaire-response.component.css']
})
export class QuestionnaireResponseComponent implements OnInit {

    @Input() forms : fhir.QuestionnaireResponse[];

    @Input() showDetail : boolean = false;

    @Input() patientId : string;

    @Output() form = new EventEmitter<any>();

    selectedForm : fhir.QuestionnaireResponse;

    @Input() useBundle :boolean = false;

    dataSource : QuestionnaireResponseDataSource;

    displayedColumns = ['date', 'status', 'authorLink','resource'];

    constructor(private linksService : LinksService,
                // private modalService: NgbModal,
                public dialog: MatDialog,
                public bundleService : BundleService,
                public fhirService : FhirService) { }

    ngOnInit() {
        console.log('Patient id = '+this.patientId);
        if (this.patientId != undefined) {
            this.dataSource = new QuestionnaireResponseDataSource(this.fhirService, this.patientId, []);
        } else {
            this.dataSource = new QuestionnaireResponseDataSource(this.fhirService, undefined, this.forms);
        }
        console.log('calling connect');
        //this.dataSource.connect(this.patientId);
    }


    showPractitioner(form :fhir.QuestionnaireResponse) {
        let practitioners = [];


        this.bundleService.getResource(form.author.reference).subscribe((practitioner) => {
                if (practitioner != undefined && practitioner.resourceType === "Practitioner") {
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
                    let resourceDialog: MatDialogRef<PractitionerDialogComponent> = this.dialog.open(PractitionerDialogComponent, dialogConfig);
                }
            }
        );

    }


    getCodeSystem(system : string) : string {
        return this.linksService.getCodeSystem(system);
    }

    isSNOMED(system: string) : boolean {
        return this.linksService.isSNOMED(system);
    }


    getSNOMEDLink(code : fhir.Coding) {
        if (this.linksService.isSNOMED(code.system)) {
            window.open(this.linksService.getSNOMEDLink(code), "_blank");
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
