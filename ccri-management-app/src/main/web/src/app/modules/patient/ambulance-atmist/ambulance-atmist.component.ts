import { Component, OnInit } from '@angular/core';
import {EprService} from '../../../service/epr.service';

@Component({
  selector: 'app-ambulance-atmist',
  templateUrl: './ambulance-atmist.component.html',
  styleUrls: ['./ambulance-atmist.component.css']
})
export class AmbulanceATMISTComponent implements OnInit {




    public mechanism = [

        {
            'name': 'complaint',
            'type': 'select',
            'multiple': true,
            'selections': [
                'RTA',
                'Burn',
                'Fall',
                'Cardiac'
            ],
            // "default": "Fall",
            'required': true
        },
        {
            'name': "notes",
            'hint': "this is a textarea hint",
            'type': "textarea",
            'required': false
        }
    ];

  public injuries = [

        {
            'name': "injuries",
            'hint': "this is a textarea hint",
            'type': "textarea",
            'required': false
        }
    ];



    public treatments = [



       {
            'name': "Procedure",
            'type': "select",
            'multiple': true,
            'selections': [
                "CPR",
                "Chest seal",
                "Pressure Dressing"
            ],
            'required': true
        },
        {
            'name': "Medication",
            'type': "select",
            'multiple': false,
            'selections': [
                "Adrenaline",
                "Amiodarone",
                "Aspirin",
                "Atropine",
                "Chlorphenamine",
                "Diazepam",
                "Entonox",
                "Glucagon",
                "Glyceryl Trinitrate",
                "Hydrocortisone",
                "Ibuprofen",
                "Ipratropium Bromide",
                "Ketamine",
                "Midazolam Patient's Own",
                "Midazolam",
                "Morphine Sulphate",
                "Naloxone Hydrochloride",
                "Oxygen",
                "Salbutamol",
                "0.9% Sodium Chloride",
                "Syntometrine",
                "Tranexamic Acid"

            ],

            'required': true,
            'flex': 20
        },
        {
            'name': "Route",
            'type': "select",
            'multiple': false,
            'selections': [
                "Subcutaneous route",
                "Oral",
                "Rectal"

            ],

            'required': true,
            'flex': 20
        },
        {
            'name': "Dose",
            'type': "select",
            'multiple': false,
            'selections': [
                "1 pen",
                "asd"

            ],

            'required': true,
            'flex': 20
        },
        {
            'name': "notes",
            'hint': "this is a textarea hint",
            'type': "textarea",
            'required': false
        }

    ];




    public news2 = [

        {
            'name': 'pulse',
            'label': 'Pulse',
            'type': 'number',
            'required': true,
            'min': 18,
            'max': 300,
            'flex': 10
        },
        {
            'name': 'systolic',
            'label': 'Systolic',
            'type': 'number',
            'required': true,
            'min': 18,
            'max': 300,
            'flex': 5
        },
        {
            'name': 'diasystolic',
            'label': 'Diasystolic',
            'type': 'number',
            'required': true,
            'min': 18,
            'max': 300,
            'flex': 5
        },
        {
            'name': 'repiratory',
            'label': 'Respiratory Rate',
            'type': 'number',
            'required': true,
            'min': 18,
            'max': 300,
            'flex': 10
        },
      {
            'name': "air",
            'type': "select",
            'multiple': false,
            'selections': [
                "Breathing room air",
                "Patient on oxygen"
            ],
          //  "default": "Mentally alert",
            'required': true,
            'flex': 20
        },
        {
            'name': "saturation",
            'label': "Oxygen",
            'type': "number",
            'required': true,
            'min': 18,
            'max': 300,
            'flex': 10
        },
        {
            'name': "temp",
            'label': "Body Temperature",
            'type': "number",
            'required': true,
            'min': 18,
            'max': 300,
            'flex': 10
        },

        {
            'name': 'ACVPU',
            'type': 'select',
            'multiple': false,
            'selections': [
                'Mentally alert',
                'Responds to voice',
                'Responds to pain',
                'Unresponsive',
                'Acute confusion'
            ],
          //  "default": "Mentally alert",
            'required': true,
    'flex': 20
        }
    ];

    title = 'ATMIST';

  constructor(private eprService: EprService) { }

  ngOnInit() {
     // this.eprService.setTitle(this.title);
  }

}
