import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class MedicationRequestDataSource extends DataSource<any> {


  constructor(public fhirService : FhirService,
              public patientId : string,
              public prescriptions : fhir.MedicationRequest[]
  ) {
    super();
  }

  private dataStore: {
    prescriptions: fhir.MedicationRequest[]
  };

  connect(): Observable<fhir.MedicationRequest[]> {
    console.log('medicationRequests DataSource connect '+this.patientId);


    let _prescriptions : BehaviorSubject<fhir.MedicationRequest[]> =<BehaviorSubject<fhir.MedicationRequest[]>>new BehaviorSubject([]);;

    this.dataStore = { prescriptions: [] };

    if (this.patientId != undefined) {

      this.fhirService.get('/MedicationRequest?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.prescriptions.push(<fhir.MedicationRequest> entry.resource);

          }
        }

        _prescriptions.next(Object.assign({}, this.dataStore).prescriptions);

      }

      ));

    } else
    if (this.prescriptions != []) {
      for (let prescription of this.prescriptions) {
        this.dataStore.prescriptions.push(<fhir.MedicationRequest> prescription);
      }
      _prescriptions.next(Object.assign({}, this.dataStore).prescriptions);
    }

    return _prescriptions.asObservable();
  }

  disconnect() {}
}
