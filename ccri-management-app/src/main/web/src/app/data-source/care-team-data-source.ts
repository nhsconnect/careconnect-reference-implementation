import {DataSource} from '@angular/cdk/table';
import {FhirService} from '../service/fhir.service';
import {BehaviorSubject, Observable} from 'rxjs';

export class CareTeamDataSource extends DataSource<any> {
  constructor(public fhirService: FhirService,
              public patientId: string,
              public careTeams: fhir.CareTeam[]
  ) {
    super();
  }

  private dataStore: {
    careTeams: fhir.CareTeam[]
  };

  connect(): Observable<fhir.CareTeam[]> {

  //  console.log('careTeams DataSource connect '+this.patientId);

    const _careTeams: BehaviorSubject<fhir.CareTeam[]> = <BehaviorSubject<fhir.CareTeam[]>>new BehaviorSubject([]);

    this.dataStore = { careTeams: [] };

    if (this.patientId !== undefined) {
      this.fhirService.get('/CareTeam?patient=' + this.patientId).subscribe((bundle => {
        if (bundle !== undefined && bundle.entry !== undefined) {
          for (const entry of bundle.entry) {
            this.dataStore.careTeams.push(<fhir.CareTeam> entry.resource);

          }
        }
        _careTeams.next(Object.assign({}, this.dataStore).careTeams);
      }));
    } else
    if (this.careTeams !== []) {
      for (const careTeam of this.careTeams) {
        this.dataStore.careTeams.push(<fhir.CareTeam> careTeam);
      }
      _careTeams.next(Object.assign({}, this.dataStore).careTeams);
    }
   return _careTeams.asObservable();
  }

  disconnect() {}
}
