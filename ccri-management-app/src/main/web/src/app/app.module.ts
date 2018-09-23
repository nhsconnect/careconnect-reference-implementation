import { BrowserModule } from '@angular/platform-browser';
import {ErrorHandler, NgModule} from '@angular/core';
import 'hammerjs';
import { AppComponent } from './app.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {
    CovalentDialogsModule, CovalentExpansionPanelModule,
    CovalentJsonFormatterModule,
    CovalentLayoutModule,
    CovalentMediaModule,
    CovalentMessageModule,
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
    MatDialogModule,
    MatGridListModule,
    MatIconModule,
    MatIconRegistry,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatPaginatorModule, MatProgressBarModule,
    MatSelectModule,
    MatSidenavModule,
    MatSnackBarModule,
    MatTableModule,
    MatToolbarModule
} from "@angular/material";
import {MAT_MOMENT_DATE_FORMATS, MatMomentDateModule, MomentDateAdapter} from "@angular/material-moment-adapter";
import {MainComponent} from "./component/main/main.component";
import { ConformanceComponent } from './component/conformance/conformance.component';
import { ResourceComponent } from './component/resource/resource.component';
import {HttpClientModule} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import {FlexLayoutModule} from "@angular/flex-layout";
import {ErrorsHandler} from "./error-handler";
import {MessageService} from "./service/message.service";



@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    ConformanceComponent,
    ResourceComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
      HttpClientModule,
      AppRoutingModule,
      FormsModule,
      FlexLayoutModule,


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
      CovalentExpansionPanelModule


  ],
  providers: [
    MatIconRegistry,
      MessageService,
    { provide: MAT_DATE_LOCALE, useValue: 'en-GB'},
    {provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE]},
    {provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS},
      {
          provide: ErrorHandler,
          useClass: ErrorsHandler,
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
