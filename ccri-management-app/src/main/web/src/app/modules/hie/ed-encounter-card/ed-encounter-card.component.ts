import {Component, Input, OnInit} from '@angular/core';
import {FhirService} from "../../../service/fhir.service";
import {Router} from "@angular/router";


@Component({
  selector: 'app-ed-encounter-card',
  templateUrl: './ed-encounter-card.component.html',
  styleUrls: ['./ed-encounter-card.component.css']
})
export class EdEncounterCardComponent implements OnInit {

  @Input()
  encounter: fhir.Encounter;

  heart: fhir.Observation = undefined;
  temp: fhir.Observation = undefined;
  resp: fhir.Observation = undefined;
  bp: fhir.Observation = undefined;
    news2: fhir.Observation = undefined;
    o2: fhir.Observation = undefined;
    alert2: fhir.Observation = undefined;
    air: fhir.Observation = undefined;

  patient: fhir.Patient = undefined;
  encounters: fhir.Encounter[] = [];
  observations: fhir.Observation[] = [];
  flags: fhir.Flag[] = [];


  plannedLocStatus: boolean = true;

    ambulanceLoc: fhir.Location = undefined;
    plannedLoc :fhir.Location = undefined;



    constructor(private fhirService: FhirService,
              private router : Router) { }

  ngOnInit() {


      // Main encounter lookup
    this.fhirService.get('/Encounter?_id='+this.encounter.id+'&_include=Encounter:patient&_revinclude=Encounter:part-of').subscribe( bundle => {
      this.encounters = [];
      this.observations = [];
      for (let entry of bundle.entry) {
          switch (entry.resource.resourceType) {
              case "Encounter":
                  let sub: fhir.Encounter = <fhir.Encounter> entry.resource;
                  if (sub.id !== this.encounter.id) {
                      //his.coords = sub.53.80634, -1.52304
                      this.encounters.push(sub);
                  }
                  break;
              case 'Patient':
                  this.patient = <fhir.Patient> entry.resource;
                  break;
              default:
                  console.log('Udder '+entry.resource.resourceType);
          }

      }
    },
        ()=>{

        }
        , ()=> {
            this.ambulanceLoc = undefined;
            this.plannedLoc = undefined;

            for(let identifier of this.patient.identifier) {
                if (identifier.system === 'https://fhir.nhs.uk/Id/nhs-number') {
                    this.getPatientData(identifier.value);
                }
            }

            let locations: fhir.Location[] = [];

            for (let enc of this.encounters) {
                this.fhirService.get('/Encounter?_id='+enc.id+'&_include=Encounter:location&_revinclude=Observation:context').subscribe(
                    bundle => {
                        for (let entry of bundle.entry) {
                            switch (entry.resource.resourceType) {

                                case 'Observation':

                                    let obs: fhir.Observation = <fhir.Observation> entry.resource;
                                    this.observations.push(obs);
                                    break;
                                case 'Location':
                                    locations.push(<fhir.Location> entry.resource);
                                    break;
                                case 'Encounter':
                                    //locations.push(<fhir.Location> entry.resource);
                                    break;
                                default:
                                    console.log(entry.resource.resourceType);
                            }

                        }
                    },
                    ()=>{},
                    ()=>
                    {
                        this.getObs();
                        if (enc.status !=='finished' ) {
                            for (let enclocation of enc.location) {

                                if (enclocation.status == 'planned' || enclocation.status == 'active') {
                                    for (let location of locations) {
                                        if (enclocation.location.reference.includes('/'+location.id)) {
                                            if (location.type.coding[0].code === 'AMB') {
                                                this.ambulanceLoc = location;

                                            } else {
                                                this.plannedLoc = location;

                                                if (enclocation.status == 'active') {
                                                    this.plannedLocStatus = true;
                                                } else {
                                                    this.plannedLocStatus = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    });

                // ambulance encounter is the only one we are interested in - the triage should be finished and handedover

            }
        })
  }


    getPatientData(nhsNumber: string) {

      // Mock up using CCRI for now (EOLC not supported in NRLS until next phase

        this.fhirService.get('/Patient?_id='+this.patient.id+'&_revinclude=Flag:patient').subscribe( bundle => {
            if (bundle.entry !== undefined) {
                for (let entry of bundle.entry) {
                    switch (entry.resource.resourceType) {
                      case 'Flag':
                        let flag: fhir.Flag = <fhir.Flag> entry.resource;
                        this.flags.push(flag);
                    }


                }
            }
        })
    }

  getObs() {

      this.heart = undefined;
      this.temp = undefined;
      this.resp = undefined;
      this.bp = undefined;
      this.news2 = undefined;
      this.o2 = undefined;
      this.air = undefined;
      this.alert2 = undefined;

      for(let obs of this.observations) {

          switch (obs.code.coding[0].code) {
              case '364075005':
                  this.heart = obs;
                  break;
              case '276885007':
                  this.temp = obs;
                  break;
              case '86290005':
                  this.resp = obs;
                  break;
              case "1104051000000101":
                  this.news2 = obs;
                  break;
              case "1104441000000107":
                  this.alert2 = obs;
                  break;
              case "75367002":
                  this.bp = obs;
                  break;
              case "431314004":
              case "866661000000106":
              case "866701000000100":
                  this.o2 = obs;
                  break;
              case "301282008":
              case "371825009":
            case "722742002":
                  this.air = obs;
                  break;
              default :
                  console.log('Not processed');
                  console.log(obs);
                  break;
          }
      }

  }

    getLastName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";

        let name = "";
        if (patient.name[0].family !== undefined) name += patient.name[0].family.toUpperCase();
        return name;

    }

  getFirstName(patient :fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.name == undefined || patient.name.length == 0)
      return "";
    // Move to address
    let name = "";
    if (patient.name[0].given !== undefined && patient.name[0].given.length>0) name += ", "+ patient.name[0].given[0];

    if (patient.name[0].prefix !== undefined && patient.name[0].prefix.length>0) name += " (" + patient.name[0].prefix[0] +")" ;
    return name;

  }

  getNHSIdentifier(patient: fhir.Patient) : String {
    if (patient == undefined) return "";
    if (patient.identifier == undefined || patient.identifier.length == 0)
      return "";
    // Move to address
    var NHSNumber :String = "";
    for (var f=0;f<patient.identifier.length;f++) {
      if (patient.identifier[f].system.includes("nhs-number") )
        NHSNumber = NHSNumber = patient.identifier[f].value.substring(0,3)+ ' '+patient.identifier[f].value.substring(3,6)+ ' '+patient.identifier[f].value.substring(6);
    }
    return NHSNumber;

  }

  getValue(obs: fhir.Observation) {
      let value = "";

      if (obs.valueQuantity !== undefined) {
        value = obs.valueQuantity.value.toString()
      }
    if (obs.valueCodeableConcept !== undefined) {
      value = obs.valueCodeableConcept.coding[0].display;
    }
    if (obs.component!=undefined && obs.component.length > 1) {
      let sys = 0;
      let dia = 0;
      for (let comp of obs.component) {
        if (comp.code.coding[0].code =='72313002') {
          sys = Number(comp.valueQuantity.value);
        }
        if (comp.code.coding[0].code =='271650006') {
          dia = Number(comp.valueQuantity.value);
        }
        if (comp.code.coding[0].code =='1091811000000102') {
          dia = Number(comp.valueQuantity.value);
        }
      }

      value = sys + '/'+dia;
    }
    if (value === "" && obs.code !== undefined && obs.code.coding !== undefined) {
      value = obs.code.coding[0].display;
    }
      return value;
  }

  getColour(obs: fhir.Observation) {
      let colour: string = undefined;
      let value : number = undefined;

      if (obs.code.coding !== undefined) {
        switch (obs.code.coding[0].code) {
          case "1104051000000101":
            // news2
            value = Number(this.getValue(obs));
            if (value > 6) {
              colour = 'warn';
            } else if (value > 4 ) {
              colour = 'accent';
            }
            /// how is a aggregate score of 3 identified
            break;

          case '364075005':
            // pulse
            value = Number(this.getValue(obs));

            if (value > 130 || value <40 ) {
              colour = 'warn';
            } else if (value > 110  ) {
              colour = 'accent';
            } else if (value > 90 || value< 51  ) {
              colour = ''; // yellow
            }
            break;
          case '276885007':
            // temp
            value  = Number(this.getValue(obs));
            if (value <= 35 ) {
              colour = 'warn';
            } else if (value > 39  ) {
              colour = 'accent';
            } else if (value > 38 || value< 36.1  ) {
              colour = ''; // yellow
            }
            break;
          case '86290005':
            // resp
            value = Number(this.getValue(obs));
            if (value > 24 || value <9 ) {
              colour = 'warn';
            } else if (value > 20  ) {
              colour = 'accent';
            } else if ( value< 12  ) {
              colour = ''; // yellow
            }
            break;

          case "365933000":
            // alert
            if (obs.valueCodeableConcept.coding[0].code == '422768004') {
              colour = 'accent';
            }
            if (obs.valueCodeableConcept.coding[0].code == '130987000') {
              colour = 'accent';
            }
            break;
          case "75367002":
            // bp
            value=undefined;

            for (let comp of obs.component) {
            //  console.log(comp.code.coding[0].code);
              if (comp.code.coding[0].code ==='72313002') {
                value = Number(obs.component[0].valueQuantity.value);
              }
            }
            if (value !== undefined) {
             // console.log('bp '+value);
              if (value < 91 || value > 219) {
                colour = 'warn';
              } else if (value < 101) {
                colour = 'accent';
              } else if (value < 111) {
                colour = ''; // yellow
              }
            }
            break;

          case "431314004":
          case "866661000000106":
          case "866701000000100":
  // o2
            value = Number(this.getValue(obs));
            let onair :boolean = false;
            if (this.air !== undefined) {
              onair = this.air.code.coding[0].code == '371825009';
            }
            if (onair) {
              if (value < 84 || value > 96) {
                colour = 'warn';
              } else if (value < 86 || value > 94) {
                colour = 'accent';
              } else if (value < 88 || value > 92) {
                colour = ''; // yellow
              }
            } else {
              if (value < 92) {
                colour = 'warn';
              } else if (value < 94) {
                colour = 'accent';
              } else if (value < 96) {
                colour = ''; // yellow
              }
            }
            break;
          case "371825009":
            colour = 'accent';
            break;
          case "301282008":
            if (obs.valueCodeableConcept.coding[0].code == '371825009') {
              colour = 'accent';
            }
            break;
        }
      }

      return colour;
  }
  getSelected(obs: fhir.Observation) :boolean {
      let value = this.getColour(obs);

      if (value !== undefined ) {

        return true;
      } else return false;
  }



    viewDetails(patient: fhir.Patient) {
        this.router.navigateByUrl('/ed/patient/'+patient.id);
    }
}
