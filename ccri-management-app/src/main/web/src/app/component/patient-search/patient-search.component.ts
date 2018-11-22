/// <reference path="../../../../node_modules/@types/fhir/index.d.ts" />

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

import { Observable, Subject, of, throwError  } from 'rxjs';



import {
  catchError,
  debounceTime, distinctUntilChanged, map, switchMap
} from 'rxjs/operators';


import {HttpErrorResponse} from '@angular/common/http';
import {FhirService} from '../../service/fhir.service';
import {NEVER} from "rxjs/internal/observable/never";
import {EprService} from "../../service/epr.service";



@Component({
  selector: 'app-patient-search',
  templateUrl: './patient-search.component.html',
  styleUrls: [ './patient-search.component.css' ]
})
export class PatientSearchComponent implements OnInit {


  patients$: Observable<fhir.Patient[]>;
  private searchTerms = new Subject<string>();

  @Output() patientSelected : EventEmitter<fhir.Patient> = new EventEmitter();

  constructor(private patientChange : EprService, private fhirService: FhirService

  ) {}



  // Push a search term into the observable stream.
  search(term: string): void {
    this.searchTerms.next(term);
  }

  createObservable(i) {
    return new Observable();
  }


  ngOnInit(): void {
    this.patients$ = this.searchTerms.pipe(
      // wait 300ms after each keystroke before considering the term
      debounceTime(300),

      // ignore new term if same as previous term
      distinctUntilChanged(),

      // switch to new search observable each time the term changes
      switchMap((term: string) => {
         return this.fhirService.searchPatients(term) }

      ),

      map(bundle  => {
        var pat$: fhir.Patient[] = [];
        var i;
        if (bundle !== undefined && bundle.hasOwnProperty("entry")) {
          for (i = 0; i < bundle.entry.length && i < 10; i++) {
            pat$[i] = <fhir.Patient>bundle.entry[i].resource;
          }
        }
        return pat$;}
        )
    ), catchError(this.handleError('getPatients', []));

  }

  private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      console.log('patient search ERROR');

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      console.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  selectPatient(patient: fhir.Patient) {

    this.patientChange.set(patient);
    this.patients$ = undefined;
    this.patientSelected.emit(patient);
  }



  logError(title: string) {
      return (message :any) => {
        if(message instanceof HttpErrorResponse) {
          if (message.status == 401) {
            //this.authService.logout();
            //this.messageService.add(title + ": 401 Unauthorised");
          }
          if (message.status == 403) {
            //this.messageService.add(title + ": 403 Forbidden (insufficient scope)");
          }
        }
        console.log("Patient Search error handling "+message);

        return NEVER;

    }
  }
}
