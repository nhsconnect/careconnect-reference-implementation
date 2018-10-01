import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-healthcare-service',
  templateUrl: './healthcare-service.component.html',
  styleUrls: ['./healthcare-service.component.css']
})
export class HealthcareServiceComponent implements OnInit {

  @Input() services : fhir.HealthcareService[];

  @Output() healthcareService = new EventEmitter<any>();

  constructor() { }

  ngOnInit() {
  }

}
