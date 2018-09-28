import {Component, Inject, Input, OnInit, ViewChild} from '@angular/core';
import {TdDigitsPipe} from "@covalent/core";
import {MAT_DIALOG_DATA, MatDialog, MatSelect} from "@angular/material";
import {FhirService} from "../../service/fhir.service";
import {NgxChartsModule} from "@swimlane/ngx-charts";

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
    timeline:boolean = false;
    yAxisLabel: string = 'Value';
    animations: boolean = false;

    colorScheme: any = {
        domain: ['#1565C0', '#03A9F4', '#FFA726', '#FFCC80'],
    };

    // line, area
    autoScale: boolean = true;

    title = 'Observation Chart';

    multi: any = [
        {
            'name': 'Values',
            'series': [
                {
                    'value': 2469,
                    'name': '2016-09-15T19:25:07.773Z',
                }
            ],
        }

    ];

@Input()
observation : fhir.Observation;

    @ViewChild('chart') chart : NgxChartsModule;


  constructor(public dialog: MatDialog,
              @Inject(MAT_DIALOG_DATA) data,
              public fhirService : FhirService) {

    this.observation = data.resource;

  }

  ngOnInit() {

      console.log(this.observation);

      let obs : string[] = this.observation.subject.reference.split('/');
      this.fhirService.get('/Observation?patient='+obs[1]+'&code='+this.observation.code.coding[0].code+"&_count=200").subscribe(
          bundle => {
                    console.log(bundle);
                  let observations : fhir.Bundle = <fhir.Bundle> bundle;
                  this.title = this.observation.code.coding[0].display;
                  this.yAxisLabel='Value';
                  let multi : any[] = [];

                  if (this.observation.valueQuantity !== undefined) {
                      this.yAxisLabel=this.observation.valueQuantity.unit;
                      multi.push({
                          'name': this.observation.code.coding[0].display,
                          'series': [
                          ],
                      })
                  }
                  if (this.observation.component !== undefined) {
                      this.yAxisLabel=this.observation.component[0].valueQuantity.unit;
                      for (let component of this.observation.component) {
                          multi.push(
                              {
                                  'name': component.code.coding[0].display,
                                  'series': [],
                              }
                          );
                      }
                  }

                  for (let entry of observations.entry) {
                      if (entry.resource.resourceType === 'Observation') {
                          let observation: fhir.Observation = <fhir.Observation> entry.resource;

                          if (observation.component === undefined || observation.component.length < 0) {
                              if (observation.valueQuantity.value !== undefined) {
                                  if (observation.effectiveDateTime !== undefined) {
                                      console.log(observation.effectiveDateTime);
                                      multi[0].series.push({
                                          'value': observation.valueQuantity.value,
                                          'name': new Date(observation.effectiveDateTime)
                                      });
                                  }
                                  if (observation.effectivePeriod !== undefined) {
                                      console.log(observation.effectivePeriod.start);
                                      multi[0].series.push({
                                          'value': observation.valueQuantity.value,
                                          'name': new Date(observation.effectivePeriod.start)
                                      });
                                  }
                              }
                          } else {
                              for (let component of observation.component) {

                                  let seriesId = undefined;
                                  let cont = 0;
                                  for (; cont < multi.length; cont++) {
                                      console.log('series name = ' + multi[cont].name);
                                      console.log('component name ' + component.code.coding[0].display);
                                      if (multi[cont].name === component.code.coding[0].display) {
                                          seriesId = cont;
                                      }
                                  }
                                  if (seriesId !== undefined) {
                                      if (observation.effectiveDateTime !== undefined) {
                                          console.log(observation.effectiveDateTime);
                                          multi[seriesId].series.push({
                                              'value': component.valueQuantity.value,
                                              'name': new Date(observation.effectiveDateTime)
                                          });
                                      }
                                      if (observation.effectivePeriod !== undefined) {
                                          console.log(observation.effectivePeriod.start);
                                          multi[seriesId].series.push({
                                              'value': component.valueQuantity.value,
                                              'name': new Date(observation.effectivePeriod.start)
                                          });
                                      }
                                  }
                              }
                          }
                      }



                  }
                  this.multi = multi;
                  console.log(this.multi);

          }
      );


  }

    axisDigits(val: any): any {
        return new TdDigitsPipe().transform(val);
    }

}
