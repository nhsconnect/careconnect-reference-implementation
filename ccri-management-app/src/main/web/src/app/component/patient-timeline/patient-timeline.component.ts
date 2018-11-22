import {Component, ElementRef, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import {DataSet, Timeline, TimelineOptions} from "vis";


declare var vis: any;

@Component({
  selector: 'app-patient-timeline',
  templateUrl: './patient-timeline.component.html',
  styleUrls: ['./patient-timeline.component.css']
})
export class PatientTimelineComponent implements OnInit {

  @Input() encounters: fhir.Encounter[];

  @Input() procedures: fhir.Procedure[];

  @Input() conditions: fhir.Condition[];

  @Input() patient: fhir.Patient;

  @Output() encounterId = new EventEmitter<any>();

  constructor(private elementRef: ElementRef) { }

  @ViewChild("modalContent", {read: TemplateRef}) tref ;

  public ngOnInit(): void {



    // create a network
    var container = document.getElementById('timeline');


    let names = ['Condition', 'Procedure', 'Encounter'];
    let groups = new DataSet();
    let g = 0;
    for (let name of names) {
      groups.add({id: g, content: name});
      g++;
    }

    let items = new DataSet([]);

    for (let condition of this.conditions) {
        let date = this.getConditionDate(condition);
        if (date !== undefined) {
            console.log(condition);
        if (condition.code !== undefined && condition.code.coding.length > 0) {

            items.add({
                id: 'Condition/' + condition.id,
                group: 0,
                content: condition.code.coding[0].display,
                start: date,
                className: 'green'
            });
        } else {
            items.add({
                id: 'Condition/' + condition.id,
                group: 0,
                content: 'nos',
                className: 'green',
                start: date
            });
        }
    }
    }

    for (let procedure of this.procedures) {

      let date = this.getProcedureDate(procedure);
      if (date !== undefined) {
          console.log(procedure);
          if (procedure.code !== undefined && procedure.code.coding.length > 0) {
              items.add({
                  id: 'Procedure/' + procedure.id,
                  group: 1,
                  content: procedure.code.coding[0].display,
                  start: date,
                  className: 'red'
              });
          } else {
              items.add({
                  id: 'Procedure/' + procedure.id,
                  group: 1,
                  content: 'nos',
                  className: 'red',
                  start: date
              });
          }
      }
    }

    for (let encounter of this.encounters) {
      if (encounter.period !== undefined && encounter.period.start !== undefined) {
        console.log(encounter);
          if (encounter.type !== undefined && encounter.type.length > 0) {
              items.add({
                  id: 'Encounter/' + encounter.id,
                  group: 2,
                  content: encounter.type[0].coding[0].display,
                  start: encounter.period.start
                  //,end: encounter.period.end
              });
          } else {
              items.add({
                  id: 'Encounter/' + encounter.id,
                  group: 2,
                  content: 'nos',
                  start: encounter.period.start
                  //,end: encounter.period.end
              });
          }
      }
    }


    let optiont: TimelineOptions = {
      width: '100%',
      height: '350px',
      start: '2016-05-02',
      end: '2019-01-01'
      /*,
      rollingMode : {
        follow : false,
        offset : 0.5
      } */

    };


    let timeline = new Timeline(container, items, groups, optiont);

    // Use single click. Works better on mobile devices/touch screens
    timeline.on('click', (properties) => {
      if (properties !== undefined && properties.item !== undefined) {
        let itemstr: string[] = properties.item.split('/');
        if (itemstr.length > 1) {
          if (itemstr[0] == 'Encounter') {
            //let content =  this.tref.nativeElement.textContent;
            console.log(this.tref);
            console.log('Encounter Selected');
            this.encounterId.emit(itemstr[1]);
           // this.modalService.open(this.tref, {windowClass: 'dark-modal'});
          }
        }
        //console.log('clicked '+properties.item)
      }
    });
  }
/*
    timeline.on('doubleClick', (properties) => {
      if (properties !== undefined && properties.item !== undefined) {
        let itemstr: string[] = properties.item.split('/');
        if (itemstr.length>1) {
          if (itemstr[0] == 'Encounter') {
            //let content =  this.tref.nativeElement.textContent;
            console.log(this.tref);
            console.log('Encounter Selected');
            this.encounterId = itemstr[1];
           this.modalService.open(this.tref, {windowClass: 'dark-modal'});
          }
        }
        //console.log('clicked '+properties.item)
        }

    });

  }
*/
  getProcedureDate(procedure: fhir.Procedure) {
    if (procedure.performedDateTime != null) return procedure.performedDateTime;
    if (procedure.performedPeriod != null) return procedure.performedPeriod.start;
  }
  getConditionDate(condition: fhir.Condition) {
    if (condition.onsetDateTime !== undefined) return condition.onsetDateTime;
    if (condition.assertedDate !=undefined) return condition.assertedDate;
  }

}
