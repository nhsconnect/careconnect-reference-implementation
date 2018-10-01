import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class ConditionDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService, public patientId : string,public conditions : fhir.Condition[]
  ) {

    super();

  }

  private dataStore: {
    conditions: fhir.Condition[]
  };

  connect(): Observable<fhir.Condition[]> {

    console.log('conditions DataSource connect '+this.patientId);
    let _conditions : BehaviorSubject<fhir.Condition[]> =<BehaviorSubject<fhir.Condition[]>>new BehaviorSubject([]);;

    this.dataStore = { conditions: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/Condition?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.conditions.push(<fhir.Condition> entry.resource);

          }
        }
        _conditions.next(Object.assign({}, this.dataStore).conditions);
      }));
    } else
    if (this.conditions != []) {
       for (let condition of this.conditions) {
         this.dataStore.conditions.push(<fhir.Condition> condition);
       }
      _conditions.next(Object.assign({}, this.dataStore).conditions);
    }


   return _conditions.asObservable();
  }

  disconnect() {}
}
