import { Component, Inject, inject, signal } from '@angular/core';
import { AuthenticationService } from '../services/authentication-service';
import { Router } from '@angular/router';

//dummyData
import { MailList } from './mailList';

@Component({
  selector: 'app-mail',
  imports: [],
  templateUrl: './mail.html',
  styleUrls: ['./mail.css', './profile.css', './navbar.css', './sidebar.css', './main-section.css',
    './filterbar.css', './selectbar.css'
  ],
})
export class Mail {
  authenticationService = inject(AuthenticationService);
  router = inject(Router);

  //dummydata
  mailList = inject(MailList).mailList;

  currentUser = this.authenticationService.user;

  getRange(n: number) {
    return Array.from({length: n}, (_, i) => i);
  }

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

  //pagination
  // only 8 mails per page

  page = signal(0);

  pageFrom = signal(1)
  pageTo = signal(this.mailList.length)

  itemsPerPage = 8

  generatePage() {
    return Array.from({length: this.itemsPerPage},
                       (_, i) => i+this.page()*this.itemsPerPage);
  }

  pageDisplay = signal(`${this.pageFrom()}-${this.pageTo()} of ${this.mailList.length}`)

  pagingLeft(){
    const n = this.mailList.length;
    const pages = Math.ceil(n/8);
    
    if(this.page() != 0){
      this.page.update(value => value-1)
      
      if(this.page() == pages-2)
        this.pageTo.set(this.pageFrom()-1)
      else
        this.pageTo.update(value => value-this.itemsPerPage)

      this.pageFrom.update(value => value-this.itemsPerPage)
    }
    
  }

  pagingRight(){
    const n = this.mailList.length;
    const pages = Math.ceil(n/8);
    if(this.page() < pages){
      if(this.page() < pages-1){
        this.page.update(value => value+1)
        this.pageFrom.update(value => value+this.itemsPerPage)
      }
      if(this.page() == pages-1)
        this.pageTo.update(value => n)
      else
        this.pageTo.update(value => value+this.itemsPerPage)
        
    }
    
      
  }


}
