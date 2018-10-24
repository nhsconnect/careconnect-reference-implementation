import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {LinksService} from "../../service/links.service";

@Component({
  selector: 'app-patient-encounter-detail',
  templateUrl: './patient-encounter-detail.component.html',
  styleUrls: ['./patient-encounter-detail.component.css']
})
export class PatientEncounterDetailComponent implements OnInit {

  patientid = undefined;
  encounterid=  undefined;

    observations: fhir.Observation[];
    obsTotal : number;

    prescriptions : fhir.MedicationRequest[];
    presTotal : number;

    procedures : fhir.Procedure[];
    procTotal : number;

    encounter : fhir.Encounter;

    conditions : fhir.Condition[];
    conditionTotal : number;

    allergies : fhir.AllergyIntolerance[];

    immunisations : fhir.Immunization[];

    documents : fhir.DocumentReference[];


  constructor(private route: ActivatedRoute,private fhirService : FhirService
      ,private linksService : LinksService) { }

  ngOnInit() {

      this.patientid = this.route.snapshot.paramMap.get('patientid');
      this.encounterid = this.route.snapshot.paramMap.get('encounterid');

      this.fhirService.get('/Encounter?_id='+this.encounterid+'&_revinclude=Observation:context&_revinclude=Encounter:part-of&_revinclude=Procedure:context&_revinclude=Condition:context&_revinclude=MedicationRequest:context&_revinclude=Immunization:encounter&_revinclude=DocumentReference:context&_count=50').subscribe(bundle=> {
              this.observations = [];
              this.obsTotal = 0;

              this.prescriptions = [];
              this.presTotal = 0;

              this.conditions = [];
              this.conditionTotal = 0;

              this.procedures = [];
              this.procTotal = 0;
              if (bundle.entry != undefined) {

                  for (let entry of bundle.entry) {

                    switch (entry.resource.resourceType) {
                        case 'Observation' :
                            this.observations.push(<fhir.Observation>entry.resource);
                            this.obsTotal++;
                            break;
                        case 'Procedure':
                                this.procedures.push(<fhir.Procedure> entry.resource);
                                this.procTotal++;
                            break;
                        case 'MedicationRequest':
                                this.prescriptions.push(<fhir.MedicationRequest> entry.resource);
                                this.presTotal++;
                            break;
                        case 'Condition':
                                this.conditions.push(<fhir.Condition> entry.resource);
                                this.conditionTotal++;
                            break;
                        case 'Immunization':
                            this.immunisations.push(<fhir.Immunization> entry.resource);

                            break;
                        case 'AllergyIntolerance':
                            this.allergies.push(<fhir.AllergyIntolerance> entry.resource);

                            break;
                        case 'DocumentReference':
                            this.documents.push(<fhir.DocumentReference> entry.resource);

                            break;
                        case 'Encounter':
                                this.encounter = <fhir.Encounter> entry.resource;
                                break;
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
