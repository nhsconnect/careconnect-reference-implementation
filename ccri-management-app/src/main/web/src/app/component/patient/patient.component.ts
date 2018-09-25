
import {Component, OnInit, Input, EventEmitter, Output} from '@angular/core';
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {PatientDataSource} from "../../data-source/patient-data-source";
import {FhirService} from "../../service/fhir.service";
import {PractitionerDialogComponent} from "../../dialog/practitioner-dialog/practitioner-dialog.component";
import {OrganisationDialogComponent} from "../../dialog/organisation-dialog/organisation-dialog.component";
import {BundleService} from "../../service/bundle.service";
import {Observable} from "rxjs";



@Component({
  selector: 'app-patient',
  templateUrl: './patient.component.html',
  styleUrls: ['./patient.component.css']
})
export class PatientComponent implements OnInit {

  @Input() patients : fhir.Patient[];

  @Input() patientsObservable : Observable<fhir.Patient[]>;

  @Input() useObservable : boolean = false;

  @Input() showResourceLink : boolean = true;

  @Output() patient = new EventEmitter<any>();

  dataSource : PatientDataSource;

  practitioners : fhir.Practitioner[];

  organisations : fhir.Organization[];

  displayedColumns = ['patient', 'dob','gender','identifier', 'contact', 'gp','prac','resource'];

  constructor( private dialog : MatDialog,
               public fhirService : FhirService,
                public bundleService : BundleService) {

  }

  ngOnInit() {
    if (!this.showResourceLink) {
      this.displayedColumns = ['select','patient', 'dob','gender','identifier', 'contact', 'gp','prac'];
    }
    if (this.useObservable) {
      this.dataSource = new PatientDataSource(this.fhirService,  undefined, this.patientsObservable, this.useObservable);
    } else {
      if (this.patients != undefined) {
        this.dataSource = new PatientDataSource(this.fhirService, this.patients, undefined, this.useObservable);
      }
    }
  }

  getFirstAddress(patient : fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.address == undefined || patient.address.length == 0)
      return "";
    return patient.address[0].line.join(", ")+", "+patient.address[0].city+", "+patient.address[0].postalCode;

  }
  getLastName(patient : fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.name == undefined || patient.name.length == 0)
      return "";

    let name = "";
    if (patient.name[0].family != undefined) name += patient.name[0].family.toUpperCase();
   return name;

  }
  getFirstName(patient : fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.name == undefined || patient.name.length == 0)
      return "";
    // Move to address
    let name = "";
    if (patient.name[0].given != undefined && patient.name[0].given.length>0) name += ", "+ patient.name[0].given[0];

    if (patient.name[0].prefix != undefined && patient.name[0].prefix.length>0) name += " (" + patient.name[0].prefix[0] +")" ;
    return name;

  }

  getFirstTelecom(patient : fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.telecom == undefined || patient.telecom.length == 0)
      return "";
    // Move to address
    return patient.telecom[0].value;

  }

  getIdentifier(identifier : fhir.Identifier) : String {
    let name : String = identifier.system
    if (identifier.system.indexOf('nhs-number') != -1) {

      name = 'NHS Number';
    } else if (identifier.system.indexOf('pas-number') != -1) {
      name='PAS Number';
    } else if (identifier.system.indexOf('PPMIdentifier') != -1) {
      name='LTH PPM Id';
    }
    return name;
  }

  getNHSIdentifier(patient : fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.identifier == undefined || patient.identifier.length == 0)
      return "";
    // Move to address
    var NHSNumber :String = "";
    for (var f=0;f<patient.identifier.length;f++) {
      if (patient.identifier[f].system.includes("nhs-number") )
        NHSNumber = patient.identifier[f].value;
    }
    return NHSNumber;

  }

  showOrganisation(patient : fhir.Patient) {
    this.organisations = [];

    this.bundleService.getResource(patient.managingOrganization.reference).subscribe( (organisation) => {

      if (organisation != undefined && organisation.resourceType === "Organization") {

        this.organisations.push(<fhir.Organization> organisation);

        const dialogConfig = new MatDialogConfig();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;

        dialogConfig.data = {
          id: 1,
          organisations : this.organisations
        };
        let resourceDialog : MatDialogRef<OrganisationDialogComponent> = this.dialog.open( OrganisationDialogComponent, dialogConfig);

      }
    });
  }



  showPractitioner(patient : fhir.Patient) {
    this.practitioners = [];

    for (let practitionerReference of patient.generalPractitioner) {
      this.bundleService.getResource(practitionerReference.reference).subscribe((practitioner) => {
          if (practitioner != undefined && practitioner.resourceType === "Practitioner") {
            this.practitioners.push(<fhir.Practitioner> practitioner);

            const dialogConfig = new MatDialogConfig();

            dialogConfig.disableClose = true;
            dialogConfig.autoFocus = true;

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

  selectPatient(patient : fhir.Patient) {
    this.patient.emit(patient);
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
