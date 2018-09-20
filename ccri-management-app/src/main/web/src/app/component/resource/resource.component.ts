import {Component, OnInit, ViewChild} from '@angular/core';
import {NavigationEnd, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {MatSelect} from "@angular/material";
import {ITdDynamicElementConfig, TdDynamicElement, TdDynamicFormsComponent} from "@covalent/dynamic-forms";


export interface QueryOptions {
    name: string;
    type: string;
    documentation : string;
}

/*

name:
"date"
type:
"date"
documentation:
"Date record was believed accurate"
 */


@Component({
  selector: 'app-resource',
  templateUrl: './resource.component.html',
  styleUrls: ['./resource.component.css']
})
export class ResourceComponent implements OnInit {

  constructor(private router : Router, private fhirSrv : FhirService) { }

  public resource = '{}';

  private _routerSub ;

  private currentResource : string = "";

  @ViewChild('field') field : MatSelect;

  @ViewChild('form') form : TdDynamicFormsComponent;

  public elements :ITdDynamicElementConfig[] = [

  ];

   public selectedValue: string;

   public options: QueryOptions[] = [

    ];

  ngOnInit() {
      console.log('Resource Init called'+ this.router.url);
      if (this.router.url.startsWith('/resource')) {
          console.log(' = Init Build');
          let resource = this.router.url.replace('/resource/','');
          this.buildOptions(resource);
      }

      this._routerSub = this.router.events
      // Here is one extra parenthesis it's missing in your code
          .subscribe( event  => {
              if ((event instanceof NavigationEnd) && (event.url.startsWith('/resource'))) {
                  console.log(' + NavChange '+event.url);
                  let resource = event.url.replace('/resource/','');
                  this.elements=[];
                  this.buildOptions(resource);
              }
          })
  }

  onAdd() {

   if (this.field.value !== undefined) {
     switch (this.field.value.type) {
       case 'date' :
         let nodeDS :ITdDynamicElementConfig = {
           "label": 'From '+this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name + '-s',
           "type": TdDynamicElement.Datepicker,
           "required": false,
           "flex" : 50
         };
         let nodeDE :ITdDynamicElementConfig = {
           "label": 'To '+this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name+ '-e',
           "type": TdDynamicElement.Datepicker,
           "required": false,
           "flex" : 50
         };
         this.elements.push(nodeDS);
         this.elements.push(nodeDE);
         break;
       case 'token' :
         // add matches
         let nodeT1 :ITdDynamicElementConfig = {
           "label": 'System - '+this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name+'-system',
           "type": TdDynamicElement.Input,
           "flex" : 50
         };
         let nodeT2 :ITdDynamicElementConfig = {
           "label": 'Code - '+this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name+'-code',
           "type": TdDynamicElement.Input,
           "flex" : 50
         };
         this.elements.push(nodeT1);
         this.elements.push(nodeT2);
         break;
       case 'string' :
         // add matches
         let nodeS :ITdDynamicElementConfig = {
           "label": this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name,
           "type": TdDynamicElement.Input,
           "required": true,
         };
         this.elements.push(nodeS);
         break;
       case 'reference' :
         let nodeR :ITdDynamicElementConfig = {
           "label": this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name,
           "type": TdDynamicElement.Input,
           "required": true,
         };
         this.elements.push(nodeR);
         break;
       case 'token' :
         let node :ITdDynamicElementConfig = {
           "label": this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.name,
           "type": TdDynamicElement.Input,
           "required": true,
         };
         this.elements.push(node);
         break;
       default:
         console.log('MISSING - '+this.field.value.type);
     }
     console.log('call refresh');
     this.form.refresh();
   }

  }

  buildOptions(resource : string) {

      if (this.fhirSrv.conformance != undefined ) {
          if (this.currentResource !== resource) {
              this.currentResource = resource;
              this.options = [];
              for(let node of this.fhirSrv.conformance.rest) {

                  for (let resourceSrc of node.resource) {
                      if (resourceSrc.type === resource) {
                          console.log(resourceSrc.type);
                          for (let param of resourceSrc.searchParam) {
                              let menuOpt : QueryOptions = {
                                name : param.name,
                                documentation : param.documentation,
                                type : param.type
                              };
                              this.options.push(menuOpt);
                          }
                      }
                  }
              }
          }
      } else {
          this.router.navigateByUrl('/');
      }

  }

}
