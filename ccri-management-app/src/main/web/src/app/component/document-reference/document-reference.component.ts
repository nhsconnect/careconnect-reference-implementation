import {Component, Input, OnInit, ViewContainerRef} from '@angular/core';
import {Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {IAlertConfig, TdDialogService} from "@covalent/core";
import {DocumentReferenceDataSource} from "../../data-source/document-reference-data-source";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {LinksService} from "../../service/links.service";
import {BundleService} from "../../service/bundle.service";
import {OrganisationDialogComponent} from "../../dialog/organisation-dialog/organisation-dialog.component";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {EprService} from "../../service/epr.service";

@Component({
  selector: 'app-document-reference',
  templateUrl: './document-reference.component.html',
  styleUrls: ['./document-reference.component.css']
})
export class DocumentReferenceComponent implements OnInit {

  @Input() documents :fhir.DocumentReference[];

  @Input() documentsTotal :number;

  @Input() patientId : string;

  practitioners : fhir.Practitioner[];

  organisations : fhir.Organization[];

  dataSource : DocumentReferenceDataSource;

  displayedColumns = ['open', 'created','type','typelink', 'author','authorLink', 'custodian', 'custodianLink', 'mime', 'status', 'resource'];

  constructor(private router: Router,
              private _dialogService: TdDialogService,
              private _viewContainerRef: ViewContainerRef,
              public fhirService : FhirService,
              private patientEprService : EprService,
              private linksService : LinksService,
              public dialog: MatDialog,
              public bundleService : BundleService) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new DocumentReferenceDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new DocumentReferenceDataSource(this.fhirService, undefined, this.documents);
    }
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

  selectDocument(document : fhir.DocumentReference) {
   // console.log("DocumentRef clicked = " + document.id);
    if (document.content != undefined && document.content.length> 0) {

      this.patientEprService.setDocumentReference(document);
      this.patientEprService.setSection('binary');

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

  showCustodian(document) {

    this.organisations = [];

    this.bundleService.getResource(document.custodian.reference).subscribe( (organisation) => {

      if (organisation != undefined && organisation.resourceType === "Organization") {

        this.organisations.push(<fhir.Organization> organisation);

        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;
       // dialogConfig.width="800px";
        dialogConfig.data = {
          id: 1,
          organisations : this.organisations
        };
        let resourceDialog : MatDialogRef<OrganisationDialogComponent> = this.dialog.open( OrganisationDialogComponent, dialogConfig);

      }
    });

  }

  showAuthors(document : fhir.DocumentReference) {

    this.practitioners = [];

    for (let practitionerReference of document.author) {
      this.bundleService.getResource(practitionerReference.reference).subscribe((practitioner) => {
          if (practitioner != undefined && practitioner.resourceType === "Practitioner") {
            this.practitioners.push(<fhir.Practitioner> practitioner);

            const dialogConfig = new MatDialogConfig();

            dialogConfig.disableClose = true;
            dialogConfig.autoFocus = true;
           // dialogConfig.width="800px";
            dialogConfig.data = {
              id: 1,
              practitioners : this.practitioners
            };
            let resourceDialog : MatDialogRef<PractitionerDialogComponent> = this.dialog.open( PractitionerDialogComponent, dialogConfig);
          }
        }
      );
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
