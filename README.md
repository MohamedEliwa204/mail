# Backend Integration Guide - MansyMail Application

> **Complete guide for connecting the Frontend to the Backend API**

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Required Data Structures](#required-data-structures)
3. [Required API Endpoints](#required-api-endpoints)
4. [Required Frontend Modifications](#required-frontend-modifications)
5. [Integration Steps](#integration-steps)

---

## 🎯 Overview

This document outlines **all required modifications** to the frontend to connect it with the backend. Currently, the application runs with **Simulation** (mock data). To enable real backend integration, you must:

1. **Remove** simulation code
2. **Uncomment** backend calls
3. **Ensure** the backend responds with the specified data structures

---

## 📦 Required Data Structures

### 1. Contact Interface
**Location**: `mail-client/src/app/services/mail-service.ts` (lines 5-10)

```typescript
export interface Contact {
  id: number;
  name: string;
  emails: string[]; // Backend must handle List<String>
}
```

**Example JSON from Backend**:
```json
{
  "id": 1,
  "name": "Ahmed Hassan",
  "emails": ["ahmed@example.com", "ahmed.work@company.com"]
}
```

---

### 2. Attachment Interface
**Location**: `mail-client/src/app/services/mail-service.ts` (lines 12-16)

```typescript
export interface Attachment {
  id?: number;
  fileName: string;
  contentType: string; // e.g., 'application/pdf'
  data?: string;       // Base64 encoded (for downloads only)
}
```

**Example JSON from Backend**:
```json
{
  "id": 1,
  "fileName": "report.pdf",
  "contentType": "application/pdf"
}
```

> ⚠️ **Note**: The `data` field is not required when sending, but is required when downloading

---

### 3. Mail Interface
**Location**: `mail-client/src/app/services/mail-service.ts` (lines 18-26)

```typescript
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
```

**Example JSON from Backend**:
```json
{
  "mailId": 101,
  "sender": "ahmed@example.com",
  "receiver": "sara@example.com",
  "body": "This is the email body",
  "subject": "Meeting Tomorrow",
  "timestamp": "2023-12-25T10:30:00",
  "priority": 1,
  "folderName": "inbox",
  "isRead": false,
  "attachments": []
}
```

---

### 4. ComposeEmailDTO Interface
**Location**: `mail-client/src/app/services/mail-service.ts` (lines 28-34)

```typescript
export interface ComposeEmailDTO {
  sender?: string;
  receivers: string[]; 
  subject: string;
  body: string;
  priority: number;
}
```

**Example JSON sent to Backend**:
```json
{
  "sender": "ahmed@example.com",
  "receivers": ["sara@example.com", "john@example.com"],
  "subject": "Project Update",
  "body": "Please review the attached files",
  "priority": 1
}
```

---

## 🔌 Required API Endpoints

### Mail Operations

#### 1. Get Inbox
```
GET /api/mail/inbox/{userEmail}
Response: Mail[]
```

#### 2. Get Sent Mails
```
GET /api/mail/sent/{userEmail}
Response: Mail[]
```

#### 3. Get Drafts
```
GET /api/mail/drafts/{userEmail}
Response: Mail[]
```

#### 4. Get Custom Folder
```
GET /api/mail/folder/{userEmail}/{folderName}
Response: Mail[]
```

#### 5. Get Single Mail
```
GET /api/mail/{mailId}
Response: Mail
```

#### 6. Send Simple Mail
```
POST /api/mail/send
Body: ComposeEmailDTO
Response: { message: "Email sent successfully" }
```

#### 7. Send Mail with Attachments
```
POST /api/mail/send-with-attachments
Content-Type: multipart/form-data
Parts:
  - 'email': JSON Blob (ComposeEmailDTO)
  - 'attachments': File[] (Binary)
Response: { message: "Email sent successfully" }
```

#### 8. Save Draft
```
POST /api/mail/draft
Body: ComposeEmailDTO
Response: { message: "Draft saved successfully" }
```

#### 9. Mark as Read
```
PUT /api/mail/{mailId}/read
Body: {}
Response: { message: "Mail marked as read" }
```

#### 10. Delete Mail
```
DELETE /api/mail/{mailId}
Response: { message: "Mail deleted" }
```

---

### Folder Operations

#### 11. Get User Folders
```
GET /api/mail/folders/{userEmail}
Response: string[] (e.g., ["Work", "Personal", "Travel"])
```

#### 12. Create Folder
```
POST /api/mail/folders/{userEmail}?folderName={name}
Body: {}
Response: { message: "Folder created successfully" }
```

#### 13. Delete Folder
```
DELETE /api/mail/folders/{userEmail}?folderName={name}
Response: { message: "Folder deleted successfully" }
```

#### 14. Rename Folder
```
PUT /api/mail/folders/{userEmail}?oldName={old}&newName={new}
Body: {}
Response: { message: "Folder renamed successfully" }
```

---

### Search Operation

#### 15. Search Mails
```
GET /api/mail/search?email={userEmail}&folder={folder}&method={method}&query={query}
Query Params:
  - email: User email address
  - folder: Folder name (inbox, sent, drafts, etc.)
  - method: Search method (subject, sender, body, priority)
  - query: Search term
Response: Mail[]
```

---

### Contacts Operations

#### 16. Get Contacts
```
GET /api/mail/contacts?userEmail={email}
Response: Contact[]
```

#### 17. Add Contact
```
POST /api/mail/contacts?userEmail={email}
Body: { name: string, emails: string[] }
Response: { message: "Contact added successfully" }
```

#### 18. Edit Contact
```
PUT /api/mail/contacts/{id}
Body: { id: number, name: string, emails: string[] }
Response: { message: "Contact updated successfully" }
```

#### 19. Delete Contact
```
DELETE /api/mail/contacts/{id}
Response: { message: "Contact deleted successfully" }
```

---

## 🔧 Required Frontend Modifications

### File: `mail-client/src/app/mail/mail.ts`

#### 📍 **Modification 1: loadInbox() - Line 311-330**

**Current Code (Simulation)**:
```typescript
loadInbox() {
  const userEmail = this.currentUser()?.email;
  this.currentFolder.set('inbox');
  this.isLoading.set(true);

  // [BACKEND INTERACTION: GET INBOX]
  // 1. BE Task: Retrieve inbox emails with optional sorting.
  // 2. Request: GET /api/mail/inbox/{email}?sort=priority (if isPriorityMode is true)
  // 3. Response: List of Mail Entities
  /* const sortParam = this.isPriorityMode() ? 'priority' : 'timestamp';
  this.mailService.getInbox(userEmail, sortParam).subscribe(...)
  */

  // Frontend Simulation: Simulate backend response with sorting
  setTimeout(() => {
    const mailData: MailEntity[] = [
      // ... dummy data ...
    ];

    // Sort based on priority mode
    if (this.isPriorityMode()) {
      mailData.sort((a, b) => a.priority - b.priority);
    } else {
      mailData.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
    }

    this.mails.set(mailData);
    this.isLoading.set(false);
  }, 500);
}
```

**✅ Remove**: All `setTimeout` code and dummy data

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
loadInbox() {
  const userEmail = this.currentUser()?.email;
  if (!userEmail) {
    this.errorMessage.set('User not logged in');
    return;
  }

  this.currentFolder.set('inbox');
  this.isLoading.set(true);
  this.errorMessage.set(null);

  // Backend Call with priority sorting support
  this.mailService.getInboxMails(userEmail).subscribe({
    next: (mails) => {
      // Apply frontend sorting based on priority mode
      if (this.isPriorityMode()) {
        mails.sort((a, b) => a.priority - b.priority);
      } else {
        mails.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
      }
      
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
```

> 💡 **Note**: Backend returns mails unsorted, frontend sorts them based on Priority Mode

---

#### 📍 **Modification 2: loadUserFolders() - Line 177-190**

**Current Code (Simulation)**:
```typescript
loadUserFolders() {
  const email = this.currentUser()?.email;
  if (!email) return;

  // [BACKEND INTERACTION: GET FOLDERS]
  // Request: GET /api/mail/folders/{email}
  // Response: ["Work", "Personal", "Travel"]
  /*
  this.mailService.getUserFolders(email).subscribe({
    next: (folders) => this.userFolders.set(folders),
    error: (e) => console.error('Error loading folders:', e)
  });
  */

  // Frontend Simulation
  this.userFolders.set(['Work', 'Personal', 'Projects']);
}
```

**✅ Remove**: `this.userFolders.set(['Work', 'Personal', 'Projects']);`

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
loadUserFolders() {
  const email = this.currentUser()?.email;
  if (!email) return;

  this.mailService.getUserFolders(email).subscribe({
    next: (folders) => this.userFolders.set(folders),
    error: (e) => console.error('Error loading folders:', e)
  });
}
```

---

#### 📍 **Modification 3: addUserFolder() - Line 200-215**

**Current Code (Simulation)**:
```typescript
addUserFolder() {
  const name = prompt('Enter folder name:');
  if (name && name.trim()) {
    const email = this.currentUser()?.email;

    // [BACKEND INTERACTION: CREATE FOLDER]
    // Request: POST /api/mail/folders/{email}?folderName=xyz
    /*
    if(email) {
      this.mailService.createFolder(email, name).subscribe({
        next: () => this.loadUserFolders(),
        error: (e) => alert('Failed to create folder')
      });
    }
    */

    // Frontend Simulation
    this.userFolders.update(folders => [...folders, name]);
  }
}
```

**✅ Remove**: `this.userFolders.update(folders => [...folders, name]);`

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
addUserFolder() {
  const name = prompt('Enter folder name:');
  if (name && name.trim()) {
    const email = this.currentUser()?.email;

    if(email) {
      this.mailService.createFolder(email, name).subscribe({
        next: () => {
          this.loadUserFolders();
          alert('Folder created successfully!');
        },
        error: (e) => {
          console.error('Error creating folder:', e);
          alert('Failed to create folder');
        }
      });
    }
  }
}
```

---

#### 📍 **Modification 4: deleteUserFolder() - Line 221-240**

**Current Code (Simulation)**:
```typescript
deleteUserFolder(event: Event, folderName: string) {
  event.stopPropagation();
  if (confirm(`Delete "${folderName}"?`)) {
    const email = this.currentUser()?.email;

    // [BACKEND INTERACTION: DELETE FOLDER]
    // Request: DELETE /api/mail/folders/{email}?folderName=xyz
    /*
    if(email) {
      this.mailService.deleteFolder(email, folderName).subscribe({
        next: () => this.loadUserFolders(),
        error: (e) => alert('Failed to delete folder')
      });
    }
    */

    // Frontend Simulation
    this.userFolders.update(folders => folders.filter(f => f !== folderName));
  }
}
```

**✅ Remove**: `this.userFolders.update(...)`

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
deleteUserFolder(event: Event, folderName: string) {
  event.stopPropagation();
  if (confirm(`Delete "${folderName}"?`)) {
    const email = this.currentUser()?.email;

    if(email) {
      this.mailService.deleteFolder(email, folderName).subscribe({
        next: () => {
          this.loadUserFolders();
          alert('Folder deleted successfully!');
        },
        error: (e) => {
          console.error('Error deleting folder:', e);
          alert('Failed to delete folder');
        }
      });
    }
  }
}
```

---

#### 📍 **Modification 5: renameUserFolder() - Line 243-265**

**Current Code (Simulation)**:
```typescript
renameUserFolder(event: Event, oldName: string) {
  event.stopPropagation();

  const newName = prompt(`Rename "${oldName}" to:`, oldName);
  if (newName && newName.trim() && newName !== oldName) {
    const email = this.currentUser()?.email;

    // [BACKEND INTERACTION: RENAME FOLDER]
    // Request: PUT /api/mail/folders/{email}?oldName=x&newName=y
    /*
    if (email) {
      this.mailService.renameFolder(email, oldName, newName).subscribe({
        next: () => this.loadUserFolders(),
        error: (e) => alert('Failed to rename folder')
      });
    }
    */

    // Frontend Simulation
    this.userFolders.update(folders =>
      folders.map(f => f === oldName ? newName : f)
    );
  }
}
```

**✅ Remove**: `this.userFolders.update(...)`

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
renameUserFolder(event: Event, oldName: string) {
  event.stopPropagation();

  const newName = prompt(`Rename "${oldName}" to:`, oldName);
  if (newName && newName.trim() && newName !== oldName) {
    const email = this.currentUser()?.email;

    if (email) {
      this.mailService.renameFolder(email, oldName, newName).subscribe({
        next: () => {
          this.loadUserFolders();
          alert('Folder renamed successfully!');
        },
        error: (e) => {
          console.error('Error renaming folder:', e);
          alert('Failed to rename folder');
        }
      });
    }
  }
}
```

---

#### 📍 **Modification 6: deleteSelectedMails() - Line 151-166**

**Current Code (Simulation)**:
```typescript
deleteSelectedMails() {
  if (this.selectedIds().size === 0) return;
  if (confirm(`Are you sure you want to delete ${this.selectedIds().size} mails?`)) {

    // [BACKEND INTERACTION: BULK DELETE]
    // 1. BE Task: Delete multiple emails.
    // 2. FE Sends: List of mailIds.
    // 3. Request: Loop DELETE /api/mail/{id} OR Single Call POST /api/mail/delete-batch [ids]
    /*
    this.selectedIds().forEach(id => {
       this.mailService.deleteMail(id).subscribe();
    });
    */

    // Frontend Simulation
    this.mails.update(currentMails =>
      currentMails.filter(m => !this.selectedIds().has(m.mailId))
    );
    this.selectedIds.set(new Set());
  }
}
```

**✅ Remove**: Simulation code (`this.mails.update(...)` and `this.selectedIds.set(...)`)

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
deleteSelectedMails() {
  if (this.selectedIds().size === 0) return;
  if (confirm(`Are you sure you want to delete ${this.selectedIds().size} mails?`)) {

    // Backend Call: Delete each mail
    const deletePromises: Observable<any>[] = [];
    this.selectedIds().forEach(id => {
      deletePromises.push(this.mailService.deleteMail(id));
    });

    // Wait for all deletes to complete
    forkJoin(deletePromises).subscribe({
      next: () => {
        this.refresh();
        this.selectedIds.set(new Set());
        alert('Mails deleted successfully!');
      },
      error: (e) => {
        console.error('Error deleting mails:', e);
        alert('Failed to delete some mails');
      }
    });
  }
}
```

> 💡 **Note**: You need to import `forkJoin` from `rxjs`:
> ```typescript
> import { Observable, forkJoin } from 'rxjs';
> ```

---

#### 📍 **Modification 7: moveSelectedMails() - Line 285-303**

**Current Code (Simulation)**:
```typescript
moveSelectedMails(event: Event) {
  const selectElement = event.target as HTMLSelectElement;
  const folderName = selectElement.value;
  selectElement.selectedIndex = 0;

  if (!folderName || this.selectedIds().size === 0) return;

  if (confirm(`Move ${this.selectedIds().size} mails to "${folderName}"?`)) {
    // [BACKEND INTERACTION: MOVE MAILS]
    // Request: PUT /api/mail/move-batch
    // Body: { mailIds: [1, 2], targetFolder: "spam" }
    this.mails.update(currentMails =>
      currentMails.filter(m => !this.selectedIds().has(m.mailId))
    );
    this.selectedIds.set(new Set());
    alert(`Moved to ${folderName}!`);
  }
}
```

**✅ Remove**: Frontend Simulation code

**✅ Add**: Backend Call

**New Code (Backend Integration)**:
```typescript
moveSelectedMails(event: Event) {
  const selectElement = event.target as HTMLSelectElement;
  const folderName = selectElement.value;
  selectElement.selectedIndex = 0;

  if (!folderName || this.selectedIds().size === 0) return;

  if (confirm(`Move ${this.selectedIds().size} mails to "${folderName}"?`)) {
    // Backend Call: Move mails to target folder
    const mailIds = Array.from(this.selectedIds());
    
    // Option 1: If backend has bulk move endpoint
    /*
    this.mailService.moveBatch(mailIds, folderName).subscribe({
      next: () => {
        this.refresh();
        this.selectedIds.set(new Set());
        alert(`Moved to ${folderName}!`);
      },
      error: (e) => console.error('Error moving mails:', e)
    });
    */

    // Option 2: If no bulk endpoint, update each mail individually
    const movePromises: Observable<any>[] = [];
    mailIds.forEach(id => {
      // Assuming you have updateMailFolder in MailService
      movePromises.push(this.mailService.updateMailFolder(id, folderName));
    });

    forkJoin(movePromises).subscribe({
      next: () => {
        this.refresh();
        this.selectedIds.set(new Set());
        alert(`Moved to ${folderName}!`);
      },
      error: (e) => {
        console.error('Error moving mails:', e);
        alert('Failed to move some mails');
      }
    });
  }
}
```

> ⚠️ **Important Note**: This code requires a new method in `MailService`:
> ```typescript
> // In mail-service.ts
> updateMailFolder(mailId: number, folderName: string): Observable<any> {
>   return this.http.put(`${this.apiURL}/${mailId}/folder`, { folderName });
> }
> ```
> 
> **Backend must provide**:
> ```
> PUT /api/mail/{mailId}/folder
> Body: { folderName: "spam" }
> ```

---

#### 📍 **Modification 8: sendComposedMail() - Line 528-569**

**Current Code (Simulation)**:
```typescript
sendComposedMail() {
  if (!this.composedMail.receivers[0] || !this.composedMail.subject) {
    alert('Please fill required fields');
    return;
  }

  // [BACKEND INTERACTION: SEND MAIL WITH ATTACHMENTS]
  // ... comments ...
  
  /*
  const formData = new FormData();
  const emailData = { ... };
  const emailBlob = new Blob([JSON.stringify(emailData)], { type: 'application/json' });
  formData.append('email', emailBlob);
  
  this.selectedAttachments().forEach((file) => {
    formData.append('attachments', file, file.name);
  });
  
  this.mailService.sendMailWithAttachments(formData).subscribe(...);
  */

  // FRONTEND SIMULATION
  console.log('=== Simulating Email Send ===');
  console.log('Body:', this.composedMail);
  console.log('Attachments:', this.selectedAttachments().length);

  setTimeout(() => {
    alert('Email sent successfully!');
    this.refresh();
    this.resetComposeForm();
  }, 1000);
}
```

**✅ Remove**: All simulation code (`console.log`, `setTimeout`)

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
sendComposedMail() {
  if (!this.composedMail.receivers[0] || !this.composedMail.subject) {
    alert('Please fill required fields');
    return;
  }

  // Check if there are attachments
  if (this.selectedAttachments().length > 0) {
    // Send with attachments using FormData
    const formData = new FormData();
    
    const emailData = {
      sender: this.composedMail.sender,
      receivers: this.composedMail.receivers,
      subject: this.composedMail.subject,
      body: this.composedMail.body,
      priority: this.composedMail.priority
    };
    
    const emailBlob = new Blob([JSON.stringify(emailData)], { type: 'application/json' });
    formData.append('email', emailBlob);
    
    this.selectedAttachments().forEach((file) => {
      formData.append('attachments', file, file.name);
    });
    
    this.mailService.sendMailWithAttachments(formData).subscribe({
      next: (res) => {
        console.log('Email sent successfully:', res);
        alert('Email sent successfully!');
        this.refresh();
        this.resetComposeForm();
      },
      error: (e) => {
        console.error('Error sending email:', e);
        alert('Failed to send email. Please try again.');
      }
    });
  } else {
    // Send without attachments (simple JSON)
    this.mailService.sendMail(this.composedMail).subscribe({
      next: (res) => {
        console.log('Email sent successfully:', res);
        alert('Email sent successfully!');
        this.refresh();
        this.resetComposeForm();
      },
      error: (e) => {
        console.error('Error sending email:', e);
        alert('Failed to send email. Please try again.');
      }
    });
  }
}
```

---

#### 📍 **Modification 9: saveDraft() - Line 753-762**

**Current Code (Simulation)**:
```typescript
saveDraft() {
  // [BACKEND INTERACTION: SAVE DRAFT]
  // 1. BE Task: Save email to drafts folder without sending.
  // 2. Request: POST /api/mail/draft
  // 3. Body: ComposeEmailDTO
  console.log('💾 Saving Draft:', this.composedMail);
  alert('Draft saved successfully!');
  this.resetComposeForm();
}
```

**✅ Remove**: Simulation code

**✅ Add**: Backend Call

**New Code (Backend Integration)**:
```typescript
saveDraft() {
  if (!this.composedMail.receivers[0] && !this.composedMail.subject && !this.composedMail.body) {
    alert('Draft is empty');
    return;
  }

  this.mailService.draftEmail(this.composedMail).subscribe({
    next: (res) => {
      console.log('Draft saved successfully:', res);
      alert('Draft saved successfully!');
      this.resetComposeForm();
      this.isComposing = false;
    },
    error: (e) => {
      console.error('Error saving draft:', e);
      alert('Failed to save draft. Please try again.');
    }
  });
}
```

---

#### 📍 **Modification 10: onSearch() - Line 594-634**

**Current Code (Simulation)**:
```typescript
onSearch() {
  const email = this.currentUser()?.email;
  const folder = this.currentFolder();
  const method = this.searchMethod();
  const query = this.searchQuery().trim();

  if (!query) {
    this.refresh();
    return;
  }
  this.isLoading.set(true);

  // [BACKEND INTERACTION: SEARCH]
  // 1. Backend Task: Search emails by criteria.
  // 2. Request: GET /api/mail/search
  // 3. Query Params: ?email=..&folder=..&method=sender&query=xyz
  /*
  if(email) {
      this.mailService.searchMails(email, folder, method, query).subscribe(...);
  }
  */

  setTimeout(() => {
    this.mails.update(current =>
      current.filter(mail => {
        const valueToCheck = (mail as any)[method]?.toString().toLowerCase() || '';
        return valueToCheck.includes(query.toLowerCase());
      })
    );
    this.isLoading.set(false);
  }, 300);
}
```

**✅ Remove**: `setTimeout` code and frontend filtering

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
onSearch() {
  const email = this.currentUser()?.email;
  const folder = this.currentFolder();
  const method = this.searchMethod();
  const query = this.searchQuery().trim();

  if (!query) {
    this.refresh();
    return;
  }

  if (!email) {
    alert('User not logged in');
    return;
  }

  this.isLoading.set(true);

  this.mailService.searchMails(email, folder, method, query).subscribe({
    next: (results) => {
      this.mails.set(results);
      this.isLoading.set(false);
    },
    error: (e) => {
      console.error('Search error:', e);
      this.errorMessage.set('Search failed');
      this.isLoading.set(false);
    }
  });
}
```

---

#### 📍 **Modification 11: loadContacts() - Line 651-673**

**Current Code (Simulation)**:
```typescript
loadContacts() {
  const userEmail = this.currentUser()?.email;

  // [BACKEND INTERACTION: GET CONTACTS]
  // Request: GET /api/contacts?userEmail=...
  // Response: List of Contact objects
  /*
  if (userEmail) {
    this.mailService.getContacts(userEmail).subscribe(contacts => this.contacts.set(contacts));
  }
  */

  // Frontend Simulation
  setTimeout(() => {
    this.contacts.set([
      { id: 1, name: 'Ahmed Hassan', emails: ['ahmed@test.com'] },
      { id: 2, name: 'Sara Mohamed', emails: ['sara@test.com'] }
    ]);
  }, 300);
}
```

**✅ Remove**: `setTimeout` code and dummy data

**✅ Enable**: Commented backend call code

**New Code (Backend Integration)**:
```typescript
loadContacts() {
  const userEmail = this.currentUser()?.email;

  if (userEmail) {
    this.mailService.getContacts(userEmail).subscribe({
      next: (contacts) => {
        this.contacts.set(contacts);
      },
      error: (e) => {
        console.error('Error loading contacts:', e);
      }
    });
  }
}
```

---

#### 📍 **Modification 12: saveContact() - Line 728-752**

**Current Code (Simulation)**:
```typescript
saveContact() {
  const name = this.contactFormName().trim();
  const emailsInput = this.contactFormEmails().trim();
  if (!name || !emailsInput) return;

  const emails = emailsInput.split(',').map(e => e.trim()).filter(e => e.length > 0);
  const editing = this.editingContact();

  if (editing) {
    // [BACKEND INTERACTION: EDIT CONTACT]
    // Request: PUT /api/contacts/{id}
    // Body: { id: 1, name: "...", emails: ["..."] }
    const updatedContact: Contact = { id: editing.id, name, emails };
    
    // Frontend Simulation
    this.contacts.update(c => c.map(x => x.id === editing.id ? updatedContact : x));
    this.resetContactForm();

  } else {
    // [BACKEND INTERACTION: ADD CONTACT]
    // Request: POST /api/contacts?userEmail=...
    // Body: { name: "...", emails: ["..."] }
    const newContact: Contact = { id: Date.now(), name, emails };
    
    // Frontend Simulation
    this.contacts.update(c => [...c, newContact]);
    this.resetContactForm();
  }
}
```

**✅ Remove**: Frontend Simulation code

**✅ Add**: Backend Calls

**New Code (Backend Integration)**:
```typescript
saveContact() {
  const name = this.contactFormName().trim();
  const emailsInput = this.contactFormEmails().trim();
  
  if (!name || !emailsInput) {
    alert('Please fill all fields');
    return;
  }

  const emails = emailsInput.split(',').map(e => e.trim()).filter(e => e.length > 0);
  
  if (emails.length === 0) {
    alert('Please enter at least one email');
    return;
  }

  const editing = this.editingContact();

  if (editing) {
    // Edit existing contact
    const updatedContact: Contact = { id: editing.id, name, emails };
    
    this.mailService.editContact(updatedContact).subscribe({
      next: () => {
        this.loadContacts();
        this.resetContactForm();
        alert('Contact updated successfully!');
      },
      error: (e) => {
        console.error('Error updating contact:', e);
        alert('Failed to update contact');
      }
    });

  } else {
    // Add new contact
    const userEmail = this.currentUser()?.email;
    if (!userEmail) return;

    const newContact: Contact = { id: 0, name, emails }; // ID will be set by backend
    
    this.mailService.addContact(newContact, userEmail).subscribe({
      next: () => {
        this.loadContacts();
        this.resetContactForm();
        alert('Contact added successfully!');
      },
      error: (e) => {
        console.error('Error adding contact:', e);
        alert('Failed to add contact');
      }
    });
  }
}
```

---

#### 📍 **Modification 13: deleteContactById() - Line 754-760**

**Current Code (Simulation)**:
```typescript
deleteContactById(contactId: number) {
  // [BACKEND INTERACTION: DELETE CONTACT]
  // Request: DELETE /api/contacts/{id}
  
  // Frontend Simulation
  this.contacts.update(c => c.filter(x => x.id !== contactId));
}
```

**✅ Remove**: Frontend Simulation code

**✅ Add**: Backend Call

**New Code (Backend Integration)**:
```typescript
deleteContactById(contactId: number) {
  const contact = this.contacts().find(c => c.id === contactId);
  if (!contact) return;

  if (!confirm(`Delete contact "${contact.name}"?`)) return;

  this.mailService.deleteContact(contactId).subscribe({
    next: () => {
      this.loadContacts();
      alert('Contact deleted successfully!');
    },
    error: (e) => {
      console.error('Error deleting contact:', e);
      alert('Failed to delete contact');
    }
  });
}
```

---

## 📝 Integration Steps (Step by Step)

### Phase 1: Preparation
1. ✅ Ensure backend is running on `http://localhost:8080`
2. ✅ Ensure all endpoints exist and are functional
3. ✅ Test each endpoint using Postman or Thunder Client

### Phase 2: Import Dependencies
In file `mail-client/src/app/mail/mail.ts`, ensure you have:

```typescript
import { Observable, forkJoin } from 'rxjs';
```

### Phase 3: Enable Integration (By Priority)

#### 🔴 High Priority (Critical)
1. **loadInbox()** - Display emails
2. **loadSent()** - Already working, no modification needed
3. **loadDrafts()** - Already working, no modification needed
4. **sendComposedMail()** - Send emails

#### 🟡 Medium Priority (Important)
5. **loadUserFolders()** - Folder management
6. **addUserFolder()**
7. **deleteUserFolder()**
8. **renameUserFolder()**
9. **saveDraft()** - Save drafts

#### 🟢 Low Priority (Nice to Have)
10. **onSearch()** - Search
11. **loadContacts()** - Contact management
12. **saveContact()**
13. **deleteContactById()**
14. **deleteSelectedMails()** - Bulk delete
15. **moveSelectedMails()** - Bulk move

### Phase 4: Testing

For each modification, follow these steps:

1. **Enable** commented backend code
2. **Remove** simulation code
3. **Test** the feature in browser
4. **Verify** data is received correctly
5. **Handle** errors properly

---

## ⚠️ Very Important Notes

### 1. CORS Configuration
Backend must allow CORS from `http://localhost:4200`:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

### 2. Timestamp Format
Backend must return timestamp in ISO 8601 format:
```
"2023-12-25T10:30:00"
```

### 3. Multipart File Upload
When sending attachments, backend must accept:
- Part `'email'`: JSON Blob
- Part `'attachments'`: Array of Files

### 4. Error Handling
Backend should return errors in this format:
```json
{
  "error": "Error message here",
  "status": 400
}
```

### 5. Authentication
Currently, user email comes from `this.currentUser()?.email`. If adding authentication:
- Use JWT Token
- Store in localStorage or sessionStorage
- Send in request Headers

---

## 🧪 Testing Checklist

After completing integration, test the following:

- [ ] Load Inbox successfully
- [ ] Load Sent Mails
- [ ] Load Drafts
- [ ] Send email without attachments
- [ ] Send email with attachments
- [ ] Save draft
- [ ] Create new folder
- [ ] Delete folder
- [ ] Rename folder
- [ ] Search emails
- [ ] Add new contact
- [ ] Edit contact
- [ ] Delete contact
- [ ] Delete multiple emails
- [ ] Move emails to another folder
- [ ] Priority Mode (sort by priority)

---

## 📊 Modifications Summary Table

| Function | File | Lines | Action |
|----------|------|-------|--------|
| `loadInbox()` | mail.ts | 311-330 | Remove simulation, enable backend call |
| `loadUserFolders()` | mail.ts | 177-190 | Remove simulation, enable backend call |
| `addUserFolder()` | mail.ts | 200-215 | Remove simulation, enable backend call |
| `deleteUserFolder()` | mail.ts | 221-240 | Remove simulation, enable backend call |
| `renameUserFolder()` | mail.ts | 243-265 | Remove simulation, enable backend call |
| `deleteSelectedMails()` | mail.ts | 151-166 | Remove simulation, add forkJoin logic |
| `moveSelectedMails()` | mail.ts | 285-303 | Remove simulation, add backend call |
| `sendComposedMail()` | mail.ts | 528-569 | Remove simulation, enable FormData logic |
| `saveDraft()` | mail.ts | 753-762 | Remove simulation, enable backend call |
| `onSearch()` | mail.ts | 594-634 | Remove simulation, enable backend call |
| `loadContacts()` | mail.ts | 651-673 | Remove simulation, enable backend call |
| `saveContact()` | mail.ts | 728-752 | Remove simulation, enable backend call |
| `deleteContactById()` | mail.ts | 754-760 | Remove simulation, enable backend call |

---

**Done ✅**

> This file contains **all** modifications required to connect the frontend with the backend.
> 
> **Note**: Do not modify any code currently, just use this file as a reference when starting integration.
