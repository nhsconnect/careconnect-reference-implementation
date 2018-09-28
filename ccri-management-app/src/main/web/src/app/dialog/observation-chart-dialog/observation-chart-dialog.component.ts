import {Component, Inject, Input, OnInit} from '@angular/core';
import {TdDigitsPipe} from "@covalent/core";
import {MAT_DIALOG_DATA, MatDialog} from "@angular/material";
import {FhirService} from "../../service/fhir.service";

@Component({
  selector: 'app-observation-chart-dialog',
  templateUrl: './observation-chart-dialog.component.html',
  styleUrls: ['./observation-chart-dialog.component.css']
})
export class ObservationChartDialogComponent implements OnInit {




    // options
    showXAxis: boolean = true;
    showYAxis: boolean = true;
    gradient: boolean = true;
    showLegend: boolean = false;
    showXAxisLabel: boolean = true;
    xAxisLabel: string = '';
    showYAxisLabel: boolean = true;
    yAxisLabel: string = 'Sales';

    colorScheme: any = {
        domain: ['#1565C0', '#03A9F4', '#FFA726', '#FFCC80'],
    };

    // line, area
    autoScale: boolean = true;


    multi: any = [
        {
            'name': 'Databases',
            'series': [
                {
                    'value': 2469,
                    'name': '2016-09-15T19:25:07.773Z',
                },
                {
                    'value': 3619,
                    'name': '2016-09-17T17:16:53.279Z',
                },
                {
                    'value': 3885,
                    'name': '2016-09-15T10:34:32.344Z',
                },
                {
                    'value': 4289,
                    'name': '2016-09-19T14:33:45.710Z',
                },
                {
                    'value': 3309,
                    'name': '2016-09-12T18:48:58.925Z',
                },
            ],
        },
        {
            'name': 'Containers',
            'series': [
                {
                    'value': 2452,
                    'name': '2016-09-15T19:25:07.773Z',
                },
                {
                    'value': 4938,
                    'name': '2016-09-17T17:16:53.279Z',
                },
                {
                    'value': 4110,
                    'name': '2016-09-15T10:34:32.344Z',
                },
                {
                    'value': 3828,
                    'name': '2016-09-19T14:33:45.710Z',
                },
                {
                    'value': 5772,
                    'name': '2016-09-12T18:48:58.925Z',
                },
            ],
        },
        {
            'name': 'Streams',
            'series': [
                {
                    'value': 4022,
                    'name': '2016-09-15T19:25:07.773Z',
                },
                {
                    'value': 2345,
                    'name': '2016-09-17T17:16:53.279Z',
                },
                {
                    'value': 5148,
                    'name': '2016-09-15T10:34:32.344Z',
                },
                {
                    'value': 6868,
                    'name': '2016-09-19T14:33:45.710Z',
                },
                {
                    'value': 5415,
                    'name': '2016-09-12T18:48:58.925Z',
                },
            ],
        },
        {
            'name': 'Queries',
            'series': [
                {
                    'value': 6194,
                    'name': '2016-09-15T19:25:07.773Z',
                },
                {
                    'value': 6585,
                    'name': '2016-09-17T17:16:53.279Z',
                },
                {
                    'value': 6857,
                    'name': '2016-09-15T10:34:32.344Z',
                },
                {
                    'value': 2545,
                    'name': '2016-09-19T14:33:45.710Z',
                },
                {
                    'value': 5986,
                    'name': '2016-09-12T18:48:58.925Z',
                },
            ],
        },
    ];

@Input()
observation : fhir.Observation;

  constructor(public dialog: MatDialog,
              @Inject(MAT_DIALOG_DATA) data,
              public fhirService : FhirService) {

    this.observation = data.observation;

    console.log(data.observation);

    let obs : string[] = this.observation.subject.reference.split('/');

      this.fhirService.get('Observation?patient='+obs[1]+'&code='+this.observation.code.coding[0].code).subscribe(
          observation => {
              console.log(observation.id);
          }
      );
      // Chart Multi
      /*
      this.multi = multi.map((group: any) => {
          group.series = group.series.map((dataItem: any) => {
              dataItem.name = new Date(dataItem.name);
              return dataItem;
          });
          return group;
      });
      */
  }

  ngOnInit() {



  }

    axisDigits(val: any): any {
        return new TdDigitsPipe().transform(val);
    }

}
