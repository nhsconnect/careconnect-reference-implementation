import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../../service/fhir.service";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.css']
})
export class MainComponent implements OnInit {

  constructor(private FHIRSrv: FhirService) { }

  public conformance: fhir.CapabilityStatement;

  public serverBase: string;

  ngOnInit() {
   // console.log('main on init');

    this.FHIRSrv.getConformance();

    this.serverBase = this.FHIRSrv.getFHIRServerBase();


    this.FHIRSrv.getConformanceChange().subscribe(capabilityStatement =>
    {
      this.conformance = capabilityStatement;
      this.serverBase = this.FHIRSrv.getFHIRServerBase();
    },
        (error)=> {
      console.log('main error'+ error);
        });
  }

}
