import {Component, OnInit, ViewChild} from '@angular/core';
import {NavigationEnd, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";
import {MatSelect} from "@angular/material";
import {ITdDynamicElementConfig, TdDynamicElement, TdDynamicFormsComponent} from "@covalent/dynamic-forms";
import {getViewData} from "@angular/core/src/render3/instructions";


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

  public resource : fhir.Bundle = undefined;

  public query = "";

  private _routerSub ;

    progressBar : boolean = false;

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
                  this.resource = undefined;
                  this.buildOptions(resource);
              }
          })
  }

  onAdd() {
    let seq :string = (this.elements.length + 1).toString(10);
   if (this.field.value !== undefined) {
     switch (this.field.value.type) {
       case 'date' :
         let nodeDSQ :ITdDynamicElementConfig = {
           "label": 'Qual',
           "name" : this.field.value.type+'-'+seq + '-1-'+ this.field.value.name,
           "type": TdDynamicElement.Select,
           "required": false,
           "selections": [
             {
               "label": "=",
               "value": "eq"
             },
             {
               "label": ">",
               "value": "gt"
             },
             {
               "label": ">=",
               "value": "ge"
             },
             {
               "label": "<",
               "value": "lt"
             },
             {
               "label": "<",
               "value": "le"
             }
           ],
           "flex" : 10
         };
         let nodeDS :ITdDynamicElementConfig = {
           "label": this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.type+'-'+seq + '-2-'+this.field.value.name ,
           "type": TdDynamicElement.Datepicker,
           "required": true,
           "flex" : 90
         };

         this.elements.push(nodeDSQ);
         this.elements.push(nodeDS);

         break;
       case 'token' :
         // add matches
         let nodeT1 :ITdDynamicElementConfig = {
           "label": 'System - '+this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.type+'-'+seq + '-1-'+this.field.value.name,
           "type": TdDynamicElement.Input,
           "flex" : 50
         };
         let nodeT2 :ITdDynamicElementConfig = {
           "label": 'Code - '+this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.type+'-'+seq + '-2-'+this.field.value.name,
           "type": TdDynamicElement.Input,
           "flex" : 50
         };
         this.elements.push(nodeT1);
         this.elements.push(nodeT2);
         break;
       case 'string' :
         // add matches
         let nodeOpt: ITdDynamicElementConfig = {
           "label": "match",
           "name": this.field.value.type+'-'+seq + '-1-'+this.field.value.name,
           "type": TdDynamicElement.Select,
           "selections": [
           {
             "label": "Matches",
             "value": ""
           },
           {
             "label": "Exactly",
             "value": "exact"
           }
         ],
           "required": true
           ,"flex" : 20
         };

         let nodeS :ITdDynamicElementConfig = {
           "label": this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.type+'-'+seq + '-2-'+this.field.value.name,
           "type": TdDynamicElement.Input,
           "required": true,
           "flex" : 80
         };
          this.elements.push(nodeOpt);
         this.elements.push(nodeS);
         break;
       case 'reference' :
         let nodeR :ITdDynamicElementConfig = {
           "label": this.field.value.name + ' - '+this.field.value.documentation,
           "name" : this.field.value.type+'-'+seq + '-1-'+this.field.value.name,
           "type": TdDynamicElement.Input,
           "required": true,
         };
         this.elements.push(nodeR);
         break;

       default:
         console.log('MISSING - '+this.field.value.type);
     }
     console.log('call refresh');
     this.form.refresh();
   }
    
  }

  onSearch() {
    var i:number;
    this.resource = undefined;
    let first : boolean = true;
    this.progressBar = true;
    let query = this.fhirSrv.getFHIRServerBase()+'/'+this.currentResource+'?'

    for(i = 0;i<this.elements.length;i++) {

      let name = this.elements[i].name;
      let content: string[]=name.split('-');
      let param = content[3];
    if (content.length>4 && (content[4]!=undefined)) param = param + '-' + content[4];

      if (!first) query = query +'&'+param
      else query=query + param;
      console.log(content[0]);
      switch (content[0]) {
        case 'date':
          query=query+'=';
          if (this.form.value[this.elements[i].name] !== undefined && this.form.value[this.elements[i].name] !== '') query = query +this.form.value[this.elements[i].name];
          if (this.form.value[this.elements[i+1].name] !== undefined) query = query +this.form.value[this.elements[i+1].name].format("YYYY-MM-DD");
          i++;
          break;
        case 'token':
          query=query+'=';
          if (this.form.value[this.elements[i].name] !== undefined) query = query +this.form.value[this.elements[i].name]+"|";
          if (this.form.value[this.elements[i+1].name] !== undefined) query = query +this.form.value[this.elements[i+1].name];
          i++;
          break;
        case 'string':
          if (this.form.value[this.elements[i].name] !== undefined) query = query +":"+this.form.value[this.elements[i].name];
          query=query+'=';
          if (this.form.value[this.elements[i+1].name] !== undefined) query = query +this.form.value[this.elements[i+1].name];
          i++;
          break;
        case 'reference':
          query=query+'=';
          if (this.form.value[this.elements[i].name] !== undefined) query = query +this.form.value[this.elements[i].name]
          break;

      }


      console.log(this.form.value[this.elements[i].name]);
      first = false;
    }
    console.log(query);
    this.query = query;
    this.fhirSrv.getResults(query).subscribe(bundle => {
      this.resource = bundle;
      this.progressBar = false;
    },
        ()=> {
        this.progressBar = false;
        })
  }


  buildOptions(resource : string) {

      if (this.fhirSrv.conformance != undefined ) {
          if (this.currentResource !== resource) {
              this.currentResource = resource;
              this.options = [];
              if (this.fhirSrv.conformance.rest !== undefined) {
                  for(let node of this.fhirSrv.conformance.rest) {

                      for (let resourceSrc of node.resource) {
                          if (resourceSrc.type === resource) {
                              console.log(resourceSrc.type);
                              if (resourceSrc.searchParam !== undefined) {
                                  for (let param of resourceSrc.searchParam) {
                                      let menuOpt: QueryOptions = {
                                          name: param.name,
                                          documentation: param.documentation,
                                          type: param.type
                                      };
                                      this.options.push(menuOpt);
                                  }
                              }
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
