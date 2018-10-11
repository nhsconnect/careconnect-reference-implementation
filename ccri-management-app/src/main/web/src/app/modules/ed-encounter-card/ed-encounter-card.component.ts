import {Component, Input, OnInit} from '@angular/core';
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-ed-encounter-card',
  templateUrl: './ed-encounter-card.component.html',
  styleUrls: ['./ed-encounter-card.component.css']
})
export class EdEncounterCardComponent implements OnInit {

  @Input()
  encounter : fhir.Encounter;

  patient : fhir.Patient = undefined;
  encounters : fhir.Encounter[] = [];

  constructor(private fhirService : FhirService) { }

  ngOnInit() {
    this.fhirService.getResource('/'+this.encounter.subject.reference).subscribe(patient => {
      this.patient = patient;
    })
    this.fhirService.get('/Encounter?_id='+this.encounter.id+'&_revinclude=*').subscribe( bundle => {
      this.encounters = [];
      for (let entry of bundle.entry) {
        let sub : fhir.Encounter = <fhir.Encounter> entry.resource;
        if (sub.id !== this.encounter.id) {
          console.log('found one');
          this.encounters.push(sub);
        }
      }
    })
  }
  getLastName(patient :fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.name == undefined || patient.name.length == 0)
      return "";

    let name = "";
    if (patient.name[0].family != undefined) name += patient.name[0].family.toUpperCase();
    return name;

  }
  getFirstName(patient :fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.name == undefined || patient.name.length == 0)
      return "";
    // Move to address
    let name = "";
    if (patient.name[0].given != undefined && patient.name[0].given.length>0) name += ", "+ patient.name[0].given[0];

    if (patient.name[0].prefix != undefined && patient.name[0].prefix.length>0) name += " (" + patient.name[0].prefix[0] +")" ;
    return name;

  }

  getNHSIdentifier(patient : fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.identifier == undefined || patient.identifier.length == 0)
      return "";
    // Move to address
    var NHSNumber :String = "";
    for (var f=0;f<patient.identifier.length;f++) {
      if (patient.identifier[f].system.includes("nhs-number") )
        NHSNumber = patient.identifier[f].value;
    }
    return NHSNumber;

  }
}
