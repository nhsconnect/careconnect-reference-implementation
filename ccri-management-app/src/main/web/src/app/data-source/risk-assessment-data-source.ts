import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class RiskAssessmentDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public risks : fhir.RiskAssessment[]
  ) {
    super();
  }

  private dataStore: {
    risks: fhir.RiskAssessment[]
  };

  connect(): Observable<fhir.RiskAssessment[]> {

    console.log('risks DataSource connect '+this.patientId);

    let _risks : BehaviorSubject<fhir.RiskAssessment[]> =<BehaviorSubject<fhir.RiskAssessment[]>>new BehaviorSubject([]);

    this.dataStore = { risks: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/RiskAssessment?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.risks.push(<fhir.RiskAssessment> entry.resource);

          }
        }
        _risks.next(Object.assign({}, this.dataStore).risks);
      }));
    } else
    if (this.risks != []) {
      for (let encounter of this.risks) {
        this.dataStore.risks.push(<fhir.RiskAssessment> encounter);
      }
      _risks.next(Object.assign({}, this.dataStore).risks);
    }
   return _risks.asObservable();
  }

  disconnect() {}
}
