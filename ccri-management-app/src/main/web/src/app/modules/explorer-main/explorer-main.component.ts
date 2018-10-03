import { Component, OnInit } from '@angular/core';
import {FhirService} from "../../service/fhir.service";
import {TdMediaService} from "@covalent/core";

@Component({
  selector: 'app-explorer-main',
  templateUrl: './explorer-main.component.html',
  styleUrls: ['./explorer-main.component.css']
})
export class ExplorerMainComponent implements OnInit {



  constructor(public fhirSrv: FhirService,public media: TdMediaService) { }

  ngOnInit() {


  }

}
