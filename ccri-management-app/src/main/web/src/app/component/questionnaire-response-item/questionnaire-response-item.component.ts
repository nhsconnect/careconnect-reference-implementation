import {Component, Input, OnInit} from '@angular/core';
import {MatTableDataSource} from '@angular/material';

@Component({
  selector: 'app-questionnaire-response-item',
  templateUrl: './questionnaire-response-item.component.html',
  styleUrls: ['./questionnaire-response-item.component.css']
})
export class QuestionnaireResponseItemComponent implements OnInit {

  @Input()
  form: fhir.QuestionnaireResponse = undefined;
  items: MatTableDataSource<any> = undefined;
  displayedColumns: string[] = ['question', 'answer'];
  constructor() { }

  ngOnInit() {
      this.items = new MatTableDataSource<any> (this.form.item);
  }

}
