import {Component, Input, OnInit} from '@angular/core';
import {FhirService} from "../../service/fhir.service";
import {Router} from "@angular/router";
import {isLineBreak} from "codelyzer/angular/sourceMappingVisitor";

@Component({
  selector: 'app-ed-encounter-card',
  templateUrl: './ed-encounter-card.component.html',
  styleUrls: ['./ed-encounter-card.component.css']
})
export class EdEncounterCardComponent implements OnInit {

  @Input()
  encounter : fhir.Encounter;

  heart : string = undefined;
  temp : string = undefined;
  resp : string = undefined;
  bp : string = undefined;
    news2 : string = undefined;
    o2 : string = undefined;
    alert : string = undefined;
    air : string = undefined;

  patient : fhir.Patient = undefined;
  encounters : fhir.Encounter[] = [];
  nrlsdocuments : fhir.DocumentReference[] = [];

  coords = undefined;

    public positions=[];

    ambulanceLoc : fhir.Location = undefined;
    plannedLoc :fhir.Location = undefined;



    constructor(private fhirService : FhirService,
              private router : Router) { }

  ngOnInit() {

    this.fhirService.getResource('/'+this.encounter.subject.reference).subscribe(patient => {
      this.patient = patient;
        for(let identifier of patient.identifier) {
            if (identifier.system === 'https://fhir.nhs.uk/Id/nhs-number') {
                this.getNRLSData(identifier.value);
            }
        }

    })
    this.fhirService.get('/Encounter?_id='+this.encounter.id+'&_revinclude=*').subscribe( bundle => {
      this.encounters = [];
      for (let entry of bundle.entry) {
        let sub : fhir.Encounter = <fhir.Encounter> entry.resource;
        if (sub.id !== this.encounter.id) {
          //his.coords = sub.53.80634, -1.52304
          this.encounters.push(sub);
        }
      }
    },
        ()=>{

        }
        , ()=> {
            this.ambulanceLoc = undefined;
            this.plannedLoc = undefined;
            for (let enc of this.encounters) {

                // ambulance encounter is the only one we are interested in - the triage should be finished and handedover
                if (enc.status !=='finished' ) {
                    for (let location of enc.location) {

                        if (location.status == 'planned' || location.status == 'active') {
                            this.fhirService.getResource('/' + location.location.reference).subscribe(location => {
                               /* if (enc.type[0].coding[0].code === '245581009') {
                                    this.coords = location.position.latitude + ', ' + location.position.longitude;
                                } */
                               // this.positions.push([location.position.latitude, location.position.longitude]);
                                if (location.type.coding[0].code === 'AMB') {
                                    this.ambulanceLoc = location;
                                } else {
                                    this.plannedLoc = location;
                                }
                            })
                        }
                    }
                    this.getObs(enc.id);
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

    getNRLSData(nhsNumber : string) {
        this.fhirService.getNRLS('/DocumentReference?subject=https%3A%2F%2Fdemographics.spineservices.nhs.uk%2FSTU3%2FPatient%2F'+nhsNumber).subscribe( bundle => {
            if (bundle.entry !== undefined) {
                for (let entry of bundle.entry) {
                    let document: fhir.DocumentReference = <fhir.DocumentReference> entry.resource;
                    this.nrlsdocuments.push(document);

                }
            }
        })
    }

  getObs(encounterId) {
      this.fhirService.get('/Encounter?_id='+encounterId+'&_revinclude=*').subscribe(bundle => {
        //console.log(bundle);
          this.heart = undefined;
          this.temp = undefined;
          this.resp = undefined;
          this.bp = undefined;
          this.news2 = undefined;
          this.o2 = undefined;
          this.air = undefined;
          this.alert = undefined;

        for (let entry of bundle.entry) {
          if (entry.resource.resourceType === 'Observation') {
            let obs : fhir.Observation = <fhir.Observation> entry.resource;
            switch (obs.code.coding[0].code) {
                case '364075005':
                  this.heart = obs.valueQuantity.value.toString();
                  break;
                case '276885007':
                    this.temp = obs.valueQuantity.value.toString();
                    break;
                case '86290005':
                    this.resp = obs.valueQuantity.value.toString();
                    break;
                case "859261000000108":
                    this.news2 = obs.valueQuantity.value.toString();
                    break;
                case "365933000":
                    this.alert = obs.valueCodeableConcept.coding[0].display;
                    break;
                case "75367002":
                    this.bp = obs.component[0].valueQuantity.value.toString() + '/'+obs.component[1].valueQuantity.value.toString();
                    break;
                case "431314004":
                    this.o2 = obs.valueQuantity.value.toString();
                    break;
                case "301282008":
                  this.air = obs.valueCodeableConcept.coding[0].display;
                  break;
                default :
                    console.log(obs);
            }

          }
        }
      })
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
