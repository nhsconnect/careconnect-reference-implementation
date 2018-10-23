import {EventEmitter, Injectable} from '@angular/core';

import {FhirService} from "./fhir.service";


@Injectable()
export class EprService {

  public routes: Object[] = [
    {
      icon: 'search',
      route: '/',
      title: 'FHIR Explorer',
    }
    ,{
      icon: 'add_circle_outline',
      route: '/ed',
      title: 'Triage (+ Patient Find)' +
        '',
    }
    ,{
      icon: 'local_hospital',
      route: '/ed/caseload',
      title: 'Caseload',
    }
    ,{
      icon: 'dashboard',
      route: '/ed/capacity',
      title: 'Emergency Planning',
    }

  ];

  public routesExt : Object[] = [{
    icon: 'lock',
    route: 'https://data.developer.nhs.uk/ccri-auth/',
    title: 'OAuth2 (SMART on FHIR) Server',
  }
    , {
      icon: 'note',
      route: 'https://data.developer.nhs.uk/document-viewer/',
      title: 'FHIR Document Viewer',
    }
  ];



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

  private title : string;

  private titleChangeEvent : EventEmitter<string> = new EventEmitter<string>();

  private patientChangeEvent : EventEmitter<fhir.Patient> = new EventEmitter();

  private resourceChangeEvent : EventEmitter<any> = new EventEmitter();

  private sectionChangeEvent : EventEmitter<string> = new EventEmitter();

  set(patient: fhir.Patient) {

    this.patient = patient;

    this.patientAllergies = [];

    this.patientChangeEvent.emit(this.patient);
  }

  getTitleChange() {
    return this.titleChangeEvent;
  }

  setTitle(title : string) {
    this.title = title;
    this.titleChangeEvent.emit(title);
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
