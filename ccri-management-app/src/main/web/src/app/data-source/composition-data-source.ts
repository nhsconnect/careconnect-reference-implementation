import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class CompositionDataSource extends DataSource<any> {


  constructor(public fhirService: FhirService, public patientId: string,public documents: fhir.Composition[]
  ) {

    super();

  }

  private dataStore: {
    documents: fhir.Composition[]
  };

  connect(): Observable<fhir.Composition[]> {

  //  console.log('documents DataSource connect '+this.patientId);
    let _documents : BehaviorSubject<fhir.Composition[]> =<BehaviorSubject<fhir.Composition[]>>new BehaviorSubject([]);;

    this.dataStore = { documents: [] };

    if (this.patientId !== undefined) {
      this.fhirService.get('/Composition?patient='+this.patientId).subscribe((bundle => {
        if (bundle !== undefined && bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.documents.push(<fhir.Composition> entry.resource);

          }
        }
        _documents.next(Object.assign({}, this.dataStore).documents);
      }));
    } else
    if (this.documents !== [] && this.documents !== undefined) {
       for (let document of this.documents) {
         this.dataStore.documents.push(<fhir.Composition> document);
       }
      _documents.next(Object.assign({}, this.dataStore).documents);
    }


   return _documents.asObservable();
  }

  disconnect() {}
}
