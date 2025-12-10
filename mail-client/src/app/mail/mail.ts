import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { AuthenticationService } from '../services/authentication-service';
import { Router } from '@angular/router';
import { MailService, Mail as MailEntity, ComposeEmailDTO } from '../services/mail-service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-mail',
  imports: [CommonModule, FormsModule],
  templateUrl: './mail.html',
  styleUrls: ['./mail.css', './profile.css', './navbar.css', './sidebar.css', './main-section.css',
    './filterbar.css', './selectbar.css', "./compose.css", "./mailview.css"
  ],
})
export class Mail implements OnInit {
  authenticationService = inject(AuthenticationService);
  mailService = inject(MailService);
  router = inject(Router);

  //dummydata
  currentUser = this.authenticationService.user;

  getRange(n: number) {
    return Array.from({length: n}, (_, i) => i);
  }

  // Mail data
  mails = signal<MailEntity[]>([]);
  currentFolder = signal<string>('inbox');
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  //profile part
  isProfileDropdownOpen: boolean = false;

  ngOnInit() {
    // Load inbox by default when component initializes
    this.loadInbox();
  }

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

  itemsPerPage = 8

  page = signal(0);

  pageFrom = signal(1)
  numOfItems = this.mails().length
  pageTo = signal(this.itemsPerPage)
  generatePage() {
    return Array.from({length: Math.min(this.itemsPerPage, this.mails().length)},
                       (_, i) => i+this.page()*this.itemsPerPage);
  }

  pageDisplay = signal(`${this.pageFrom()}-${this.pageTo()} of ${this.mails().length}`)

  pagingLeft(){
    const n = this.mails().length;
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
    const n = this.mails().length;
    const pages = Math.ceil(n/this.itemsPerPage);
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

  // Load inbox mails
  loadInbox() {
    const userEmail = this.currentUser()?.email;
    if (!userEmail) {
      this.errorMessage.set('User not logged in');
      return;
    }

    this.currentFolder.set('inbox');
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.mailService.getInboxMails(userEmail).subscribe({
      next: (mails) => {
        this.mails.set(mails);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading inbox:', error);
        this.errorMessage.set('Failed to load inbox');
        this.isLoading.set(false);
      }
    });
  }

  // Load sent mails
  loadSent() {
    const userEmail = this.currentUser()?.email;
    if (!userEmail) {
      this.errorMessage.set('User not logged in');
      return;
    }

    this.currentFolder.set('sent');
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.mailService.getSentMails(userEmail).subscribe({
      next: (mails) => {
        this.mails.set(mails);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading sent mails:', error);
        this.errorMessage.set('Failed to load sent mails');
        this.isLoading.set(false);
      }
    });
  }

  // Load draft mails
  loadDrafts() {
    const userEmail = this.currentUser()?.email;
    if (!userEmail) {
      this.errorMessage.set('User not logged in');
      return;
    }

    this.currentFolder.set('drafts');
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.mailService.getDraftMails(userEmail).subscribe({
      next: (mails) => {
        this.mails.set(mails);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error loading drafts:', error);
        this.errorMessage.set('Failed to load drafts');
        this.isLoading.set(false);
      }
    });
  }

  // Load mails by folder name
  loadFolder(folderName: string) {
    const userEmail = this.currentUser()?.email;
    if (!userEmail) {
      this.errorMessage.set('User not logged in');
      return;
    }

    this.currentFolder.set(folderName);
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.mailService.getMailsByFolder(userEmail, folderName).subscribe({
      next: (mails) => {
        this.mails.set(mails);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error(`Error loading ${folderName}:`, error);
        this.errorMessage.set(`Failed to load ${folderName}`);
        this.isLoading.set(false);
      }
    });
  }

  // Refresh current folder
  refresh() {
    const folder = this.currentFolder();
    if (folder === 'inbox') {
      this.loadInbox();
    } else if (folder === 'sent') {
      this.loadSent();
    } else if (folder === 'drafts') {
      this.loadDrafts();
    } else {
      this.loadFolder(folder);
    }
  }

  //compose email
  isComposing = false;
  compseToggle(){
    this.isComposing = !this.isComposing
  }

  composedMail: ComposeEmailDTO = {
    sender: this.currentUser()?.email,
    receivers: [],
    subject: '',
    body: '',
    priority: 1
  }


  sendComposedMail(){
    console.log(this.composedMail.sender)
    console.log(this.composedMail.receivers)
    console.log(this.composedMail.subject)
    console.log(this.composedMail.body)
    console.log(this.composedMail.priority)
    if(this.composedMail.receivers.length > 0){
      this.mailService.sendMail(this.composedMail).subscribe({
        next: res => {
          console.log(res.message)
          this.refresh()},
        error: e => {
          if (e.error && e.error.error) {
            console.log(`Error: ${e.error.error}`);
          } 
          else {
            console.log('Unknown error', e);
          }
        }
      });
      this.composedMail.receivers = []
      this.composedMail.subject = ''
      this.composedMail.body = ''
      this.composedMail.priority = 1
      console.log("Email is sent")
    }
  }

  //mail preview
  selectedMail = signal<MailEntity | null>(null);

  setselectedMail(mail: MailEntity){
    this.selectedMail.set(mail)
    console.log(this.selectedMail)
  }

  clearselectedMail(){
    this.selectedMail.set(null)
    console.log(this.selectedMail)
  }

}
