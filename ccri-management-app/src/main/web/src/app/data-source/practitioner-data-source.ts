import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class PractitionerDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,

              public practitioners : fhir.Practitioner[],
              public practitionersObservable : Observable<fhir.Practitioner[]>,
              public useObservable : boolean = false
  ) {
    super();
  }

  private dataStore: {
    practitioners: fhir.Practitioner[]
  };

  connect(): Observable<fhir.Practitioner[]> {

    console.log('calling data service');
    if (this.useObservable) {
      console.log('Practitioners Observable ');
      return this.practitionersObservable;
    }

    let _practitioners : BehaviorSubject<fhir.Practitioner[]> =<BehaviorSubject<fhir.Practitioner[]>>new BehaviorSubject([]);;

    this.dataStore = { practitioners: [] };

    if (this.practitioners != undefined && this.practitioners != []) {
      for (let practitioner of this.practitioners) {
        this.dataStore.practitioners.push(<fhir.Practitioner> practitioner);
      }
      _practitioners.next(Object.assign({}, this.dataStore).practitioners);
    }

   return _practitioners.asObservable();
  }

  disconnect() {}
}
