import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class AllergyIntoleranceDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public allergies : fhir.AllergyIntolerance[]
  ) {
    super();
  }

  private dataStore: {
    allergies: fhir.AllergyIntolerance[]
  };

  connect(): Observable<fhir.AllergyIntolerance[]> {

    console.log('allergies DataSource connect '+this.patientId);

    let _allergies : BehaviorSubject<fhir.AllergyIntolerance[]> =<BehaviorSubject<fhir.AllergyIntolerance[]>>new BehaviorSubject([]);

    this.dataStore = { allergies: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/AllergyIntolerance?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.allergies.push(<fhir.AllergyIntolerance> entry.resource);

          }
        }
        _allergies.next(Object.assign({}, this.dataStore).allergies);
      }));
    } else
    if (this.allergies != []) {
      for (let encounter of this.allergies) {
        this.dataStore.allergies.push(<fhir.AllergyIntolerance> encounter);
      }
      _allergies.next(Object.assign({}, this.dataStore).allergies);
    }
   return _allergies.asObservable();
  }

  disconnect() {}
}
