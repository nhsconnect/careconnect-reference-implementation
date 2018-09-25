import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class MedicationDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,

              public medications : fhir.Medication[]
  ) {
    super();
  }

  private dataStore: {
    medications: fhir.Medication[]
  };

  connect(): Observable<fhir.Medication[]> {


    let _medications : BehaviorSubject<fhir.Medication[]> =<BehaviorSubject<fhir.Medication[]>>new BehaviorSubject([]);;

    this.dataStore = { medications: [] };

    if (this.medications != []) {
      for (let procedure of this.medications) {
        this.dataStore.medications.push(<fhir.Medication> procedure);
      }
      _medications.next(Object.assign({}, this.dataStore).medications);
    }

   return _medications.asObservable();
  }

  disconnect() {}
}
