import { Component, inject, OnInit, signal } from '@angular/core';
import { AuthenticationService } from '../services/authentication-service';
import { Router } from '@angular/router';
import { MailService, Mail as MailEntity } from '../services/mail-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-mail',
  imports: [CommonModule],
  templateUrl: './mail.html',
  styleUrls: ['./mail.css', './profile.css', './navbar.css', './sidebar.css', './main-section.css'],
})
export class Mail implements OnInit {
  authenticationService = inject(AuthenticationService);
  mailService = inject(MailService);
  router = inject(Router);

  currentUser = this.authenticationService.user;

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

}
