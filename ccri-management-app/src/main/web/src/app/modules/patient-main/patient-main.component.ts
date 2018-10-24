import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {EprService} from "../../service/epr.service";
import {MatChip} from "@angular/material";

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-main.component.html',
  styleUrls: ['./patient-main.component.css']
})
export class PatientMainComponent implements OnInit {

    patient : fhir.Patient = undefined;


    yascolor = 'info';
    acutecolor = 'info';
    gpcolor = 'info';
    nrlscolor = 'info';
    @ViewChild('gpchip') gpchip : MatChip;




  constructor(private router : Router, private fhirSrv : FhirService,  private route: ActivatedRoute, private eprService : EprService) { }

  ngOnInit() {

      let patientid = this.route.snapshot.paramMap.get('patientid');
         this.eprService.setTitle('Health Information Exchange Portal');
      this.acutecolor = 'info';
      this.yascolor = 'info';

      this.fhirSrv.getResource('/Patient/'+patientid).subscribe(patient => {

              this.patient = patient;
              this.acutecolor = 'primary';
              this.yascolor = 'primary';
          }
          ,()=> {
            this.acutecolor = 'warn';
            this.yascolor = 'warn';
        }

      );
      this.eprService.getGPCStatusChangeEvent().subscribe( colour => {
          this.gpcolor = colour;
      });
      this.eprService.getNRLSStatusChangeEvent().subscribe( colour => {
          this.nrlscolor = colour;
      });
  }

    onClick(event, btn) {
      console.log(event);
      switch (btn) {
          case 'bs':
              this.router.navigate(['summary'], {relativeTo: this.route });
              break;
          case 'bo':
              this.router.navigate(['vital-signs'], {relativeTo: this.route });
              break;
          case 'be':
              this.router.navigate(['encounters'], {relativeTo: this.route });
              break;
          case 'bd':
              this.router.navigate(['documents'], {relativeTo: this.route });
              break;
      }
    }

    getFirstName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";
        // Move to address
        let name = "";
        if (patient.name[0].given != undefined && patient.name[0].given.length>0) name += ", "+ patient.name[0].given[0];

        if (patient.name[0].prefix != undefined && patient.name[0].prefix.length>0) name += " (" + patient.name[0].prefix[0] +")" ;
        return name;

    }

    getNHSIdentifier(patient : fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.identifier == undefined || patient.identifier.length == 0)
            return "";
        // Move to address
        var NHSNumber :String = "";
        for (var f=0;f<patient.identifier.length;f++) {
            if (patient.identifier[f].system.includes("nhs-number") )
                NHSNumber = patient.identifier[f].value;
        }
        return NHSNumber;

    }

    getLastName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";

        let name = "";
        if (patient.name[0].family != undefined) name += patient.name[0].family.toUpperCase();
        return name;

    }

}