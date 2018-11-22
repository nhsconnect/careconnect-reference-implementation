import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BehaviorSubject, Observable} from "rxjs";


export class ReferralRequestDataSource extends DataSource<any> {
  constructor(public fhirService: FhirService,
              public patientId: string,
              public referrals: fhir.ReferralRequest[]
  ) {
    super();
  }

  private dataStore: {
    referrals: fhir.ReferralRequest[]
  };

  connect(): Observable<fhir.ReferralRequest[]> {

   // console.log('referrals DataSource connect '+this.patientId);

    let _referrals : BehaviorSubject<fhir.ReferralRequest[]> =<BehaviorSubject<fhir.ReferralRequest[]>>new BehaviorSubject([]);

    this.dataStore = { referrals: [] };

    if (this.patientId !== undefined) {
      this.fhirService.get('/ReferralRequest?patient='+this.patientId).subscribe((bundle => {
        if (bundle !== undefined && bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
            this.dataStore.referrals.push(<fhir.ReferralRequest> entry.resource);

          }
        }
        _referrals.next(Object.assign({}, this.dataStore).referrals);
      }));
    } else
    if (this.referrals != [] && this.referrals !== undefined) {
      for (let referral of this.referrals) {
        this.dataStore.referrals.push(<fhir.ReferralRequest> referral);
      }
      _referrals.next(Object.assign({}, this.dataStore).referrals);
    }
   return _referrals.asObservable();
  }

  disconnect() {}
}
