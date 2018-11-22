import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

export class MedicationAdministrationDataSource extends DataSource<any> {


  constructor(public fhirService: FhirService,
              public patientId: string,
              public administrations: fhir.MedicationAdministration[]
  ) {
    super();
  }

  private dataStore: {
    administrations: fhir.MedicationAdministration[]
  };

  connect(): Observable<fhir.MedicationAdministration[]> {
  //  console.log('medicationAdministrations DataSource connect '+this.patientId);


    let _administrations : BehaviorSubject<fhir.MedicationAdministration[]> =<BehaviorSubject<fhir.MedicationAdministration[]>>new BehaviorSubject([]);;

    this.dataStore = { administrations: [] };

    if (this.patientId !== undefined) {

      this.fhirService.get('/MedicationAdministrationt?patient='+this.patientId).subscribe((bundle => {
        if (bundle !== undefined && bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.administrations.push(<fhir.MedicationAdministration> entry.resource);

          }
        }

        _administrations.next(Object.assign({}, this.dataStore).administrations);

      }

      ));

    } else
    if (this.administrations != []) {
      for (let administration of this.administrations) {
        this.dataStore.administrations.push(<fhir.MedicationAdministration> administration);
      }
      _administrations.next(Object.assign({}, this.dataStore).administrations);
    }

    return _administrations.asObservable();
  }

  disconnect() {}
}
