import {EventEmitter, Injectable} from '@angular/core';

import {FhirService} from "./fhir.service";


@Injectable()
export class EprService {

  patient: fhir.Patient = undefined;

  resource : any = undefined;

  section : string;

  userName : string;

  userEmail : string;

  patientAllergies : fhir.AllergyIntolerance[] = [];

  constructor(
    private fhirService : FhirService
  ) { }

  documentReference : fhir.DocumentReference;

  private patientChangeEvent : EventEmitter<fhir.Patient> = new EventEmitter();

  private resourceChangeEvent : EventEmitter<any> = new EventEmitter();

  private sectionChangeEvent : EventEmitter<string> = new EventEmitter();

  set(patient: fhir.Patient) {

    this.patient = patient;

    this.patientAllergies = [];
/*
    if (patient != undefined && patient.id != undefined) {
      this.fhirService.get('/AllergyIntolerance?patient='+patient.id).subscribe(data => {

          if (data.entry != undefined) {
            for (let entry of data.entry) {
              this.patientAllergies.push(<fhir.AllergyIntolerance> entry.resource);
            }
          }
        }
      );
    }
*/
    this.patientChangeEvent.emit(this.patient);
  }
  clear() {
    this.patient = undefined;
    this.patientChangeEvent.emit(this.patient);
  }
  getPatientChangeEmitter() {
    return this.patientChangeEvent;
  }

  getResourceChangeEvent() {
    return this.resourceChangeEvent;
  }

  getSectionChangeEvent() {
    return this.sectionChangeEvent;
  }

  setSection(section : string) {
    this.section = section;
    this.sectionChangeEvent.emit(section);
  }

  setResource(resource) {
    this.resource = resource;
    this.resourceChangeEvent.emit(resource);
  }

  setDocumentReference(document : fhir.DocumentReference) {
    this.documentReference = document;
  }

}
