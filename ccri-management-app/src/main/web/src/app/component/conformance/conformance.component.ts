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

  constructor(private fhirSrv : FhirService) {


  }

  ngOnInit() {

      console.log('calling FHIR Service from CaabilityStatement');
      this.conformance = this.fhirSrv.conformance;

      this.fhirSrv.getConformanceChange().subscribe(conformance => {
          this.conformance = conformance;
      });

      this.fhirSrv.getConformance();
  }

}
