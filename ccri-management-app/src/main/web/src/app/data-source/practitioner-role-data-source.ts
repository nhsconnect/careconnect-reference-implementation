import {DataSource} from "@angular/cdk/table";
import {FhirService} from "../service/fhir.service";
import {BundleService} from "../service/bundle.service";
import {BehaviorSubject, Observable} from "rxjs";

export class PractitionerRoleDataSource extends DataSource<any> {

  constructor(public fhirService : FhirService,
              public bundleService : BundleService,
              public practitioner : fhir.Practitioner,
              public useBundle : boolean = false
  ) {
    super();
    this.practitioner = practitioner;
  }

  private dataStore: {
    roles: fhir.PractitionerRole[]
  };

  connect(): Observable<fhir.PractitionerRole[]> {

    let _roles : BehaviorSubject<fhir.PractitionerRole[]> =<BehaviorSubject<fhir.PractitionerRole[]>>new BehaviorSubject([]);

    this.dataStore = { roles : [] };

    console.log('PractitionerRole.connect useBundle = '+this.useBundle);

    if (!this.useBundle) {
      this.fhirService.get('/PractitionerRole?practitioner='+this.practitioner.id).subscribe(bundle => {
        if (bundle != undefined && bundle.entry != undefined) {
          for (let entry of bundle.entry) {
            console.log('entry = ' + entry.resource.resourceType);
            this.dataStore.roles.push(<fhir.PractitionerRole> entry.resource);

          }
        }
        _roles.next(Object.assign({}, this.dataStore).roles);
      });
    } else {
      this.bundleService.getRolesForPractitioner(this.practitioner.id).subscribe(roles => {
        if (roles != undefined ) {
          for (let role of roles) {
            console.log('role = ' + role);
            this.dataStore.roles.push(<fhir.PractitionerRole> role);

          }
        }
        _roles.next(Object.assign({}, this.dataStore).roles);
      });
    }
    return _roles.asObservable();
  }

  disconnect() {}
}
