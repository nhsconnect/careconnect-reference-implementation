import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.css']
})
export class MainComponent implements OnInit {

  constructor(private FHIRSrv : FhirService) { }

  public conformance : fhir.CapabilityStatement;

  public serverBase : string;

  ngOnInit() {

    this.serverBase = this.FHIRSrv.getFHIRServerBase();

    this.FHIRSrv.getConformanceChange().subscribe(capabilityStatement =>
    {
      this.conformance = capabilityStatement;
    });
  }

}
