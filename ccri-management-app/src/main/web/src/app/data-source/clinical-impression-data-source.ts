import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class ClinicalImpressionDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public clinicalImpressions : fhir.ClinicalImpression[]
  ) {
    super();
  }

  private dataStore: {
    clinicalImpressions: fhir.ClinicalImpression[]
  };

  connect(): Observable<fhir.ClinicalImpression[]> {

    console.log('clinicalImpressions DataSource connect '+this.patientId);

    let _clinicalImpressions : BehaviorSubject<fhir.ClinicalImpression[]> =<BehaviorSubject<fhir.ClinicalImpression[]>>new BehaviorSubject([]);

    this.dataStore = { clinicalImpressions: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/ClinicalImpression?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.clinicalImpressions.push(<fhir.ClinicalImpression> entry.resource);

          }
        }
        _clinicalImpressions.next(Object.assign({}, this.dataStore).clinicalImpressions);
      }));
    } else
    if (this.clinicalImpressions != []) {
      for (let impression of this.clinicalImpressions) {
        this.dataStore.clinicalImpressions.push(<fhir.ClinicalImpression> impression);
      }
      _clinicalImpressions.next(Object.assign({}, this.dataStore).clinicalImpressions);
    }
   return _clinicalImpressions.asObservable();
  }

  disconnect() {}
}
