import {Component, OnInit, ViewChild} from '@angular/core';
import {FhirService} from "../../../service/fhir.service";
import {NguiMapComponent} from "@ngui/map";
import {EprService} from "../../../service/epr.service";


declare class MarkerPosition {
  location : any[];
  icon;
  title: string;
}


@Component({
  selector: 'app-capacity',
  templateUrl: './capacity.component.html',
  styleUrls: ['./capacity.component.css']
})

export class CapacityComponent implements OnInit {

  @ViewChild(NguiMapComponent)
  ngMap:NguiMapComponent;

  public positions :MarkerPosition[] =[];

  coords = undefined;

  public count: number = 0;

  anchor = {
  url: 'https://plnkr.co/img/plunker.png',
  anchor: [16,16],
  size: [32,32],
  scaledSize: [32,32]
};

  encounters: fhir.Encounter[] = [];

  constructor(private fhirService: FhirService, private eprService : EprService) { }

  ngOnInit() {
    this.getEncounters();
    this.eprService.setTitle('Emergency Planning - Garforth Sector');
  }

  getEncounters() {
    this.encounters= [];
    this.fhirService.get("/Encounter?type=409971007&status=triaged").subscribe(bundle => {

        if (bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
            this.encounters.push(<fhir.Encounter> entry.resource);

          }
        }
      }, () => {},
      () => {
        for (let encounter of this.encounters) {
          this.getEncounterDetail(encounter);
        }
      }
    );
    this.fhirService.get("/Encounter?type=409971007&status=in-progress").subscribe(bundle => {

        if (bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
            this.encounters.push(<fhir.Encounter> entry.resource);
          }
        }
      },
      () => {},
      () => {
        for (let encounter of this.encounters) {
          this.getEncounterDetail(encounter);
        }
      });

  }


  getEncounterDetail(encounter: fhir.Encounter) {

    this.fhirService.get('/Encounter?_id='+encounter.id+'&_revinclude=*').subscribe( bundle => {
        this.encounters = [];
        for (let entry of bundle.entry) {
          let sub: fhir.Encounter = <fhir.Encounter> entry.resource;
          if (sub.id !== encounter.id) {
            this.coords = '53.759701, -1.445495';
            this.encounters.push(sub);
          }
        }
      },
      ()=>{

      }
      , ()=> {

        for (let enc of this.encounters) {

          // ambulance encounter is the only one we are interested in - the triage should be finished and handedover
          if (enc.status !=='finished' ) {
            for (let enclocation of enc.location) {

              if (enclocation.status == 'planned' || enclocation.status == 'active') {
                this.fhirService.getResource('/' + enclocation.location.reference).subscribe(location => {
                  if (enc.type[0].coding[0].code === '245581009') {
                    //   this.coords = location.position.latitude + ', ' + location.position.longitude;
                   }
                  let marker : MarkerPosition = {
                    location : [location.position.latitude, location.position.longitude],
                    icon : this.anchor,
                    title : location.type.coding[0].code + ': '+ location.name

                  };

                  this.positions.push(marker);

                })
              }
            }

          }
        }
      })
  }



  onMapReady(map) {
   // console.log('map', map);
  //  console.log('markers', map.markers);  // to get all markers as an array
  }
  onIdle(event) {
    //console.log('map', event.target);
  }
  onMarkerInit(marker) {
   // console.log('marker', marker);
  }
  onMapClick(event) {
    this.positions.push(event.latLng);
    event.target.panTo(event.latLng);
  }
}
