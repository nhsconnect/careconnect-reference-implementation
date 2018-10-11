import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-ed-encounter-list',
  templateUrl: './ed-encounter-list.component.html',
  styleUrls: ['./ed-encounter-list.component.css']
})
export class EdEncounterListComponent implements OnInit {

  constructor(private fhirService : FhirService) { }

  encounters : fhir.Encounter[] = [];

  ngOnInit() {
    this.getEncounters();
    this.fhirService.getConformanceChange().subscribe(capabilityStatement =>
    {
      this.getEncounters();
    });


  }

  getEncounters() {
    this.fhirService.get("/Encounter?type=409971007&status=in-progress").subscribe(bundle => {
      this.encounters= [];
      for (let entry of bundle.entry) {
        this.encounters.push(<fhir.Encounter> entry.resource);
      }
    });

  }

}
