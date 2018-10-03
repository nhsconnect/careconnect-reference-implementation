import {Component, Input, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {FhirService} from "../../../service/fhir.service";
import {ActivatedRoute} from "@angular/router";
import {IAlertConfig, TdDialogService} from "@covalent/core";

@Component({
  selector: 'app-pdf-viewer',
  templateUrl: './pdf-viewer.component.html',
  styleUrls: ['./pdf-viewer.component.css']
})
export class PdfViewerComponent implements OnInit {

  docId : string;


  @Input() document : any;

  pdfSrc: string = '';

  page: number = 1;
  totalPages: number;
  isLoaded: boolean = false;

  @Input() binaryId : string;

  constructor(private route: ActivatedRoute,
              private fhirService : FhirService,
              private _dialogService: TdDialogService,
              private _viewContainerRef: ViewContainerRef) { }


  ngOnInit() {

    this.getDocument(this.binaryId);
  }


  getDocument(id : string): void {

    this.docId = id;

    //let modalWaitRef = this.modalService.open( this.modalWait,{ windowClass: 'dark-modal' });

    this.fhirService.getBinaryRaw(id).subscribe(
      (res) => {
        var fileURL = URL.createObjectURL(res);
        this.pdfSrc=fileURL;
      //  modalWaitRef.close();
      },
      (err) => {
        this.showWarnDlg("Unable to load document");
      }
    );

  }
  nextPage() {
    this.page++;
  }

  prevPage() {
    this.page--;
  }
  afterLoadComplete(pdfData: any) {
    this.totalPages = pdfData.numPages;
    this.isLoaded = true;
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
