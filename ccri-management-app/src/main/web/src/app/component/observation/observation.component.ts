import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LinksService} from "../../service/links.service";
import {ResourceDialogComponent} from "../../dialog/resource-dialog/resource-dialog.component";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material";
import {ObservationDataSource} from "../../data-source/observation-data-source";
import {FhirService} from "../../service/fhir.service";


@Component({
  selector: 'app-observation',
  templateUrl: './observation.component.html',
  styleUrls: ['./observation.component.css']
})
export class ObservationComponent implements OnInit {

  @Input() observations : fhir.Observation[];

  @Input() showDetail : boolean = false;

  @Input() patientId : string;

  @Output() observation = new EventEmitter<any>();

  selectedObs : fhir.Observation;

  dataSource : ObservationDataSource;

  displayedColumns = ['date', 'code','codelink','category', 'status','value', 'resource'];

  constructor(private linksService : LinksService,
             // private modalService: NgbModal,
              public dialog: MatDialog,
              public fhirService : FhirService) { }

  ngOnInit() {
    console.log('Patient id = '+this.patientId);
    if (this.patientId != undefined) {
      this.dataSource = new ObservationDataSource(this.fhirService, this.patientId, []);
    } else {
      this.dataSource = new ObservationDataSource(this.fhirService, undefined, this.observations);
    }
    console.log('calling connect');
    //this.dataSource.connect(this.patientId);
  }



  getValue(observation : fhir.Observation) : string {
    //console.log("getValue called" + observation.valueQuantity.value);
    if (observation == undefined) return "";

    if (observation.valueQuantity != undefined ) {
      //console.log(observation.valueQuantity.value);
      let unit: string = "";
      if (observation.valueQuantity.unit !== undefined) {
        unit = observation.valueQuantity.unit;
      }
      return observation.valueQuantity.value.toPrecision(4) + " " + unit;
    }

    if (observation.component == undefined || observation.component.length < 2)
      return "";
    // Only coded for blood pressures at present
    if (observation.component[0].valueQuantity === undefined )
      return "";
    if (observation.component[1].valueQuantity === undefined )
      return "";
    let unit0: string = "";
    let unit1: string = "";
    if (observation.component[0].valueQuantity.unit !== undefined) {
      unit0 = observation.component[0].valueQuantity.unit;
    }
    if (observation.component[1].valueQuantity.unit !== undefined) {
      unit1 = observation.component[1].valueQuantity.unit;
    }
    if (observation.component[0].code !== undefined && observation.component[0].code.coding !== undefined && observation.component[0].code.coding.length > 0) {
      unit0 = observation.component[0].code.coding[0].display;
    }
    if (observation.component[1].code !== undefined && observation.component[1].code.coding !== undefined && observation.component[1].code.coding.length > 0) {
      unit1 = observation.component[1].code.coding[0].display;
    }

    if (unit0 === unit1 || unit1==="") {
      return observation.component[0].valueQuantity.value + "/" + observation.component[1].valueQuantity.value + " " + unit0;
    } else {
      return observation.component[0].valueQuantity.value + "/" + observation.component[1].valueQuantity.value + " " + unit0 + "/" + unit1;
    }

  }

  getCodeSystem(system : string) : string {
     return this.linksService.getCodeSystem(system);
  }

  isSNOMED(system: string) : boolean {
    return this.linksService.isSNOMED(system);
  }

  /*

  onClick(content , observation : fhir.Observation) {
    console.log("Clicked - "+ observation.id);
    this.selectedObs = observation;
    //this.router.navigate(['./medicalrecord/'+this.getPatientId(this.observation.subject.reference)+'/observation/'+this.observation.code.coding[0].code ] );
    this.modalService.open(content, { windowClass: 'dark-modal' });
  }
  */

  getSNOMEDLink(code : fhir.Coding) {
    if (this.linksService.isSNOMED(code.system)) {
      window.open(this.linksService.getSNOMEDLink(code), "_blank");
    }
  }

  select(resource) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      id: 1,
      resource: resource
    };
    let resourceDialog : MatDialogRef<ResourceDialogComponent> = this.dialog.open( ResourceDialogComponent, dialogConfig);
  }
}
