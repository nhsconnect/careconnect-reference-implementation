import {Component, Input, OnInit} from '@angular/core';
import {FhirService} from "../../service/fhir.service";
import {LinksService} from "../../service/links.service";

@Component({
  selector: 'app-encounter-detail',
  templateUrl: './encounter-detail.component.html',
  styleUrls: ['./encounter-detail.component.css']
})
export class EncounterDetailComponent implements OnInit {

  @Input() patientId : number;

  @Input() encounterId : string;

  observations: fhir.Observation[];
  obsTotal : number;

  prescriptions : fhir.MedicationRequest[];
  presTotal : number;

  procedures : fhir.Procedure[];
  procTotal : number;

  encounter : fhir.Encounter;

  conditions : fhir.Condition[];
  conditionTotal : number;

  constructor(private fhirService : FhirService
            ,private linksService : LinksService
  ) { }

  ngOnInit() {

    this.fhirService.get('/Encounter?_id='+this.encounterId+'&_revinclude=*&_count=50').subscribe(data=> {
        this.observations = [];
        this.obsTotal = 0;

        this.prescriptions = [];
        this.presTotal = 0;

      this.conditions = [];
      this.conditionTotal = 0;

        this.procedures = [];
        this.procTotal = 0;
        if (data.entry != undefined) {

          for (let entNo = 0; entNo < data.entry.length; entNo++) {
           // console.log(data.entry[entNo].resource);
            if (data.entry[entNo].resource.resourceType == 'Observation') {
              this.observations.push(<fhir.Observation>data.entry[entNo].resource);
              this.obsTotal++;
            }
            if (data.entry[entNo].resource.resourceType == 'Procedure') {
              this.procedures.push(<fhir.Procedure>data.entry[entNo].resource);
              this.procTotal++;
            }
            if (data.entry[entNo].resource.resourceType == 'MedicationRequest') {
              this.prescriptions.push(<fhir.MedicationRequest>data.entry[entNo].resource);
              this.presTotal++;
            }
            if (data.entry[entNo].resource.resourceType == 'Condition') {
              this.conditions.push(<fhir.Condition>data.entry[entNo].resource);
              this.conditionTotal++;
            }
            if (data.entry[entNo].resource.resourceType == 'Encounter') {
              this.encounter = <fhir.Encounter> data.entry[entNo].resource;

            }
          }
        }
      }
    );
  }
  getCodeSystem(system : string) : string {
    return this.linksService.getCodeSystem(system);
  }

  isSNOMED(system: string) : boolean {
    return this.linksService.isSNOMED(system);
  }


  getSNOMEDLink(code : fhir.Coding) {
    if (this.linksService.isSNOMED(code.system)) {
      window.open(this.linksService.getSNOMEDLink(code), "_blank");
    }
  }
}
