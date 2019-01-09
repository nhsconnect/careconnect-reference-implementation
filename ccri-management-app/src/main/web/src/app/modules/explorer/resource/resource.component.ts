import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {FhirService, Formats} from '../../../service/fhir.service';
import {MatSelect} from '@angular/material';
import {ITdDynamicElementConfig, TdDynamicElement, TdDynamicFormsComponent} from '@covalent/dynamic-forms';



export interface QueryOptions {
    name: string;
    type: string;
    documentation: string;
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
export class ResourceComponent implements OnInit, AfterViewInit {


    public resource: fhir.Bundle = undefined;

    public resourceString: any = undefined;

    public query = undefined;
    
    public id_query = undefined;
    

    public rest: any;

    private _routerSub ;

    public base: string;

    public format: Formats;

    progressBar = false;

    searchVisible = false;

    expanded = false;

    entries: any[];

    allergies: fhir.AllergyIntolerance[];
    carePlans: fhir.CarePlan[];
    consents: fhir.Consent[];
    impressions: fhir.ClinicalImpression[];
    conditions: fhir.Condition[];
    documents: fhir.DocumentReference[];
    encounters: fhir.Encounter[];
    goals: fhir.Goal[];
    services: fhir.HealthcareService[];
    immunisations: fhir.Immunization[];
    locations: fhir.Location[];
    medications: fhir.Medication[];
    medicationAdministrations: fhir.MedicationAdministration[];
    medicationStatements: fhir.MedicationStatement[];
    medicationDispenses: fhir.MedicationDispense[];
    observations: fhir.Observation[];
    organisations: fhir.Organization[];

    prescriptions: fhir.MedicationRequest[];
    forms: fhir.QuestionnaireResponse[];

    procedures: fhir.Procedure[];
    patients: fhir.Patient[];
    practitioners: fhir.Practitioner[];

    referrals: fhir.ReferralRequest[];
    risks: fhir.RiskAssessment[];
    roles: fhir.PractitionerRole[];

  public currentResource = '';

  @ViewChild('field') field: MatSelect;

  @ViewChild('dynform') form: TdDynamicFormsComponent;
  @ViewChild('dynform1') form1 : TdDynamicFormsComponent;
  public elements :ITdDynamicElementConfig[] = [
 ];

	public elements_id :ITdDynamicElementConfig[] = [
  {
      label: '',
      name: 'patientid',
      type: TdDynamicElement.Input,
      required: true
    }

    ];
   public selectedValue: string;

   public options: QueryOptions[] = [

    ];


    constructor(private router: Router, private fhirSrv: FhirService,  private route: ActivatedRoute) { }

  ngOnInit() {
     // console.log('Resource Init called'+ this.router.url);

    this.clearDown();


      const resource = this.route.snapshot.paramMap.get('resourceType');

      if (this.fhirSrv.conformance !== undefined ) {
          this.buildOptions(resource);
      } else {
          this.fhirSrv.getConformance();

          this.fhirSrv.getConformanceChange().subscribe(
              conformance => {
                  if (this.fhirSrv.conformance !== undefined) {
                      this.buildOptions(resource);
                  }
              }
          );
      }

      this.route.url.subscribe( url => {
          // console.log('activated route url ='+url);
          // console.log('activated route segment ='+url[0]);
          if (url[0].path === 'resource') {
              const resourceType = this.route.snapshot.paramMap.get('resourceType');
              this.resource = undefined;
              this.resourceString = undefined;
              this.query = undefined;
              this.clearDown();
              this.onClear();
              this.buildOptions(resourceType);
          }
      });


      this.format = this.fhirSrv.getFormat();

      this.fhirSrv.getFormatChange().subscribe( format => {
          this.format = format;
          this.getResults();
      });

/*
      this._routerSub = this.router.events
      // Here is one extra parenthesis it's missing in your code
          .subscribe( event  => {
             // console.log('router event');
              if ((event instanceof NavigationEnd) && (event.url.startsWith('/resource'))) {
                  console.log(' + NavChange '+event.url);
                  let resource = event.url.replace('/resource/','');
                  this.elements=[];
                  if (this.form !== undefined) {
                      this.form.form.valueChanges.subscribe((val) => {
                          this.buildQuery();
                      })
                  }
                  this.resource = undefined;
                  this.resourceString = undefined;
                  this.query = undefined;
                  this.clearDown();
                  this.buildOptions(resource);
              }
          }) */
  }

  clearDown() {
    this.entries = [];
    this.medicationStatements = [];
    this.medicationDispenses = [];
      this.medicationAdministrations = [];
    this.prescriptions = [];
    this.medications = [];
    this.conditions = [];
    this.procedures = [];
    this.observations = [];
    this.encounters = [];
    this.allergies = [];
    this.patients = [];
    this.practitioners = [];
    this.organisations = [];
    this.roles = [];
    this.services = [];
    this.immunisations = [];
    this.forms = [];
    this.risks = [];
    this.goals = [];
    this.impressions = [];
    this.consents = [];
    this.carePlans = [];
    this.documents = [];
    this.referrals = [];
  }

  onExpand() {
      this.expanded = true;
  }

  onCollapse() {
      this.expanded = false;
  }

  onAdd(param) {
    const seq: string = (this.elements.length + 1).toString(10);
   if (param !== undefined) {
     switch (param.type) {
       case 'date' :
         const nodeDSQ: ITdDynamicElementConfig = {
           'label': 'Qual',
           'name' : param.type + '-' + seq + '-1-' + param.name,
           'type': TdDynamicElement.Select,
           'required': false,
           'selections': [
             {
               'label': '=',
               'value': 'eq'
             },
             {
               'label': '>',
               'value': 'gt'
             },
             {
               'label': '>=',
               'value': 'ge'
             },
             {
               'label': '<',
               'value': 'lt'
             },
             {
               'label': '<',
               'value': 'le'
             }
           ],
           'flex' : 10
         };
         const nodeDS: ITdDynamicElementConfig = {
           'label': param.name + ' - ' + param.documentation,
           'name' : param.type + '-' + seq + '-2-' + param.name ,
           'type': TdDynamicElement.Datepicker,
           'required': true,
           'flex' : 90
         };

         this.elements.push(nodeDSQ);
         this.elements.push(nodeDS);

         break;
       case 'token' :
         // add matches
         const nodeT1: ITdDynamicElementConfig = {
           'label': 'System - ' + param.name + ' - ' + param.documentation,
           'name' : param.type + '-' + seq + '-1-' + param.name,
           'type': TdDynamicElement.Input,
           'flex' : 50
         };
         const nodeT2: ITdDynamicElementConfig = {
           'label': 'Code - ' + param.name + ' - ' + param.documentation,
           'name' : param.type + '-' + seq + '-2-' + param.name,
           'type': TdDynamicElement.Input,
             'required': true,
           'flex' : 50
         };
         this.elements.push(nodeT1);
         this.elements.push(nodeT2);
         break;
       case 'string' :
         // add matches
         const nodeOpt: ITdDynamicElementConfig = {
           'label': 'match',
           'name': param.type + '-' + seq + '-1-' + param.name,
           'type': TdDynamicElement.Select,
           'selections': [
           {
             'label': 'Matches',
             'value': ''
           },
           {
             'label': 'Exactly',
             'value': 'exact'
           }
         ],
           'required': false,
           'flex': 20
         };

         const nodeS: ITdDynamicElementConfig = {
           'label': param.name + ' - ' + param.documentation,
           'name' : param.type + '-' + seq + '-2-' + param.name,
           'type': TdDynamicElement.Input,
           'required': true,
           'flex' : 80
         };
          this.elements.push(nodeOpt);
         this.elements.push(nodeS);
         break;
       case 'reference' :
         const nodeR: ITdDynamicElementConfig = {
           'label': param.name + ' - ' + param.documentation,
           'name' : param.type + '-' + seq + '-1-' + param.name,
           'type': TdDynamicElement.Input,
           'required': true,
         };
         this.elements.push(nodeR);
         break;

       default:
         console.log('MISSING - ' + param.type);
     }
     console.log('call refresh');
     this.form.refresh();
     this.buildQuery();
   }
  }

  onMore(linkUrl: string) {
      this.progressBar = true;
      this.clearDown();
      this.fhirSrv.getResults(linkUrl).subscribe(bundle => {
          switch ( this.format ) {
            case 'jsonf':
              this.resource = bundle;
              break;
            case 'json' :
              this.resource = bundle;
              this.resourceString = JSON.stringify(bundle, null, 2);
              break;
            case 'epr' :
              this.resource = bundle;
              this.getResources();
              break;
            case 'xml':
              const reader = new FileReader();
              reader.addEventListener('loadend', (e) => {
                this.resourceString = reader.result;
              });
              reader.readAsText(<Blob> bundle);
          }
          this.progressBar = false;
          },
          () => {
              this.progressBar = false;
          });
  }

    ngAfterViewInit() {
      console.log('after init');

        if (this.form !== undefined) {
            this.form.form.valueChanges.subscribe((val) => {
                this.buildQuery();
            });
        }
    }

  buildQuery() {
      let i: number;
      let first = true;
      let query = this.fhirSrv.getFHIRServerBase() + '/' + this.currentResource + '?';

      for (i = 0; i < this.elements.length; i++) {

          const name = this.elements[i].name;
          const content: string[] = name.split('-');
          let param = content[3];
          if (content.length > 4 && (content[4] !== undefined)) { param = param + '-' + content[4]; }

          if (!first) {
              query = query + '&' + param;
          } else { query = query + param; }
          //  console.log(content[0]);
          switch (content[0]) {
              case 'date':
                  query = query + '=';
                  if (this.form.value[this.elements[i].name] !== undefined && this.form.value[this.elements[i].name] !== '') {
                      query = query + this.form.value[this.elements[i].name]; }
                  if (this.form.value[this.elements[i + 1].name] !== undefined) {
                      query = query + this.form.value[this.elements[i + 1].name].format('YYYY-MM-DD'); }
                  i++;
                  break;
              case 'token':
                  query = query + '=';
                  if (this.form.value[this.elements[i].name] !== undefined) {
                      query = query + this.form.value[this.elements[i].name] + '%7C'; }
                  if (this.form.value[this.elements[i + 1].name] !== undefined) {
                      query = query + this.form.value[this.elements[i + 1].name]; }
                  i++;
                  break;
              case 'string':
                  if (this.form.value[this.elements[i].name] !== undefined) {
                      query = query + ':' + this.form.value[this.elements[i].name]; }
                  query = query + '=';
                  if (this.form.value[this.elements[i + 1].name] !== undefined) {
                      query = query + this.form.value[this.elements[i + 1].name]; }
                  i++;
                  break;
              case 'reference':
                  query = query + '=';
                  if (this.form.value[this.elements[i].name] !== undefined) {
                      query = query + this.form.value[this.elements[i].name]; }
                  break;

          }


          console.log(this.form.value[this.elements[i].name]);
          first = false;
      }
      //  console.log(query);
      this.query = query;
  }
  onSearch() {

    this.clearDown();
      console.log(this.form.valid);
      if (this.form.valid && this.elements.length > 0) {
          this.resource = undefined;
          this.progressBar = true;
          this.buildQuery();
          this.getResults();
      }
  }
    onSearch_id() {

        if (this.form1.valid && this.elements_id.length > 0) {
            this.resource = undefined;
           // console.log(this.elements_id[1].value);
            this.progressBar = true;
            
            let id_query = this.fhirSrv.getFHIRServerBase() + '/' + this.currentResource + '/' + + this.form1.value[this.elements_id[0].name];
            this.id_query = id_query;
            console.log(id_query);
            this.query = id_query;
            this.getResults();
            console.log(id_query);
        }
    }
  onClear() {
      this.elements = [];
      if (this.form !== undefined) {
        this.form.refresh();
      }
  }

  getResults() {
      if (this.query !== undefined && (this.query !== '')) {
          console.log(this.format + ' Query = ' + this.query);
            this.fhirSrv.getResults(this.query).subscribe(bundle => {
                  switch (this.format) {
                      case 'jsonf':
                          this.resource = bundle;
                          break;
                      case 'json' :
                          this.resource = bundle;
                          this.resourceString = JSON.stringify(bundle, null, 2);
                          break;
                      case 'epr' :
                        this.resource = bundle;
                        this.getResources();
                        break;
                      case 'xml':
                          const reader = new FileReader();
                          reader.addEventListener('loadend', (e) => {
                              this.resourceString = reader.result;
                          });
                          reader.readAsText(<Blob> bundle);
                  }
                  this.progressBar = false;
              },
              () => {
                  this.progressBar = false;
              });
      }
  }
  


  buildOptions(resource: string) {
      this.searchVisible = false;
      if (this.fhirSrv.conformance !== undefined ) {
          if (this.currentResource !== resource) {
              this.currentResource = resource;
              this.base = this.fhirSrv.getFHIRServerBase() + '/' + this.currentResource;
              this.options = [];
              if (this.fhirSrv.conformance.rest !== undefined) {
                  for (const node of this.fhirSrv.conformance.rest) {

                      for (const resourceSrc of node.resource) {
                          if (resourceSrc.type === resource) {
                             // console.log(resourceSrc.type);
                              this.rest = resourceSrc;
                              if (resourceSrc.searchParam !== undefined) {
                                  this.searchVisible = true;
                                  for (const param of resourceSrc.searchParam) {
                                      const menuOpt: QueryOptions = {
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
          console.log('In Resource - Forcing naviagation to root');
          // this.router.navigateByUrl('/');
      }

  }

  getResources()  {


    if (this.resource.entry !== undefined) {
      for (const entry  of this.resource.entry) {

        const resource = entry.resource;

          switch (resource.resourceType) {
            case 'AllergyIntolerance' :
              const allergyIntolerance: fhir.AllergyIntolerance = <fhir.AllergyIntolerance> resource;
              this.allergies.push(allergyIntolerance);
              break;
            case 'CarePlan' :
              const carePlan: fhir.CarePlan = <fhir.CarePlan> resource;
              this.carePlans.push(carePlan);
              break;
            case 'Consent' :
                const consent: fhir.Consent = <fhir.Consent> resource;
              this.consents.push(consent);
              break;
            case 'ClinicalImpression' :
                const clinicalImpression: fhir.ClinicalImpression = <fhir.ClinicalImpression> resource;
              this.impressions.push(clinicalImpression);
              break;
            case 'Condition' :
                const condition: fhir.Condition = <fhir.Condition> resource;
              this.conditions.push(condition);
              break;
              case 'DocumentReference' :
                  const document: fhir.DocumentReference = <fhir.DocumentReference> resource;
                  this.documents.push(document);
                  break;
            case 'Encounter' :
                const encounter: fhir.Encounter = <fhir.Encounter> resource;
              this.encounters.push(encounter);
              break;
            case 'Goal':
                const goal: fhir.Goal = <fhir.Goal> resource;

              this.goals.push(goal);
              break;
            case 'HealthcareService':
                const service: fhir.HealthcareService = <fhir.HealthcareService> resource;
              this.services.push(service);
              break;
            case 'Immunization' :
                const immunisation: fhir.Immunization = <fhir.Immunization> resource;
              this.immunisations.push(immunisation);
              break;
            case 'Location':
                const location: fhir.Location = <fhir.Location> resource;

              this.locations.push(location);
              break;
            case 'List' :
                const list: fhir.List = <fhir.List> resource;
              if (list.entry !== undefined) {
                if (list.code !== undefined && list.code.coding.length > 0) {
                  this.entries.push({
                    'resource': 'List'
                    , 'code': list.code.coding[0].code
                    , 'display': 'Entries ' + list.entry.length
                  });
                } else {
                  this.entries.push({
                    'resource': 'List'
                    , 'display': 'Entries ' + list.entry.length
                  });
                }


              }
              break;
            case 'Medication' :
                const medication: fhir.Medication = <fhir.Medication> resource;
              this.medications.push(medication);
              break;
              case 'MedicationAdministration' :
                  const medicationAdministration: fhir.MedicationAdministration = <fhir.MedicationAdministration> resource;
                  this.medicationAdministrations.push(medicationAdministration);
                  break;
            case 'MedicationRequest' :
                const medicationRequest: fhir.MedicationRequest = <fhir.MedicationRequest> resource;
              this.prescriptions.push(medicationRequest);
              break;
            case 'MedicationDispense' :
                const medicationDispense: fhir.MedicationDispense = <fhir.MedicationDispense> resource;
              this.medicationDispenses.push(medicationDispense);
              break;
            case 'MedicationStatement' :
                const medicationStatement: fhir.MedicationStatement = <fhir.MedicationStatement> resource;
              this.medicationStatements.push(medicationStatement);
              break;
            case 'Observation' :
                const observation: fhir.Observation = <fhir.Observation> resource;
              this.observations.push(observation);
              break;
            case 'Procedure' :
                const procedure: fhir.Procedure = <fhir.Procedure> resource;
              this.procedures.push(procedure);
              break;
            case 'Patient' :
                const patient: fhir.Patient = <fhir.Patient> resource;
              this.patients.push(patient);
              break;
            case 'Practitioner':
                const practitioner: fhir.Practitioner = <fhir.Practitioner> resource;
              this.practitioners.push(practitioner);
              break;
            case 'PractitionerRole':
                const practitionerRole: fhir.PractitionerRole = <fhir.PractitionerRole> resource;
              this.roles.push(practitionerRole);
              break;
            case 'Organization':
                const organization: fhir.Organization = <fhir.Organization> resource;
              this.organisations.push(organization);
              break;
            case 'QuestionnaireResponse' :
                const form: fhir.QuestionnaireResponse = <fhir.QuestionnaireResponse> resource;
              this.forms.push(form);
              break;
              case 'ReferralRequest':
                  console.log('Referral Request');
                  const referral: fhir.ReferralRequest = <fhir.ReferralRequest> resource;
                  this.referrals.push(referral);
                  break;
            case 'RiskAssessment':
              const risk: fhir.RiskAssessment = <fhir.RiskAssessment> resource;
              this.risks.push(risk);
              break;

            default :
              console.log('**** missing ' + resource.resourceType);
              this.entries.push(resource.resourceType);
          }
        }
      }



  }

    onResoureSelected(event) {

  }

}
