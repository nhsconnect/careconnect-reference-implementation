import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {Oauth2Service} from "../../service/oauth2.service";
import {Router} from "@angular/router";


@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.css']
})
export class LogoutComponent implements OnInit {



  constructor(
    private authService: AuthService,
    private oauth2 : Oauth2Service,
    private router : Router
  ) { }

  ngOnInit(
  ) {
    this.oauth2.removeToken();
    this.router.navigateByUrl('');
    // window.location.href = this.authService.getLogonServer()+'/logout?afterAuth=' + document.baseURI;
  }

}
