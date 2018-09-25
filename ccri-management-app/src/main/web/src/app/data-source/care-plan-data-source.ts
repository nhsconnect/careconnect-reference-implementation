import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class CarePlanDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public carePlans : fhir.CarePlan[]
  ) {
    super();
  }

  private dataStore: {
    carePlans: fhir.CarePlan[]
  };

  connect(): Observable<fhir.CarePlan[]> {

    console.log('carePlans DataSource connect '+this.patientId);

    let _carePlans : BehaviorSubject<fhir.CarePlan[]> =<BehaviorSubject<fhir.CarePlan[]>>new BehaviorSubject([]);

    this.dataStore = { carePlans: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/CarePlan?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.carePlans.push(<fhir.CarePlan> entry.resource);

          }
        }
        _carePlans.next(Object.assign({}, this.dataStore).carePlans);
      }));
    } else
    if (this.carePlans != []) {
      for (let carePlan of this.carePlans) {
        this.dataStore.carePlans.push(<fhir.CarePlan> carePlan);
      }
      _carePlans.next(Object.assign({}, this.dataStore).carePlans);
    }
   return _carePlans.asObservable();
  }

  disconnect() {}
}
