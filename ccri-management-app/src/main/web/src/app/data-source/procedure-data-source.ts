import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class ProcedureDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public procedures : fhir.Procedure[]
  ) {
    super();
  }

  private dataStore: {
    procedures: fhir.Procedure[]
  };

  connect(): Observable<fhir.Procedure[]> {

    console.log('procedures DataSource connect '+this.patientId);
    let _procedures : BehaviorSubject<fhir.Procedure[]> =<BehaviorSubject<fhir.Procedure[]>>new BehaviorSubject([]);;

    this.dataStore = { procedures: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/Procedure?patient'+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.procedures.push(<fhir.Procedure> entry.resource);

          }
        }
        _procedures.next(Object.assign({}, this.dataStore).procedures);
      }));
    } else
    if (this.procedures != []) {
      for (let procedure of this.procedures) {
        this.dataStore.procedures.push(<fhir.Procedure> procedure);
      }
      _procedures.next(Object.assign({}, this.dataStore).procedures);
    }

   return _procedures.asObservable();
  }

  disconnect() {}
}
