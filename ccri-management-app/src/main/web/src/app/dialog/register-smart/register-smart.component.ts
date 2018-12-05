import {Component, Inject, Input, OnInit, ViewContainerRef} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {FhirService} from '../../service/fhir.service';

import {RegisterSmartSecretComponent} from "../register-smart-secret/register-smart-secret.component";
import {AuthService} from "../../service/auth.service";

declare var $: any;

@Component({
  selector: 'app-register-smart',
  templateUrl: './register-smart.component.html',
  styleUrls: ['./register-smart.component.css']
})
export class RegisterSmartComponent implements OnInit {

    registerForm : FormGroup;

    endpoint: fhir.Endpoint;

  constructor(
      public dialog: MatDialog,
      public dialogRef: MatDialogRef<RegisterSmartComponent>,
    private _formBuilder: FormBuilder,
    private authService: AuthService,
    private fhirService: FhirService,
    @Inject(MAT_DIALOG_DATA) data) {

    if (data !== undefined && data.endpoint !== undefined) {
        this.endpoint = data.endpoint;
        this.edit = true;
        this.appName = data.endpoint.name;
        this.appLaunch = data.endpoint.address;
    }
  }

  @Input()
  resource = undefined;

  appName: string;

  appLaunch: string;

  appLogo: string;

  appRedirect;

  appDescription;

  appSupplier: string;

  edit:boolean = false;

  files: File | FileList;



  ngOnInit() {

      const reg = '^https?:\\/\\/\\w+(:[0-9]*)?([-a-zA-Z0-9@:%._\\+~#=]{2,256})?[\\/a-zA-Z0-9_-]+$';

      console.log('Init Called TREE');



      if (this.edit) {
          this.registerForm = this._formBuilder.group({
              'appName' : [ new FormControl(this.appName), Validators.required ],
              'appLaunch' : [ new FormControl(this.appLaunch), [ Validators.required, Validators.pattern(reg)] ],
              'appRedirect' : [ new FormControl(this.appRedirect)  ],
              'appLogo' : [ new FormControl(this.appLogo)  ],
              'appDescription' : [ new FormControl(this.appDescription)  ],
              'appSupplier' : [ new FormControl(this.appSupplier)  ],

          });
      } else {
          this.registerForm = this._formBuilder.group({
              'appName' : [ new FormControl(this.appName), Validators.required ],
              'appLaunch' : [ new FormControl(this.appLaunch), [ Validators.required, Validators.pattern(reg)] ],
              'appRedirect' : [ new FormControl(this.appRedirect)  ],
              'appLogo' : [ new FormControl(this.appLogo)  ],
              'appDescription' : [ new FormControl(this.appDescription) , Validators.required  ],
              'appSupplier' : [ new FormControl(this.appSupplier), Validators.required   ],

          });
      }

  }

    fileSelectMsg: string = 'No file selected yet.';
    fileUploadMsg: string = 'No file uploaded yet.';
    disabled: boolean = false;

    selectEvent(file: File): void {
        this.fileSelectMsg = file.name;
    }

    uploadEvent(file: File): void {
        this.fileUploadMsg = file.name;
    }

    cancelEvent(): void {
        this.fileSelectMsg = 'No file selected yet.';
        this.fileUploadMsg = 'No file uploaded yet.';
    }

    getDisabledValue() {
        return this.edit;
    }

    registerApp() {
        console.log('register SMART App');

        let redirects: string[] = [];

        if (!this.edit) {
            if (this.registerForm.controls['appRedirect'].value !== undefined && this.registerForm.controls['appRedirect'].value !== '') {
                redirects = this.registerForm.controls['appRedirect'].value.split('/n');
            }
            ;
            this.authService.performRegisterSMARTApp(this.registerForm.controls['appName'].value,
                this.registerForm.controls['appLaunch'].value,
                redirects,
                this.registerForm.controls['appLogo'].value,
                this.registerForm.controls['appSupplier'].value
            ).subscribe(response => {

                    console.log(response);


                    let endpoint: fhir.Endpoint = {
                        resourceType: 'Endpoint',
                        identifier: [{
                            system: 'https://fhir.leedsth.nhs.uk/Id/clientId',
                            value: response.client_id
                        }],
                        status: 'active',
                        "connectionType": {
                            "system": "http://hl7.org/fhir/endpoint-connection-type",
                            "code": "direct-project"
                        },
                        name: this.registerForm.controls['appName'].value,
                        payloadType: [
                            {
                                "coding": [
                                    {
                                        "system": "http://hl7.org/fhir/resource-types",
                                        "code": "Endpoint"
                                    }
                                ]
                            }
                        ],
                        address: this.registerForm.controls['appLaunch'].value
                    };

                    this.fhirService.post('/Endpoint',endpoint).subscribe(resp => {
                            console.log(resp);
                            const dialogConfig = new MatDialogConfig();

                            dialogConfig.disableClose = true;
                            dialogConfig.autoFocus = true;
                            dialogConfig.data = {
                                id: 1,
                                response: response
                            };
                            this.dialog.open(RegisterSmartSecretComponent, dialogConfig);
                        }
                    )


                }
                , (error: any) => {
                    console.log("Register Response Error = " + error);
                }
                , () => {

                    console.log("Register complete()");
                    this.dialogRef.close();
                }
            );

        } else {

            // No edit of OAuth2 details at present

            this.endpoint.name = this.registerForm.controls['appName'].value;
            this.endpoint.address = this.registerForm.controls['appLaunch'].value;

            if (this.endpoint.id === undefined) {
             // console.log(this.registerForm.controls['appName'].value.value);
              this.endpoint.name = this.registerForm.controls['appName'].value.value;
              this.fhirService.post('/Endpoint', this.endpoint).subscribe(resp => {
                  console.log(resp);
                  /*
                  const dialogConfig = new MatDialogConfig();

                  dialogConfig.disableClose = true;
                  dialogConfig.autoFocus = true;
                  dialogConfig.data = {
                    id: 1
                  };
                  this.dialog.open(RegisterSmartSecretComponent, dialogConfig);*/
                },
                (error: any) => {
                  console.log("Register Response Error = " + error);
                },
                () => {
                  this.dialogRef.close();
                })
            } else {
              this.fhirService.put('/Endpoint/' + this.endpoint.id, this.endpoint).subscribe(resp => {
                  console.log(resp);
                  /*
                  const dialogConfig = new MatDialogConfig();

                  dialogConfig.disableClose = true;
                  dialogConfig.autoFocus = true;
                  dialogConfig.data = {
                    id: 1
                  };
                  this.dialog.open(RegisterSmartSecretComponent, dialogConfig);
                  */
                },
                (error: any) => {
                  console.log("Register Response Error = " + error);
                },
                () => {
                  this.dialogRef.close();
                }
              )
            }
        }
    }



}


