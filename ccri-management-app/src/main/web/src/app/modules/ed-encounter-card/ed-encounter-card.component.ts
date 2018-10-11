import {Component, Input, OnInit} from '@angular/core';
import {FhirService} from "../../service/fhir.service";
import {Router} from "@angular/router";

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

  coords = undefined;

    public positions=[];

  constructor(private fhirService : FhirService,
              private router : Router) { }

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
          if (sub.type != undefined && sub.type.length>0 && sub.type[0].coding.length >0 ) {
            for(let location of sub.location) {
              this.fhirService.getResource('/'+location.location.reference).subscribe( location => {
                if (sub.type[0].coding[0].code === '245581009') {
                  this.coords = location.position.latitude + ', '+location.position.longitude;
                  }
                this.positions.push([location.position.latitude, location.position.longitude]);
              })
            }
          }
          //his.coords = sub.53.80634, -1.52304
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

    onMapReady(map) {
        console.log('map', map);
        console.log('markers', map.markers);  // to get all markers as an array
    }
    onIdle(event) {
        console.log('map', event.target);
    }
    onMarkerInit(marker) {
        console.log('marker', marker);
    }
    onMapClick(event) {
        this.positions.push(event.latLng);
        event.target.panTo(event.latLng);
    }

    viewDetails(patient : fhir.Patient) {
        this.router.navigateByUrl('/ed/patient/'+patient.id);
    }
}
