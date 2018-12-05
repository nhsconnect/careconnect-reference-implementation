import { Injectable } from '@angular/core';
import {FhirService} from "./fhir.service";
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class BundleService {

  private bundle: fhir.Bundle;

  constructor(private fhirService: FhirService) { }

  public setBundle(bundle: fhir.Bundle) {
    this.bundle = bundle;
  }

  public getBundle(): fhir.Bundle {
    return this.bundle;
  }

  public getRolesForPractitioner(reference: string)  : Observable<fhir.PractitionerRole[]> {


    let roles: fhir.PractitionerRole[]  = [];
    let _roles: BehaviorSubject<fhir.PractitionerRole[]> =<BehaviorSubject<fhir.PractitionerRole[]>>new BehaviorSubject([]);


    console.log('BundleService.getRolesForPractitioner Search ref='+reference);

    if (this.bundle !== undefined && reference.indexOf('/') == -1) {

      for (let entry of this.bundle.entry) {
        if (entry.resource.resourceType == 'PractitionerRole') {


          let role: fhir.PractitionerRole = <fhir.PractitionerRole> entry.resource;
          console.log('Item Reference '+ role.practitioner.reference);
          if (role.practitioner !== undefined && role.practitioner.reference.indexOf(reference) != -1) {
            console.log('Found - ' +entry.resource.resourceType);
            roles.push(<fhir.PractitionerRole> entry.resource);
          }
        }
      }
      _roles.next(roles);
      return _roles;
    } else {
      this.fhirService.get('/PractitionerRole?practitioner='+reference).subscribe(bundle => {
        for (let entry of bundle.entry) {
          console.log(entry.resource.id);
          let role: fhir.PractitionerRole = <fhir.PractitionerRole> entry.resource;
          roles.push(<fhir.PractitionerRole> entry.resource);
        }
        _roles.next(roles);
        return _roles;
      });
    }
  }

  public getResource(reference: string) : Observable<fhir.Resource> {
    console.log("Bundle Get Reference = " +reference);

    let resource: fhir.Resource  = undefined;
    let _resource: BehaviorSubject<fhir.Resource> =<BehaviorSubject<fhir.Resource>>new BehaviorSubject([]);

    let resourceRes :fhir.Resource = undefined;
    if (this.bundle !== undefined && reference.indexOf('/') == -1) {
      for (let entry of this.bundle.entry) {
        if (entry.fullUrl === reference || entry.resource.id === reference) {
          console.log(entry.resource.resourceType);
          resource = entry.resource;
          _resource.next(resource);
        }
      }
    } else {
      return this.fhirService.getResource('/'+reference);
    }
    return _resource;
  }


}
