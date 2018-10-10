import {Component, Input, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ResourceDialogComponent} from "../../../dialog/resource-dialog/resource-dialog.component";
import {IAlertConfig, TdDialogService} from "@covalent/core";
import {BundleService} from "../../../service/bundle.service";

@Component({
  selector: 'app-view-document',
  templateUrl: './view-document.component.html',
  styleUrls: ['./view-document.component.css']
})
export class ViewDocumentComponent implements OnInit {

  @Input() document : fhir.Bundle;
  @Input() systemType : string;
  @Input() binaryId : string;

  composition : fhir.Composition = undefined;
  patient : fhir.Patient = undefined;
  encounter : fhir.Encounter = undefined;
  sections : fhir.CompositionSection[] = [];
  resource : any = undefined;
  docId : string;

  constructor(private route: ActivatedRoute,
            private fhirService : FhirService,
            public dialog: MatDialog,
            public bundleService : BundleService,
              private _dialogService: TdDialogService,
              private _viewContainerRef: ViewContainerRef) { }

  ngOnInit() {


    if (this.systemType != "EPR") {
      this.systemType="FDMS";
      let id = this.binaryId;
      this.getDocument(id);
    } else {

      if (this.document!=null) {
        this.getComposition();
      }
    }
  }




  getDocument(id : string): void {

    this.docId = this.binaryId;

  //  let modalWaitRef = this.modalService.open( this.modalWait,{ windowClass: 'dark-modal' });

    this.fhirService.getBinary(id).subscribe( document => {
      let binary : fhir.Binary = document;
      //console.log(atob(binary.content));
      this.document = JSON.parse(atob(binary.content));
      this.bundleService.setBundle(this.document);
    }, err=>{

        this.showWarnDlg("Unable to load document");

      },
      ()=> {

        this.getComposition();
      //  modalWaitRef.close();
      }

      );
  }

  getComposition() {
    for (let entry of this.document.entry) {
      if (entry.resource.resourceType === "Composition") {
        this.composition = <fhir.Composition>entry.resource;
        for (let section of this.composition.section) {
          this.sections.push(section);
        }
      } else if (entry.resource.resourceType === "Patient") {
        this.patient = <fhir.Patient> entry.resource;
      }
    }
    if (this.composition != undefined && this.composition.encounter != undefined) {
      console.log('Encounter '+this.composition.encounter.reference);
      for (let entry of this.document.entry) {
        if (entry.resource.resourceType === "Encounter") {
         // console.log('Doc Ent = '+entry.fullUrl);
            if (entry.fullUrl !== undefined) {
                if (entry.fullUrl.indexOf(this.composition.encounter.reference) != -1)
                    this.encounter = <fhir.Encounter>entry.resource;
            }
        }
      }
    }
  }

  openResource(resource ){

    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      id: 1,
      resource: resource
    };
    let resourceDialog : MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
    //resourceDialog.componentInstance.resource = this.resource;

  }



  downloadPDFActual(documentid : string) {
    let thefile = {};
    this.fhirService.getCompositionDocumentPDF(documentid)
      .subscribe(data => {

          thefile = new Blob([data], {type: "application/pdf"});
        },
        error => {
          console.log("Error downloading the file." ,error);
        },
        () => {

          let a = window.document.createElement("a");
          a.href = window.URL.createObjectURL(thefile);
          a.download = "composition.pdf";
          document.body.appendChild(a);
          a.click();  // IE: "Access is denied"; see: https://connect.microsoft.com/IE/feedback/details/797361/ie-10-treats-blob-url-as-cross-origin-and-denies-access
          document.body.removeChild(a);
        }
      );
  }

  downloadPDF() {
    console.log("Download PDF "+ this.systemType);
    this.downloadPDFActual(this.docId);

  }

  downloadHTML() {
    console.log("Download HTML");
    this.downloadHTMLActual(this.docId);

  }

  downloadHTMLActual(docuemntid : string) {
    console.log("Download HTML");

    let thefile = {};
    this.fhirService.getCompositionDocumentHTML(docuemntid)
      .subscribe(data => {

        thefile = new Blob([data], { type: "application/octet-stream" });
      },
        error => console.log("Error downloading the file.", error),
        () => {

          let a = window.document.createElement("a");
          a.href = window.URL.createObjectURL(thefile);
          a.download = "composition.html";
          document.body.appendChild(a);
          a.click();  // IE: "Access is denied"; see: https://connect.microsoft.com/IE/feedback/details/797361/ie-10-treats-blob-url-as-cross-origin-and-denies-access
          document.body.removeChild(a);
        }
        );



  }

  showWarnDlg(message : string) {
    let alertConfig : IAlertConfig = { message : message};
    alertConfig.disableClose =  false; // defaults to false
    alertConfig.viewContainerRef = this._viewContainerRef;
    alertConfig.title = 'Warning'; //OPTIONAL, hides if not provided
    alertConfig.closeButton = 'Ok'; //OPTIONAL, defaults to 'CLOSE'
    alertConfig.width = '400px'; //OPTIONAL, defaults to 400px
    this._dialogService.openAlert(alertConfig);
  }

}
