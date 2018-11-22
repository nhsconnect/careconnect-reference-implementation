import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from  '../../service/links.service';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {BundleService} from '../../service/bundle.service';
import {FhirService} from '../../service/fhir.service';
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {ReferralRequestDataSource} from "../../data-source/referral-request-data-source";

@Component({
  selector: 'app-referral-request',
  templateUrl: './referral-request.component.html',
  styleUrls: ['./referral-request.component.css']
})
export class ReferralRequestComponent implements OnInit {

    @Input() referrals: fhir.ReferralRequest[];

    @Input() showDetail: boolean = false;

    @Input() patientId: string;

    @Output() referral = new EventEmitter<any>();

    selectedReferral: fhir.ReferralRequest;

    @Input() useBundle :boolean = false;

    dataSource : ReferralRequestDataSource;

    displayedColumns = ['date', 'status', 'intent', 'type','specialty','service','reason','requestor','recipient','resource'];


  constructor(private linksService: LinksService,
              // private modalService: NgbModal,
              public dialog: MatDialog,
              public bundleService: BundleService,
              public fhirService: FhirService) {


  }

  ngOnInit() {

      console.log('Patient id = '+this.patientId);
      if (this.patientId !== undefined) {
          this.dataSource = new ReferralRequestDataSource(this.fhirService, this.patientId, []);
      } else {
          this.dataSource = new ReferralRequestDataSource(this.fhirService, undefined, this.referrals);
      }
      console.log('calling connect');
  }

    select(resource) {
        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;
        dialogConfig.data = {
            id: 1,
            resource: resource
        };
        const resourceDialog: MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
    }

}
