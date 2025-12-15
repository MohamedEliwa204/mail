import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// [BACKEND DATA STRUCTURES]
// Ensure Backend DTOs match these interfaces exactly.

export interface Contact {
  id: number;
  name: string;
  emails: string[]; // Backend must handle List<String>
}

export interface Attachment {
  id?: number;
  fileName: string;
  contentType: string; // e.g., 'application/pdf'
  data?: string;       // Not needed for upload, but needed for download (Base64)
}

export interface Mail {
  mailId: number;
  sender: string;
  receiver: string;
  body: string;
  subject: string;
  timestamp: string; // ISO 8601 Format (e.g., "2023-12-25T10:30:00")
  priority: number;  // 1=High, 3=Normal, 4=Low
  folderName: string;
  isRead: boolean;
  attachments?: Attachment[];
}

export interface ComposeEmailDTO {
  sender?: string;
  receivers: string[]; 
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

  /* [BACKEND REQ] GET Inbox
     Request: GET /api/mail/inbox/{userEmail}
     Response: JSON Array of Mail objects */
  getInboxMails(userEmail: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/inbox/${userEmail}`);
  }

  /* [BACKEND REQ] GET Sent
     Request: GET /api/mail/sent/{userEmail}
     Response: JSON Array of Mail objects */
  getSentMails(userEmail: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/sent/${userEmail}`);
  }

  /* [BACKEND REQ] GET Drafts
     Request: GET /api/mail/drafts/{userEmail}
     Response: JSON Array of Mail objects */
  getDraftMails(userEmail: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/drafts/${userEmail}`);
  }

  /* [BACKEND REQ] GET Custom Folder
     Request: GET /api/mail/folder/{userEmail}/{folderName}
     Response: JSON Array of Mail objects */
  getMailsByFolder(userEmail: string, folderName: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/folder/${userEmail}/${folderName}`);
  }

  /* [BACKEND REQ] GET Single Mail
     Request: GET /api/mail/{mailId}
     Response: Single JSON Mail object */
  getMailById(mailId: number): Observable<Mail> {
    return this.http.get<Mail>(`${this.apiURL}/${mailId}`);
  }

  /* [BACKEND REQ] Send Simple Mail (Text Only)
     Request: POST /api/mail/send
     Body: ComposeEmailDTO (JSON) */
  sendMail(composeEmailDTO: ComposeEmailDTO): Observable<any> {
    return this.http.post(`${this.apiURL}/send`, composeEmailDTO);
  }

  /* [BACKEND REQ] Save Draft
     Request: POST /api/mail/draft
     Body: ComposeEmailDTO (JSON) */
  draftEmail(composeEmailDTO: ComposeEmailDTO): Observable<any> {
    return this.http.post(`${this.apiURL}/draft`, composeEmailDTO);
  }

  /* [BACKEND REQ] Mark Read
     Request: PUT /api/mail/{mailId}/read
     Body: Empty */
  markAsRead(mailId: number): Observable<any> {
    return this.http.put(`${this.apiURL}/${mailId}/read`, {});
  }

  /* [BACKEND REQ] Delete Mail
     Request: DELETE /api/mail/{mailId} */
  deleteMail(mailId: number): Observable<any> {
    return this.http.delete(`${this.apiURL}/${mailId}`);
  }

  /* [BACKEND REQ] User Folders CRUD */
  getUserFolders(userEmail: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiURL}/folders/${userEmail}`);
  }

  createFolder(userEmail: string, folderName: string): Observable<any> {
    return this.http.post(`${this.apiURL}/folders/${userEmail}`, {}, { 
      params: { folderName } 
    });
  }

  deleteFolder(userEmail: string, folderName: string): Observable<any> {
    return this.http.delete(`${this.apiURL}/folders/${userEmail}`, { 
      params: { folderName } 
    });
  }

  renameFolder(userEmail: string, oldName: string, newName: string): Observable<any> {
    return this.http.put(`${this.apiURL}/folders/${userEmail}`, {}, { 
      params: { oldName, newName } 
    });
  }

  /* [BACKEND REQ] Advanced Search
     1. Function: Filter emails based on criteria.
     2. Request: GET /api/mail/search
     3. Query Params: ?email=x&folder=inbox&method=subject&query=hello
     4. Response: JSON Array of Mail objects
  */
  searchMails(userEmail: string, folder: string, method: string, query: string): Observable<Mail[]> {
    return this.http.get<Mail[]>(`${this.apiURL}/search`, {
      params: {
        email: userEmail,
        folder: folder,
        method: method,
        query: query
      }
    });
  }

  /* [BACKEND REQ] Send Mail With Attachments
     1. Function: Handle multipart file upload + email metadata.
     2. Content-Type: multipart/form-data
     3. Request Parts:
        - Part 'email': JSON string/blob of ComposeEmailDTO
        - Part 'attachments': List of Files
  */
  sendMailWithAttachments(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiURL}/send-with-attachments`, formData);
  }

  /* [BACKEND REQ] Contacts Management CRUD */
  
getContacts(userEmail: string, sortingType: boolean): Observable<Contact[]> {
  return this.http.get<Contact[]>(`${this.apiURL}/contacts`, {
    params: {
      email: userEmail,
      sort: sortingType.toString()
    }
  });
}

  addContact(contact: Contact, userEmail: string): Observable<any> {
    return this.http.post(`${this.apiURL}/contacts`, contact, {
      params: { userEmail }
    });
  }

  editContact(contact: Contact): Observable<any> {
    return this.http.put(`${this.apiURL}/contacts`, contact);
  }

  deleteContact(contactId: number): Observable<any> {
    return this.http.delete(`${this.apiURL}/contacts/${contactId}`);
  }
}