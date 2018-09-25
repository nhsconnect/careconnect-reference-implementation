import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {FhirService} from "../../service/fhir.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {MedicationRequestDataSource} from "../../data-source/medication-request-data-source";
import {BundleService} from "../../service/bundle.service";
import {MedicationDialogComponent} from "../../dialog/medication-dialog/medication-dialog.component";

@Component({
  selector: 'app-medication-request',
  templateUrl: './medication-request.component.html',
  styleUrls: ['./medication-request.component.css']
})
export class MedicationRequestComponent implements OnInit {

  @Input() medicationRequests : fhir.MedicationRequest[];

  @Input() showDetail : boolean = false;

  meds : fhir.Medication[];


  selectedMeds : fhir.Medication[];

  @Output() medicationRequest = new EventEmitter<any>();

  @Input() patientId : string;

  dataSource : MedicationRequestDataSource;

  displayedColumns = ['medication', 'medicationlink','dose','route','routelink','form', 'status','authored',  'resource'];


  constructor(private linksService : LinksService,

              private fhirService : FhirService,
              private bundleService : BundleService,
              public dialog: MatDialog) { }

  ngOnInit() {
    if (this.patientId != undefined) {
      this.dataSource = new MedicationRequestDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new MedicationRequestDataSource(this.fhirService, undefined, this.medicationRequests);
    }
  }
  isSNOMED(system: string) : boolean {
    return this.linksService.isSNOMED(system);
  }
  getCodeSystem(system : string) : string {
    return this.linksService.getCodeSystem(system);
  }

  getDMDLink(code : fhir.Coding) {
    window.open(this.linksService.getDMDLink(code), "_blank");
  }
  getSNOMEDLink(code : fhir.Coding) {
    window.open(this.linksService.getSNOMEDLink(code), "_blank");

  }

  onClick(medicationRequest : fhir.MedicationRequest) {
    console.log("Clicked - " + medicationRequest.id);
    this.selectedMeds = [];

    if (this.bundleService.getBundle() != undefined) {

      if (medicationRequest.medicationReference != null) {
        console.log("medicationReference - " + medicationRequest.medicationReference.reference);
        this.bundleService.getResource(medicationRequest.medicationReference.reference).subscribe(
          (medtemp) => {
            if (medtemp != undefined && medtemp.resourceType === "Medication") {
              console.log('meds list ' + medtemp.id);
              this.selectedMeds.push(<fhir.Medication> medtemp);

              const dialogConfig = new MatDialogConfig();

              dialogConfig.disableClose = true;
              dialogConfig.autoFocus = true;
              dialogConfig.data = {
                id: 1,
                medications: this.selectedMeds
              };
              let resourceDialog: MatDialogRef<MedicationDialogComponent> = this.dialog.open(MedicationDialogComponent, dialogConfig);
            }
          }
        )
      }
    } else {
      let reference = medicationRequest.medicationReference.reference;
      console.log(reference);
      let refArray: string[] = reference.split('/');
      if (refArray.length>1) {
        this.fhirService.get('/Medication/'+(refArray[refArray.length - 1])).subscribe(data => {
            if (data != undefined) {
              this.meds.push(<fhir.Medication>data);
              this.selectedMeds.push(<fhir.Medication>data);
            }
          },
          error1 => {
          },
          () => {
            console.log("Content = ");
            const dialogConfig = new MatDialogConfig();

            dialogConfig.disableClose = true;
            dialogConfig.autoFocus = true;
            dialogConfig.data = {
              id: 1,
              medications: this.selectedMeds
            };
            let resourceDialog : MatDialogRef<MedicationDialogComponent> = this.dialog.open( MedicationDialogComponent, dialogConfig);
          }
        );
      }
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
