import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";

@Component({
  selector: 'app-ed-encounter-list',
  templateUrl: './ed-encounter-list.component.html',
  styleUrls: ['./ed-encounter-list.component.css']
})
export class EdEncounterListComponent implements OnInit {

  constructor(private fhirService: FhirService, private eprService : EprService) { }

  encounters: fhir.Encounter[] = [];

  ngOnInit() {
    this.getEncounters();
    this.eprService.setTitle('Emergency Control Centre View - Garforth Sector');
    this.fhirService.getConformanceChange().subscribe(capabilityStatement =>
    {
      this.getEncounters();
    });


  }

  getEncounters() {
      this.encounters= [];
    this.fhirService.get("/Encounter?type=409971007&status=triaged").subscribe(bundle => {

      if (bundle.entry !== undefined) {
          for (let entry of bundle.entry) {
              this.encounters.push(<fhir.Encounter> entry.resource);
          }
      }
    });
      this.fhirService.get("/Encounter?type=409971007&status=in-progress").subscribe(bundle => {

          if (bundle.entry !== undefined) {
              for (let entry of bundle.entry) {
                  this.encounters.push(<fhir.Encounter> entry.resource);
              }
          }
      });

  }

}
