import { Component, Inject, inject } from '@angular/core';
import { AuthenticationService } from '../services/authentication-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-mail',
  imports: [],
  templateUrl: './mail.html',
  styleUrls: ['./mail.css', './profile.css', './navbar.css', './sidebar.css', './main-section.css'],
})
export class Mail {
  authenticationService = inject(AuthenticationService);
  router = inject(Router);

  currentUser = this.authenticationService.user;

  //profile part
  isProfileDropdownOpen: boolean = false;

  toggleProfileDropdown(){
    this.isProfileDropdownOpen = !this.isProfileDropdownOpen;
  }

  addAccount(){
    this.router.navigateByUrl('/login');
  }

  signOut(){
    this.authenticationService.signOut();
  }

}
