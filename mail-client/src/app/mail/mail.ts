import { Component, inject } from '@angular/core';
import { AuthenticationService } from '../services/authentication-service';

@Component({
  selector: 'app-mail',
  imports: [],
  templateUrl: './mail.html',
  styleUrl: './mail.css',
})
export class Mail {
  authenticationService = inject(AuthenticationService);

  currentUser = this.authenticationService.user;
}
