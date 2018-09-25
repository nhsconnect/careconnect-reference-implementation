import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class ConsentDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public consents : fhir.Consent[]
  ) {
    super();
  }

  private dataStore: {
    consents: fhir.Consent[]
  };

  connect(): Observable<fhir.Consent[]> {

    console.log('consents DataSource connect '+this.patientId);

    let _consents : BehaviorSubject<fhir.Consent[]> =<BehaviorSubject<fhir.Consent[]>>new BehaviorSubject([]);

    this.dataStore = { consents: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/Consent?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.consents.push(<fhir.Consent> entry.resource);

          }
        }
        _consents.next(Object.assign({}, this.dataStore).consents);
      }));
    } else
    if (this.consents != []) {
      for (let consent of this.consents) {
        this.dataStore.consents.push(<fhir.Consent> consent);
      }
      _consents.next(Object.assign({}, this.dataStore).consents);
    }
   return _consents.asObservable();
  }

  disconnect() {}
}
