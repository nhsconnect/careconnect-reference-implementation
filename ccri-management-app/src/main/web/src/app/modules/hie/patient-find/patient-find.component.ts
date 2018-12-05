import { Component, OnInit } from '@angular/core';
import {StepState} from "@covalent/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {EprService} from "../../../service/epr.service";


@Component({
  selector: 'app-patient-find',
  templateUrl: './patient-find.component.html',
  styleUrls: ['./patient-find.component.css']
})
export class PatientFindComponent implements OnInit {

  constructor(private _formBuilder: FormBuilder,private router: Router, private eprService : EprService) { }

  ngOnInit() {
    this.eprService.setTitle('Patient Find');

      this.triageFormGroup = this._formBuilder.group({
          breathingCtrl: ['', Validators.required]
      });
  }

    triageFormGroup: FormGroup;

    yesno = [
        {name: 'Yes', viewValue: 0},
        {name: 'No', viewValue: 1}
    ];
    sexes = [
        {name: 'Female', viewValue: 'female'},
        {name: 'Male', viewValue: 'male'}
    ];

    activeDeactiveStep1Msg: string = 'No select/deselect detected yet';
    stateStep2: StepState = StepState.Required;
    stateStep3: StepState = StepState.Complete;
    disabled: boolean = false;

    toggleRequiredStep2(): void {
        this.stateStep2 = (this.stateStep2 === StepState.Required ? StepState.None : StepState.Required);
    }

    toggleCompleteStep3(): void {
        this.stateStep3 = (this.stateStep3 === StepState.Complete ? StepState.None : StepState.Complete);
    }

    activeStep1Event(): void {
        this.activeDeactiveStep1Msg = 'Active event emitted.';
    }

    deactiveStep1Event(): void {
        this.activeDeactiveStep1Msg = 'Deactive event emitted.';
    }

    selectPatient(patient: fhir.Patient) {
        //  console.log('Patient change - '+patient.id);
        if (patient !== undefined) {
            //this.patientChange.set(patient);
            this.router.navigateByUrl('ed/patient/'+patient.id  );

        }
    }


}
