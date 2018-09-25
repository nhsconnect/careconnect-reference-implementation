import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class MedicationStatementDataSource extends DataSource<any> {
  constructor(public fhirService : FhirService,
              public patientId : string,
              public medicationStatements : fhir.MedicationStatement[]
  ) {
    super();
  }

  private dataStore: {
    medicationStatements: fhir.MedicationStatement[]
  };

  connect(): Observable<fhir.MedicationStatement[]> {

    console.log('medicationStatements DataSource connect '+this.patientId);
    let _medicationStatements : BehaviorSubject<fhir.MedicationStatement[]> =<BehaviorSubject<fhir.MedicationStatement[]>>new BehaviorSubject([]);;

    this.dataStore = { medicationStatements: [] };

    if (this.patientId != undefined) {
      this.fhirService.get('/MedicationStatement?patient='+this.patientId).subscribe((bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.medicationStatements.push(<fhir.MedicationStatement> entry.resource);

          }
        }
        _medicationStatements.next(Object.assign({}, this.dataStore).medicationStatements);
      }));
    } else
    if (this.medicationStatements != []) {
      for (let procedure of this.medicationStatements) {
        this.dataStore.medicationStatements.push(<fhir.MedicationStatement> procedure);
      }
      _medicationStatements.next(Object.assign({}, this.dataStore).medicationStatements);
    }

   return _medicationStatements.asObservable();
  }

  disconnect() {}
}
