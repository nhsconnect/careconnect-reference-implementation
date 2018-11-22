export class DocumentRef {

  private _speciality: string;

  private _type: string;

  private _patients: fhir.Patient[];

  private _file : File;

  private _docDate : Date;

  private _organisations: fhir.Organization[];

  private _practitioners: fhir.Practitioner[];

  private _service: string;


  get speciality(): string {
    return this._speciality;
  }

  set speciality(value: string) {
    this._speciality = value;
  }

  get type(): string {
    return this._type;
  }

  set type(value: string) {
    this._type = value;
  }

  get service(): string {
    return this._service;
  }

  set service(value: string) {
    this._service = value;
  }

  set patients(value: fhir.Patient[]) {
    this._patients = value;
  }

  set file(value: File) {
    this._file = value;
  }

  set docDate(value: Date) {
    this._docDate = value;
  }

  set organisations(value: fhir.Organization[]) {
    this._organisations= value;
  }

  set practitioners(value: fhir.Practitioner[]) {
    this._practitioners = value;
  }


  get patients(): fhir.Patient[] {
    return this._patients;
  }

  get file(): File {
    return this._file;
  }

  get docDate(): Date {
    return this._docDate;
  }

  get organisations(): fhir.Organization[] {
    return this._organisations;
  }

  get practitioners(): fhir.Practitioner[] {
    return this._practitioners;
  }

}
