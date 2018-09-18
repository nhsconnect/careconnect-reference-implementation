import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../service/fhir.service";
//import {} from "@types/fhir";

@Component({
  selector: 'app-conformance',
  templateUrl: './conformance.component.html',
  styleUrls: ['./conformance.component.css']
})
export class ConformanceComponent implements OnInit {

  public conformance : any;

  constructor(private fhirService : FhirService) {


  }

  ngOnInit() {

      console.log('calling FHIR Service from CaabilityStatement');
      this.fhirService.getConformance().subscribe(conformance => {
          this.conformance = conformance;
      });
  }

}
