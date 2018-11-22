import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";
import {MatDialog} from '@angular/material';
import {IAlertConfig, TdDialogService} from "@covalent/core";

@Component({
  selector: 'app-patient-documents',
  templateUrl: './patient-documents.component.html',
  styleUrls: ['./patient-documents.component.css']
})
export class PatientDocumentsComponent implements OnInit {

    documents: fhir.DocumentReference[] = [];

    resource: fhir.Bundle;

    constructor(private router : Router,
                private fhirSrv: FhirService,
                private route: ActivatedRoute,
                private eprService : EprService,
                public dialog: MatDialog,
                private _dialogService: TdDialogService,
                private _viewContainerRef: ViewContainerRef
    ) { }

    ngOnInit() {
        let patientid = this.route.snapshot.paramMap.get('patientid');

        this.fhirSrv.get('/DocumentReference?patient='+patientid).subscribe(
            bundle => {
                this.resource = bundle;
                this.getResources();

            }
        );
    }

    clearDown() {
        this.documents = [];
    }

    getResources() {
        let bundle = this.resource;
        if (bundle.entry !== undefined) {
            for (let entry of bundle.entry) {

                switch (entry.resource.resourceType) {

                    case 'DocumentReference':
                        let document: fhir.DocumentReference = <fhir.DocumentReference> entry.resource;
                        this.documents.push(document);
                        break;
                }

            }
        }

    }

    onMore(linkUrl: string) {

        this.clearDown();
        this.fhirSrv.getResults(linkUrl).subscribe(bundle => {

                this.resource = bundle;
                this.getResources();

            },
            () => {
                //this.progressBar = false;
            })
    }

    selectResource(document: fhir.DocumentReference) {

      if (document.content !== undefined && document.content.length> 0) {

        this.eprService.setDocumentReference(document);

        this.router.navigate([document.id], {relativeTo: this.route });

      } else {
        let alertConfig : IAlertConfig = { message : 'Unable to locate document.'};
        alertConfig.disableClose =  false; // defaults to false
        alertConfig.viewContainerRef = this._viewContainerRef;
        alertConfig.title = 'Alert'; //OPTIONAL, hides if not provided
        alertConfig.closeButton = 'Close'; //OPTIONAL, defaults to 'CLOSE'
        alertConfig.width = '400px'; //OPTIONAL, defaults to 400px
        this._dialogService.openAlert(alertConfig);
      }

    }
}
