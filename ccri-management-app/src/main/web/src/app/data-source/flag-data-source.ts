import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class FlagDataSource extends DataSource<any> {
  constructor(public fhirService: FhirService,
              public patientId: string,
              public flags: fhir.Flag[]
  ) {
    super();
  }

  private dataStore: {
    flags: fhir.Flag[]
  };

  connect(): Observable<fhir.Flag[]> {

  //  console.log('flags DataSource connect '+this.patientId);

    let _flags : BehaviorSubject<fhir.Flag[]> =<BehaviorSubject<fhir.Flag[]>>new BehaviorSubject([]);

    this.dataStore = { flags: [] };

    if (this.patientId !== undefined) {
      this.fhirService.get('/Flag?patient='+this.patientId).subscribe((bundle => {
        if (bundle !== undefined && bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.flags.push(<fhir.Flag> entry.resource);

          }
        }
        _flags.next(Object.assign({}, this.dataStore).flags);
      }));
    } else
    if (this.flags != []) {
      for (let flag of this.flags) {
        this.dataStore.flags.push(<fhir.Flag> flag);
      }
      _flags.next(Object.assign({}, this.dataStore).flags);
    }
   return _flags.asObservable();
  }

  disconnect() {}
}
