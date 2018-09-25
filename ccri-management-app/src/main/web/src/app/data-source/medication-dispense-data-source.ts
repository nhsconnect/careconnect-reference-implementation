import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class MedicationDispenseDataSource extends DataSource<any> {


  constructor(public fhirService : FhirService,
              public patientId : string,
              public dispenses : fhir.MedicationDispense[]
  ) {
    super();
  }

  private dataStore: {
    dispenses: fhir.MedicationDispense[]
  };

  connect(): Observable<fhir.MedicationDispense[]> {
    console.log('medicationDispenses DataSource connect '+this.patientId);


    let _dispenses : BehaviorSubject<fhir.MedicationDispense[]> =<BehaviorSubject<fhir.MedicationDispense[]>>new BehaviorSubject([]);;

    this.dataStore = { dispenses: [] };

    if (this.patientId != undefined) {

      this.fhirService.get('/MedicationDispenset?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.dispenses.push(<fhir.MedicationDispense> entry.resource);

          }
        }

        _dispenses.next(Object.assign({}, this.dataStore).dispenses);

      }

      ));

    } else
    if (this.dispenses != []) {
      for (let dispense of this.dispenses) {
        this.dataStore.dispenses.push(<fhir.MedicationDispense> dispense);
      }
      _dispenses.next(Object.assign({}, this.dataStore).dispenses);
    }

    return _dispenses.asObservable();
  }

  disconnect() {}
}
