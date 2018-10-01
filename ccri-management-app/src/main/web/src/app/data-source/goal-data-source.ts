import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class GoalDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public goals : fhir.Goal[]
  ) {
    super();
  }

  private dataStore: {
    goals: fhir.Goal[]
  };

  connect(): Observable<fhir.Goal[]> {

    console.log('goals DataSource connect '+this.patientId);

    let _goals : BehaviorSubject<fhir.Goal[]> =<BehaviorSubject<fhir.Goal[]>>new BehaviorSubject([]);

    this.dataStore = { goals: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/Goal?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.goals.push(<fhir.Goal> entry.resource);

          }
        }
        _goals.next(Object.assign({}, this.dataStore).goals);
      }));
    } else
    if (this.goals != []) {
      for (let encounter of this.goals) {
        this.dataStore.goals.push(<fhir.Goal> encounter);
      }
      _goals.next(Object.assign({}, this.dataStore).goals);
    }
   return _goals.asObservable();
  }

  disconnect() {}
}
