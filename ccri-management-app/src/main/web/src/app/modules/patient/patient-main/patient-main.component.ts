import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FhirService} from "../../../service/fhir.service";
import {EprService} from "../../../service/epr.service";
import {MatChip} from '@angular/material';
import {AuthService} from "../../../service/auth.service";
import {Oauth2Service} from "../../../service/oauth2.service";

@Component({
  selector: 'app-patient-details',
  templateUrl: './patient-main.component.html',
  styleUrls: ['./patient-main.component.css']
})
export class PatientMainComponent implements OnInit {

    patient: fhir.Patient = undefined;

    sidenavopen = false;

    yascolor = 'info';
    acutecolor = 'info';
    gpcolor = 'info';
    nrlscolor = 'info';

    bscolour= 'accent';
    bocolour = 'info';
    becolour= 'info';
    bdcolour = 'info';
    bpcolour = 'info';
    btcolour = 'info';
    bicolour = 'info';
    bmcolour = 'info';
    brcolour = 'info';
    aacolour = 'info';

    cards : any[] = [];

     @ViewChild('gpchip') gpchip : MatChip;




  constructor(private router : Router,
              private fhirSrv: FhirService,
              private route: ActivatedRoute,
              private eprService : EprService,
              private authService : AuthService,
              private oauth2 : Oauth2Service) { }

  ngOnInit() {

      let patientid = this.route.snapshot.paramMap.get('patientid');
         this.eprService.setTitle('Health Information Exchange Portal');
      this.acutecolor = 'info';
      this.yascolor = 'info';

      this.fhirSrv.get('/Patient?_id='+patientid+'&_revinclude=Flag:patient').subscribe(bundle => {

          if (bundle.entry !== undefined) {
              for (let entry of bundle.entry) {
                 switch (entry.resource.resourceType) {
                   case 'Patient' :
                     this.patient = <fhir.Patient> entry.resource;
                     break;
                   case 'Flag':
                     this.eprService.addFlag(<fhir.Flag> entry.resource);
                 }
              }
          }

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

      if (this.oauth2.isAuthenticated()) {
        this.authService.getClients().subscribe( response => {
          //console.log(clients);
          const clients: any[] = response as any[];
          for (let client of clients) {

            if (client.scope.includes("launch")) {
              let found = false;
              for (let search of this.cards) {
                if (search.clientId === client.clientId) {
                  found = true;
                }
              }
              if (!found) {
                console.log(client);
                let newclient = {
                  id: client.id,
                  name: client.clientName,
                  image: client.logoUri,
                  url: '',
                  notes: client.clientDescription,
                  source: '',
                  clientId: client.clientId
                }
                this.addClient(newclient);
              }
            }
          }

        });
      }
  }

  addClient(client) {
    this.fhirSrv.get('/Endpoint?identifier='+client.clientId).subscribe( result => {
        let bundle: fhir.Bundle = result;
        let endpoint: fhir.Endpoint = undefined;
        if (bundle.entry !== undefined) {
          for (const entry of bundle.entry) {
            let resource: fhir.Resource = entry.resource;
            if (resource.resourceType === 'Endpoint') {
              client.endpoint = resource;
              if (client.endpoint.address !== undefined) {
                this.cards.push(client);
              }
            }
          }
        }
      },
      ()=>{

      },
      () => {

      });

  }


  onClick(event, btn) {
    //  console.log(event);
        this.bscolour= 'info';
        this.bocolour = 'info';
        this.becolour= 'info';
        this.bdcolour = 'info';
        this.bpcolour = 'info';
        this.btcolour = 'info';
        this.bicolour = 'info';
        this.bmcolour = 'info';
      this.aacolour = 'info';
      this.brcolour = 'info';
      switch (btn) {
          case 'aa':
              this.router.navigate(['atmist'], {relativeTo: this.route });
              this.aacolour = 'accent';
              break;
          case 'bs':
              this.router.navigate(['summary'], {relativeTo: this.route });
              this.bscolour = 'accent';
              break;
          case 'bo':
              this.router.navigate(['observation'], {relativeTo: this.route });
              this.bocolour = 'accent';
              break;
          case 'be':
              this.router.navigate(['encounter'], {relativeTo: this.route });
              this.becolour = 'accent';
              break;
          case 'bd':
              this.router.navigate(['document'], {relativeTo: this.route });
              this.bdcolour = 'accent';
              break;
          case 'bp':
              this.router.navigate(['procedure'], {relativeTo: this.route });
              this.bpcolour = 'accent';
              break;
          case 'bt':
              this.router.navigate(['timeline'], {relativeTo: this.route });
              this.btcolour = 'accent';
              break;
          case 'bi':
          this.router.navigate(['immunisation'], {relativeTo: this.route });
          this.bicolour = 'accent';
          break;
          case 'bm':
              this.router.navigate(['medication'], {relativeTo: this.route });
              this.bmcolour = 'accent';
              break;
          case 'br':
              this.router.navigate(['referral'], {relativeTo: this.route });
              this.brcolour = 'accent';
              break;
      }
    }

    getFirstName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";
        // Move to address
        let name = "";
        if (patient.name[0].given !== undefined && patient.name[0].given.length>0) name += ", "+ patient.name[0].given[0];

        if (patient.name[0].prefix !== undefined && patient.name[0].prefix.length>0) name += " (" + patient.name[0].prefix[0] +")" ;
        return name;

    }

    getNHSIdentifier(patient: fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.identifier == undefined || patient.identifier.length == 0)
            return "";
        // Move to address
        var NHSNumber :String = "";
        for (var f=0;f<patient.identifier.length;f++) {
            if (patient.identifier[f].system.includes("nhs-number") )
                NHSNumber = patient.identifier[f].value.substring(0,3)+ ' '+patient.identifier[f].value.substring(3,6)+ ' '+patient.identifier[f].value.substring(6);
        }
        return NHSNumber;

    }

    getLastName(patient :fhir.Patient) : String {
        if (patient == undefined) return "";
        if (patient.name == undefined || patient.name.length == 0)
            return "";

        let name = "";
        if (patient.name[0].family !== undefined) name += patient.name[0].family.toUpperCase();
        return name;

    }

  smartApp(card) {

    let launch: string = undefined;

    console.log('App Lauch '+card.url);

    if (card.url !== '') {

      this.authService.launchSMART(card.clientId, '4ae23017813e417d937e3ba21974581', this.eprService.patient.id).subscribe(response => {
          launch = response.launch_id;
          console.log("Returned Launch = " + launch);
        },
        (err) => {
          console.log(err);
        },
        () => {
          window.open(card.url + '?iss=' + this.fhirSrv.getBaseUrl() + '&launch=' + launch, '_blank');
        }
      );
    } else {
      this.fhirSrv.get('/Endpoint?identifier='+card.clientId).subscribe( result => {
        console.log(result);
        let bundle: fhir.Bundle = result;
        if (bundle.entry !== undefined) {
          for (const entry of bundle.entry) {
            let resource: fhir.Resource = entry.resource;
            if (resource.resourceType === 'Endpoint') {
              let endpoint: fhir.Endpoint = <fhir.Endpoint> resource;
              this.authService.launchSMART(card.clientId, '4ae23017813e417d937e3ba21974581', this.eprService.patient.id).subscribe(response => {
                  launch = response.launch_id;
                  console.log("Returned Launch = " + launch);
                },
                (err) => {
                  console.log(err);
                },
                () => {
                  window.open(endpoint.address + '?iss=' + this.fhirSrv.getBaseUrl() + '&launch=' + launch, '_blank');
                }
              );
            }
          }
        }
      })
    }

  }

  onToggle(event : any) {
      console.log(this.sidenavopen);

      this.sidenavopen = !this.sidenavopen;
  }

}
