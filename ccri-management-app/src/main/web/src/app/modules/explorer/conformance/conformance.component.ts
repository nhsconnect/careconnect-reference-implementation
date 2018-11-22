import { Component, OnInit } from '@angular/core';
import {FhirService, Formats} from "../../../service/fhir.service";
//import {} from "@types/fhir";

@Component({
  selector: 'app-conformance',
  templateUrl: './conformance.component.html',
  styleUrls: ['./conformance.component.css']
})
export class ConformanceComponent implements OnInit {

  public resource : any;

  public resourceString : any = undefined;

  public format : Formats;

  constructor(private fhirSrv: FhirService) {


  }

  ngOnInit() {

      console.log('calling FHIR Service from CabilityStatement');
      //this.resource = this.fhirSrv.conformance;

      this.format = this.fhirSrv.getFormat();

      this.fhirSrv.getFormatChange().subscribe( format => {
        this.format = format;
        // If format changes refresh the conformance statement
        this.getResults();
      });

      this.getResults();

  }

  getResults() {
    this.fhirSrv.getResults(this.fhirSrv.getFHIRServerBase()+'/metadata').subscribe(conformance => {
      switch(this.format) {
        case 'jsonf':
          this.resource = conformance;
          break;
        case 'json' :
          this.resource = conformance;
          this.resourceString = JSON.stringify(conformance, null, 2);
          break;

        case 'xml':
          let reader = new FileReader();
          reader.addEventListener('loadend', (e) => {
            this.resourceString = reader.result;
          });
          reader.readAsText(<Blob> conformance);
      }
    });

  }
}
