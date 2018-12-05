import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-composition-detail',
  templateUrl: './composition-detail.component.html',
  styleUrls: ['./composition-detail.component.css']
})
export class CompositionDetailComponent implements OnInit {

  @Input() composition: fhir.Composition;

  @Input() encounter: fhir.Encounter;

  constructor() { }

  ngOnInit() {
  }

  getService(service: fhir.Extension[]) :string {
    if (service == undefined) return "";
    let display: string = "";
    for( let extension of service) {
      if (extension.url === "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-CareSettingType-1") {
        display = extension.valueCodeableConcept.coding[0].display;
      }
    }
    return display;
  }


}
