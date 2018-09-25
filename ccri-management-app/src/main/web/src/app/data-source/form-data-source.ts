import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class QuestionnaireResponseDataSource extends DataSource<any> {

  //forms: fhir.QuestionnaireResponse[]

  private dataStore: {
    forms: fhir.QuestionnaireResponse[]
  };


  constructor(public fhirService : FhirService, public patientId : string, public forms: fhir.QuestionnaireResponse[]
  ) {
    super();

  }


  connect(): Observable<fhir.QuestionnaireResponse[]> {

    console.log('Obs DataSource connect '+this.patientId);
    let _obs : BehaviorSubject<fhir.QuestionnaireResponse[]> =<BehaviorSubject<fhir.QuestionnaireResponse[]>>new BehaviorSubject([]);

    this.dataStore = { forms: [] };

     if (this.patientId != undefined) {
      this.fhirService.get('/QuestionnaireResponse?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.forms.push(<fhir.QuestionnaireResponse> entry.resource);

          }
        }
        _obs.next(Object.assign({}, this.dataStore).forms);
      }));
    } else if (this.forms != [] && this.forms != undefined) {
       console.log('QuestionnaireResponse not null');
       for (let form of this.forms) {
         this.dataStore.forms.push( form);
       }
       _obs.next(Object.assign({}, this.dataStore).forms);
     }
   return _obs.asObservable();
  }

  disconnect() {}
}
