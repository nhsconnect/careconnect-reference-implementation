import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class ObservationDataSource extends DataSource<any> {

  //observations: fhir.Observation[]

  private dataStore: {
    observations: fhir.Observation[]
  };


  constructor(public fhirService : FhirService, public patientId : string, public observations: fhir.Observation[]
  ) {
    super();

  }


  connect(): Observable<fhir.Observation[]> {

    console.log('Obs DataSource connect '+this.patientId);
    let _obs : BehaviorSubject<fhir.Observation[]> =<BehaviorSubject<fhir.Observation[]>>new BehaviorSubject([]);

    this.dataStore = { observations: [] };

     if (this.patientId != undefined) {
      this.fhirService.get('/Observation?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.observations.push(<fhir.Observation> entry.resource);

          }
        }
        _obs.next(Object.assign({}, this.dataStore).observations);
      }));
    } else if (this.observations != [] && this.observations != undefined) {
       console.log('Observation not null');
       for (let observation of this.observations) {
         this.dataStore.observations.push( observation);
       }
       _obs.next(Object.assign({}, this.dataStore).observations);
     }
   return _obs.asObservable();
  }

  disconnect() {}
}
