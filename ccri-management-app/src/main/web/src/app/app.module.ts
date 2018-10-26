import { BrowserModule } from '@angular/platform-browser';
import {APP_INITIALIZER, ErrorHandler, NgModule} from '@angular/core';
import 'hammerjs';
import { AppComponent } from './app.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {
  CovalentChipsModule,
  CovalentDialogsModule, CovalentExpansionPanelModule,
  CovalentJsonFormatterModule,
  CovalentLayoutModule,
  CovalentMediaModule, CovalentMenuModule,
  CovalentMessageModule, CovalentNotificationsModule,
  CovalentStepsModule
} from "@covalent/core";
import {CovalentHttpModule} from "@covalent/http";
import {CovalentHighlightModule} from "@covalent/highlight";
import {CovalentMarkdownModule} from "@covalent/markdown";
import {CovalentDynamicFormsModule} from "@covalent/dynamic-forms";
import {AppRoutingModule} from "./app-routing.module";
import {
    DateAdapter,
    MAT_DATE_FORMATS,
    MAT_DATE_LOCALE,
    MatBadgeModule,
    MatButtonModule,
    MatCardModule, MatChipsModule,
    MatDatepickerModule,
    MatDialogModule, MatFormFieldModule, MatGridListModule,

    MatIconModule,
    MatIconRegistry,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatPaginatorModule, MatProgressBarModule, MatRadioModule,
    MatSelectModule,
    MatSidenavModule,
    MatSnackBarModule,
    MatTableModule,
    MatToolbarModule, MatTooltipModule,


} from "@angular/material";
import {MAT_MOMENT_DATE_FORMATS, MatMomentDateModule, MomentDateAdapter} from "@angular/material-moment-adapter";
import {MainComponent} from "./modules/main/main.component";
import { ConformanceComponent } from './modules/conformance/conformance.component';
import { ResourceComponent } from './modules/resource/resource.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {FlexLayoutModule} from "@angular/flex-layout";
import {ErrorsHandler} from "./error-handler";
import {MessageService} from "./service/message.service";
import {DocumentReferenceComponent} from "./component/document-reference/document-reference.component";
import {MedicationComponent} from "./component/medication/medication.component";
import {MedicationRequestComponent} from "./component/medication-request/medication-request.component";
import {AllergyIntoleranceComponent} from "./component/allergy-intolerance/allergy-intolerance.component";
import {ProcedureComponent} from "./component/procedure/procedure.component";
import {ConditionComponent} from "./component/condition/condition.component";
import {MedicationStatementComponent} from "./component/medication-statement/medication-statement.component";
import {EncounterComponent} from "./component/encounter/encounter.component";
import {ObservationComponent} from "./component/observation/observation.component";
import {ImmunisationDetailComponent} from './dialog/immunisation-detail/immunisation-detail.component';
import {ImmunisationComponent} from "./component/immunisation/immunisation.component";

import {ResourceDialogComponent} from "./dialog/resource-dialog/resource-dialog.component";
import {OrganisationComponent} from "./component/organisation/organisation.component";
import {PractitionerComponent} from "./component/practitioner/practitioner.component";
import {MedicationDispenseComponent} from "./component/medication-dispense/medication-dispense.component";
import {CarePlanComponent} from "./component/care-plan/care-plan.component";
import {ConsentComponent} from "./component/consent/consent.component";
import {ClinicalImpressionComponent} from "./component/clinical-impression/clinical-impression.component";
import {GoalComponent} from "./component/goal/goal.component";
import {RiskAssessmentComponent} from "./component/risk-assessment/risk-assessment.component";
import {QuestionnaireResponseComponent} from "./component/questionnaire-response/questionnaire-response.component";
import {EncounterDialogComponent} from "./dialog/encounter-dialog/encounter-dialog.component";
import {PractitionerRoleDialogComponent} from "./dialog/practitioner-role-dialog/practitioner-role-dialog.component";
import {HealthcareServiceComponent} from "./component/healthcare-service/healthcare-service.component";
import {PractitionerRoleComponent} from "./component/practitioner-role/practitioner-role.component";
import {LocationComponent} from "./component/location/location.component";
import {OrganisationDialogComponent} from "./dialog/organisation-dialog/organisation-dialog.component";
import {PractitionerDialogComponent} from "./dialog/practitioner-dialog/practitioner-dialog.component";
import {LocationDialogComponent} from "./dialog/location-dialog/location-dialog.component";
import {IssueDialogComponent} from "./dialog/issue-dialog/issue-dialog.component";
import {MedicationDialogComponent} from "./dialog/medication-dialog/medication-dialog.component";
import {PatientComponent} from "./component/patient/patient.component";
import {MedicationDispenseDetailComponent} from "./dialog/medication-dispense-detail/medication-dispense-detail.component";
import {LinksService} from "./service/links.service";
import {EprService} from "./service/epr.service";
import {ResponseInterceptor} from "./response-interceptor";
import {AppConfig} from "./app-config";
import { ObservationChartDialogComponent } from './dialog/observation-chart-dialog/observation-chart-dialog.component';
import {NgxChartsModule} from "@swimlane/ngx-charts";
import { EdDashboardComponent } from './modules/ed-dashboard/ed-dashboard.component';
import { ExplorerMainComponent } from './modules/explorer-main/explorer-main.component';
import {EprRoutingModule} from "./modules/epr-routing.module";
import {BinaryComponent} from "./component/binary/binary/binary.component";
import {PdfViewerComponent} from "./component/binary/pdf-viewer/pdf-viewer.component";
import {ImgViewerComponent} from "./component/binary/img-viewer/img-viewer.component";
import {ViewDocumentComponent} from "./component/binary/composition-view/view-document.component";
import {ViewDocumentSectionComponent} from "./component/binary/composition-view-section/view-document-section.component";
import {PdfViewerModule} from "ng2-pdf-viewer";
import {ImageViewerModule} from "@hallysonh/ngx-imageviewer";
import { MapComponent } from './component/map/map.component';
import { EdEncounterListComponent } from './modules/ed-encounter-list/ed-encounter-list.component';
import {EdRoutingModule} from "./modules/ed-routing.module";
import { EdEncounterCardComponent } from './modules/ed-encounter-card/ed-encounter-card.component';
import {NguiMapModule} from "@ngui/map";
import { PatientMainComponent } from './modules/patient-main/patient-main.component';
import { TriageComponent } from './modules/triage/triage.component';
import {PatientSearchComponent} from "./component/patient-search/patient-search.component";
import { CapacityComponent } from './modules/capacity/capacity.component';
import { PatientSummaryComponent } from './modules/patient-summary/patient-summary.component';
import { PatientVitalSignsComponent } from './modules/patient-vital-signs/patient-vital-signs.component';
import { PatientEncountersComponent } from './modules/patient-encounters/patient-encounters.component';
import { PatientDocumentsComponent } from './modules/patient-documents/patient-documents.component';
import { PatientEncounterDetailComponent } from './modules/patient-encounter-detail/patient-encounter-detail.component';
import { PatientProcedureComponent } from './modules/patient-procedure/patient-procedure.component';
import {CompositionDetailComponent} from "./component/composition-detail/composition-detail.component";
import {CompositionComponent} from "./component/composition/composition.component";
import { PatientTimeSeriesComponent } from './modules/patient-time-series/patient-time-series.component';
import {PatientTimelineComponent} from "./component/patient-timeline/patient-timeline.component";
import { PatientImmunisationComponent } from './modules/patient-immunisation/patient-immunisation.component';


export function initializeApp(appConfig: AppConfig) {
    return () => appConfig.load();
}

@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    ConformanceComponent,
    ResourceComponent,

    AllergyIntoleranceComponent,
      BinaryComponent,
      PdfViewerComponent,
      ImgViewerComponent,
      ViewDocumentComponent,
      ViewDocumentSectionComponent,

    ClinicalImpressionComponent,
    ConsentComponent,
    CarePlanComponent,
    ConditionComponent,
    CompositionComponent,
    CompositionDetailComponent,
    DocumentReferenceComponent,
    EncounterComponent,
    GoalComponent,
    ImmunisationComponent,
    ImmunisationDetailComponent,
    LocationComponent,
    MedicationDispenseComponent,
    MedicationDispenseDetailComponent,
    MedicationStatementComponent,
    MedicationRequestComponent,
    MedicationComponent,
    ObservationComponent,
    OrganisationComponent,
    PatientComponent,
    ProcedureComponent,
    PractitionerComponent,
    QuestionnaireResponseComponent,
    RiskAssessmentComponent,



    ResourceDialogComponent,
    MedicationDialogComponent,
    IssueDialogComponent,
    LocationDialogComponent,
    PractitionerDialogComponent,
    OrganisationDialogComponent,
    PractitionerRoleComponent,
    HealthcareServiceComponent,
    PractitionerRoleDialogComponent,
    EncounterDialogComponent,
    ObservationChartDialogComponent,
    EdDashboardComponent,
    ExplorerMainComponent,
    MapComponent,
    EdEncounterListComponent,
    EdEncounterCardComponent,
    PatientMainComponent,
    TriageComponent,
      PatientSearchComponent,
      CapacityComponent,
      PatientSummaryComponent,
      PatientVitalSignsComponent,
      PatientEncountersComponent,
      PatientDocumentsComponent,
      PatientEncounterDetailComponent,
      PatientProcedureComponent,
      PatientTimelineComponent,
      PatientTimeSeriesComponent,
      PatientImmunisationComponent



  ],
  entryComponents: [
    ResourceDialogComponent,
    MedicationDialogComponent,
    IssueDialogComponent,
    LocationDialogComponent,
    PractitionerDialogComponent,
    OrganisationDialogComponent,
    PractitionerRoleDialogComponent,
    EncounterDialogComponent,
    MedicationDispenseDetailComponent,
    ImmunisationDetailComponent,
      ObservationChartDialogComponent,
      TriageComponent

  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
      HttpClientModule,

      FormsModule,
      FlexLayoutModule,
      ReactiveFormsModule,
      MatFormFieldModule,



    MatMomentDateModule,
    MatDatepickerModule,
    MatSidenavModule,
    MatInputModule,
    MatListModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,

    MatToolbarModule,
    MatTableModule,
    MatGridListModule,
    MatDialogModule,
    MatPaginatorModule,
    MatMenuModule,
    MatSnackBarModule,
      MatBadgeModule,
      MatChipsModule,
      MatProgressBarModule,
      MatRadioModule,
    MatTooltipModule,
    MatGridListModule,
    MatGridListModule,

    CovalentLayoutModule,
    CovalentStepsModule,
    // (optional) Additional Covalent Modules imports
    CovalentHttpModule.forRoot(),
    CovalentHighlightModule,
    CovalentMarkdownModule,
    CovalentDynamicFormsModule,
    CovalentMediaModule,
    CovalentMessageModule,
    CovalentJsonFormatterModule,
    CovalentDialogsModule,
    CovalentExpansionPanelModule,
      CovalentChipsModule,
    CovalentNotificationsModule,
    CovalentMenuModule,

    NgxChartsModule,

      PdfViewerModule,
      ImageViewerModule,
     NguiMapModule.forRoot({apiUrl: 'https://maps.google.com/maps/api/js?key=AIzaSyDC8GmtqZiQXc16qf1v870NKy-phjv-1N0'}),
    //  NguiMapModule.forRoot({apiUrl: 'https://maps.google.com/maps/api/js?key='}),

    EprRoutingModule,
    EdRoutingModule,
      AppRoutingModule
  ],
  providers: [
      AppConfig,
      { provide: APP_INITIALIZER,
          useFactory: initializeApp,
          deps: [AppConfig], multi: true },
    MatIconRegistry,
      MessageService,
    LinksService,
    EprService,
    { provide: MAT_DATE_LOCALE, useValue: 'en-GB'},
    {provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS},
    {
          provide: ErrorHandler,
          useClass: ErrorsHandler,
      },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ResponseInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(
    public matIconRegistry: MatIconRegistry) {
    matIconRegistry.registerFontClassAlias('fontawesome', 'fa');
  }
}
