import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class OrganisationDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,

              public organisations : fhir.Organization[],
              public organisationsObservable : Observable<fhir.Organization[]>,
              public useObservable : boolean = false
  ) {
    super();
  }

  private dataStore: {
    organisations: fhir.Organization[]
  };

  connect(): Observable<fhir.Organization[]> {
    console.log('calling data service');
    if (this.useObservable) {
      console.log('Organization Observable ');
      return this.organisationsObservable;
    }

    let _organisations : BehaviorSubject<fhir.Organization[]> =<BehaviorSubject<fhir.Organization[]>>new BehaviorSubject([]);;

    this.dataStore = { organisations: [] };

    if (this.organisations != undefined && this.organisations != []) {
      for (let organisation of this.organisations) {
        this.dataStore.organisations.push(<fhir.Organization> organisation);
      }
      _organisations.next(Object.assign({}, this.dataStore).organisations);
    }

   return _organisations.asObservable();
  }

  disconnect() {}
}
