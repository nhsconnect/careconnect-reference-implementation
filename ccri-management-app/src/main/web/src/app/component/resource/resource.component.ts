import { Component, OnInit } from '@angular/core';
import {NavigationEnd, Router} from "@angular/router";
import {FhirService} from "../../service/fhir.service";


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

  public elements = [
      {
          "name": "file-input",
          "label": "Browse a file",
          "type": "file-input"
      }
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
                  this.buildOptions(resource);
              }
          })
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
                              this.options.push(param);
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
