import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Attachment {
  id?: number;
  fileName: string;
  contentType: string;
  data?: string; // Will be base64 encoded when transferred over HTTP
}

export interface Mail {
  mailId: number;
  sender: string;
  receiver: string;
  body: string;
  subject: string;
  timestamp: string; // LocalDateTime from backend comes as ISO string
  priority: number;
  folderName: string;
  isRead: boolean;
  attachments?: Attachment[];
  // Note: senderRel and receiverRel are lazy-loaded relations, 
  // usually not included in API responses
}

export interface ComposeEmailDTO {
  sender?: string;
  receivers: string[]; // List of email addresses
  subject: string;
  body: string;
  priority: number;
}

@Injectable({
  providedIn: 'root'
})
export class MailService {
  private http = inject(HttpClient);
  private apiURL = 'http://localhost:8080/api/mail';

  // Get inbox mails for a user
  getInboxMails(userEmail: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/inbox/${userEmail}`);
  }

  // Get sent mails for a user
  getSentMails(userEmail: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/sent/${userEmail}`);
  }

  // Get draft mails for a user
  getDraftMails(userEmail: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/drafts/${userEmail}`);
  }

  // Get mails by folder name
  getMailsByFolder(userEmail: string, folderName: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/folder/${userEmail}/${folderName}`);
  }

  // Get a specific mail by ID
  getMailById(mailId: number): Observable<Mail> {
    return this.http.get<Mail>(`${this.apiURL}/${mailId}`);
  }

  // Send an email
  sendMail(composeEmailDTO: ComposeEmailDTO): Observable<any> {
    return this.http.post(`${this.apiURL}/send`, composeEmailDTO);
  }

  // Save email as draft
  draftEmail(composeEmailDTO: ComposeEmailDTO): Observable<any> {
    return this.http.post(`${this.apiURL}/draft`, composeEmailDTO);
  }

  // Mark mail as read
  markAsRead(mailId: number): Observable<any> {
    return this.http.put(`${this.apiURL}/${mailId}/read`, {});
  }

  // Delete mail (move to trash)
  deleteMail(mailId: number): Observable<any> {
    return this.http.delete(`${this.apiURL}/${mailId}`);
  }
}

