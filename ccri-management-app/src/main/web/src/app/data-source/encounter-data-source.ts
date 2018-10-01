import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class EncounterDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public encounters : fhir.Encounter[]
  ) {
    super();
  }

  private dataStore: {
    encounters: fhir.Encounter[]
  };

  connect(): Observable<fhir.Encounter[]> {

    console.log('encounters DataSource connect '+this.patientId);

    let _encounters : BehaviorSubject<fhir.Encounter[]> =<BehaviorSubject<fhir.Encounter[]>>new BehaviorSubject([]);

    this.dataStore = { encounters: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/Encounter?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            console.log(entry.resource._resourceType);
            this.dataStore.encounters.push(<fhir.Encounter> entry.resource);

          }
        }
        _encounters.next(Object.assign({}, this.dataStore).encounters);
      }));
    } else
    if (this.encounters != []) {
      for (let encounter of this.encounters) {
        this.dataStore.encounters.push(<fhir.Encounter> encounter);
      }
      _encounters.next(Object.assign({}, this.dataStore).encounters);
    }
   return _encounters.asObservable();
  }

  disconnect() {}
}
