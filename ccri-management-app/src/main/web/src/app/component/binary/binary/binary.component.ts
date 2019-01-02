import {Component, OnInit, ViewContainerRef} from '@angular/core';
import {EprService} from '../../../service/epr.service';
import {FhirService} from '../../../service/fhir.service';
import {IAlertConfig, TdDialogService} from '@covalent/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-binary',
  templateUrl: './binary.component.html',
  styleUrls: ['./binary.component.css']
})
export class BinaryComponent implements OnInit {



  private document: fhir.DocumentReference;

  public docType: string;

  public binaryId: string;

  private documentReferenceId: string;


  constructor(public patientEprService: EprService,
              private fhirService: FhirService,
              private _dialogService: TdDialogService,
              private _viewContainerRef: ViewContainerRef,
              private route: ActivatedRoute) { }

  ngOnInit() {

     this.documentReferenceId = this.route.snapshot.paramMap.get('docid');


     if (this.documentReferenceId !== undefined) {
         if (this.documentReferenceId.length > 8 && this.patientEprService.documentReference !== undefined ) {
              // do magic

             this.document = this.patientEprService.documentReference;
             this.processDocument();



         } else {
             this.fhirService.getResource('/DocumentReference/' + this.documentReferenceId).subscribe(resource => {
                     this.document = <fhir.DocumentReference> resource;
                     this.processDocument();

                     if ((this.patientEprService.patient === undefined) ||
                       (this.document.subject !== undefined &&
                         ('Patient/' + this.patientEprService.patient.id) !== this.document.subject.reference)) {
                         this.fhirService.getResource('/' + this.document.subject.reference).subscribe(patient => {
                             this.patientEprService.set(<fhir.Patient> patient);
                         });
                     }
                 },
                 () => {
                     const alertConfig: IAlertConfig = {message: 'Unable to locate document.'};
                     alertConfig.disableClose = false; // defaults to false
                     alertConfig.viewContainerRef = this._viewContainerRef;
                     alertConfig.title = 'Alert'; // OPTIONAL, hides if not provided
                     alertConfig.closeButton = 'Close'; // OPTIONAL, defaults to 'CLOSE'
                     alertConfig.width = '400px'; // OPTIONAL, defaults to 400px
                     this._dialogService.openAlert(alertConfig);
                 });
         }
     }

  }

  processDocument() {
      const array: string[] = this.document.content[0].attachment.url.split('/');
      this.binaryId = array[array.length - 1];

      if (this.binaryId !== undefined) {
          if (this.document.content[0].attachment.contentType === 'application/fhir+xml') {
              this.docType = 'fhir';
          } else if (this.document.content[0].attachment.contentType === 'application/pdf') {
              this.docType = 'pdf';
          } else if (this.document.content[0].attachment.contentType.indexOf('image') !== -1) {
              this.docType = 'img';
          } else if (this.document.content[0].attachment.contentType.indexOf('html') !== -1) {
            this.docType = 'html';
          }
      }
      console.log('DocumentRef Id = ' + this.binaryId);
  }
}
