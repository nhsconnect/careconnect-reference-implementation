import {Component, Input, OnInit, ViewContainerRef} from '@angular/core';
import {IAlertConfig, TdDialogService} from '@covalent/core';
import {FhirService} from '../../../service/fhir.service';

@Component({
  selector: 'app-html-viewer',
  templateUrl: './html-viewer.component.html',
  styleUrls: ['./html-viewer.component.css']
})
export class HtmlViewerComponent implements OnInit {

  @Input() binaryId: string;

  htmlSrc: string;

  constructor( private fhirService: FhirService,
               private _dialogService: TdDialogService,
               private _viewContainerRef: ViewContainerRef) { }

  ngOnInit() {
    this.getDocument(this.binaryId);
  }
  getDocument(id: string): void {

    // let modalWaitRef = this.modalService.open( this.modalWait,{ windowClass: 'dark-modal' });

    this.fhirService.getBinary(id).subscribe(
      (res) => {
        console.log(res);
        this.htmlSrc = atob(res.content);
      },
      (err) => {
        this.showWarnDlg('Unable to load document');
      }

    );

  }

  showWarnDlg(message: string) {
    const alertConfig: IAlertConfig = { message : message};
    alertConfig.disableClose =  false; // defaults to false
    alertConfig.viewContainerRef = this._viewContainerRef;
    alertConfig.title = 'Warning'; // OPTIONAL, hides if not provided
    alertConfig.closeButton = 'Ok'; // OPTIONAL, defaults to 'CLOSE'
    alertConfig.width = '400px'; // OPTIONAL, defaults to 400px
    this._dialogService.openAlert(alertConfig);
  }
}
