import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class LocationDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public locations : fhir.Location[]
  ) {
    super();
  }

  private dataStore: {
    locations: fhir.Location[]
  };

  connect(): Observable<fhir.Location[]> {


    let _locations : BehaviorSubject<fhir.Location[]> =<BehaviorSubject<fhir.Location[]>>new BehaviorSubject([]);;

    this.dataStore = { locations: [] };

    if (this.locations != [] && this.locations != undefined) {
      for (let location of this.locations) {
        this.dataStore.locations.push(<fhir.Location> location);
      }
      _locations.next(Object.assign({}, this.dataStore).locations);
    }

   return _locations.asObservable();
  }

  disconnect() {}
}
