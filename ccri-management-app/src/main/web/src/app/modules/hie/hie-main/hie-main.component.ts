import {Component, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {Router} from '@angular/router';
import {EprService} from '../../../service/epr.service';
import {MessageService} from '../../../service/message.service';
import {IAlertConfig, TdDialogService} from '@covalent/core';
import {AuthService} from '../../../service/auth.service';



@Component({
  selector: 'app-hie-main',
  templateUrl: './hie-main.component.html',
  styleUrls: ['./hie-main.component.css']
})
export class HieMainComponent implements OnInit {


    routes: Object[] = [
    ];

    routesExt: Object[] = [
    ];

    navmenu: Object[] = [];

    title  = 'Garforth Sector - Yorkshire Ambulance Service';

    rbac = 'tier1';

    constructor( private router: Router,
                 public eprService: EprService,
                 private _dialogService: TdDialogService,
                 private _viewContainerRef: ViewContainerRef,
                 private messageService: MessageService,
                 public authService: AuthService
      ) { }

  ngOnInit() {

    this.routes = this.eprService.routes;
    this.routesExt = this.eprService.routesExt;

    this.eprService.getTitleChange().subscribe( title => {
      this.title = title;
    });

    this.messageService.getMessageEvent().subscribe(
      error => {
        if (this.router.url.includes('exp')) {
          const alertConfig: IAlertConfig = {
            message: error
          };
          alertConfig.disableClose = false; // defaults to false
          alertConfig.viewContainerRef = this._viewContainerRef;
          alertConfig.title = 'Alert'; // OPTIONAL, hides if not provided

          alertConfig.width = '400px'; // OPTIONAL, defaults to 400px
          this._dialogService.openConfirm(alertConfig).afterClosed().subscribe((accept: boolean) => {

          });
        } else {
          console.log('not my baby');
        }

      }

    );

  }
  role(role: string) {
        this.rbac = role;
  }

    onClick(route) {
        this.router.navigateByUrl(route);
    }


}
