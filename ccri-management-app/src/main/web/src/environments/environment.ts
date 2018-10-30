// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
    oauth2 : {
        eprUrl : 'http://127.0.0.1:9090/ccri-smartonfhir/STU3',
        client_id : 'nhs-smart-ehr',
        client_secret : 'APa5oCe6SHhty_or2q34WpNcq0-X957n6p48TkAJw14YCtmZeQil60XvCfuByIPd8DlXyusxAGxp5_Z5UKlgZJU',
        cookie_domain : 'localhost'
    },
  apps: [
  /*  {
      name: 'QRisk',
      image: 'https://avatars2.githubusercontent.com/u/841981?s=200&v=4',
      url: 'https://54.201.252.26/csp/qrisk/launch.html',
      notes: 'QRisk by J2 Interactive',
      source : 'J2 Interactive'
    },
    {
      name: 'SMART on FHIR Developer App 1',
      image: 'https://content.hspconsortium.org/images/my-web-app/logo/my.png',
      url: 'http://localhost:4200/launch',
      notes: 'Launches SMART on FHIR App on http://localhost:4200/launch',
      source : 'SMART on FHIR Developers'
    },
    {
      name: 'SMART on FHIR Developer App 2',
      image: 'https://content.hspconsortium.org/images/my-web-app/logo/my.png',
      url: 'http://127.0.0.1:4201/launch',
      notes: 'Launches SMART on FHIR App on http://127.0.0.1:4201/launch',
      source : 'SMART on FHIR Developers'
    } */
  ]

};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
