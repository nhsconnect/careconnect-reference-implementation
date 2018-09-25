import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class ImmunizationDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public immunisations : fhir.Immunization[]
  ) {
    super();
  }

  private dataStore: {
    immunisations: fhir.Immunization[]
  };

  connect(): Observable<fhir.Immunization[]> {

    console.log('immunisations DataSource connect '+this.patientId);
    let _immunisations : BehaviorSubject<fhir.Immunization[]> =<BehaviorSubject<fhir.Immunization[]>>new BehaviorSubject([]);;

    this.dataStore = { immunisations: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/Immunization?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.immunisations.push(<fhir.Immunization> entry.resource);

          }
        }
        _immunisations.next(Object.assign({}, this.dataStore).immunisations);
      }))
    } else if (this.immunisations != []) {
        for (let immunisation of this.immunisations) {
          this.dataStore.immunisations.push(<fhir.Immunization> immunisation);
        }
        _immunisations.next(Object.assign({}, this.dataStore).immunisations);
      };

   return _immunisations.asObservable();
  }

  disconnect() {}
}
