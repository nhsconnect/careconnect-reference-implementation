import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class PatientDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,

              public patients : fhir.Patient[],
              public patientObservable : Observable<fhir.Patient[]>,
              public useObservable : boolean = false
  ) {
    super();

  }

  private dataStore: {
    patients: fhir.Patient[]
  };

  connect(): Observable<fhir.Patient[]> {

    //
    console.log('calling data service');
    if (this.useObservable) {
      console.log('Patient Observable ');
      return this.patientObservable;
    }


    let _patients : BehaviorSubject<fhir.Patient[]> =<BehaviorSubject<fhir.Patient[]>>new BehaviorSubject([]);;

    this.dataStore = { patients: [] };

    if (this.patients != []) {
      for (let patient of this.patients) {
        this.dataStore.patients.push(<fhir.Patient> patient);
      }
      _patients.next(Object.assign({}, this.dataStore).patients);
    }

   return _patients.asObservable();
  }

  disconnect() {}
}
