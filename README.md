# Mail System API - Frontend ↔️ Backend

## Data Structure

### Mail Object
```json
{
  "mailId": 1,
  "sender": "user1@example.com",
  "receiver": "user2@example.com",
  "subject": "Meeting",
  "body": "Email content...",
  "timestamp": "2025-12-10T10:30:00",
  "priority": 1,
  "folderName": "inbox",
  "isRead": false,
  "attachments": [
    {
      "id": 1,
      "fileName": "file.pdf",
      "contentType": "application/pdf",
      "data": null
    }
  ]
}
```
### Send/Draft Request
```json
{
  "sender": "user1@example.com",
  "receivers": ["user2@example.com", "user3@example.com"],
  "subject": "Meeting",
  "body": "Email content...",
  "priority": 1
}
```

---

## Required Backend Endpoints

### ✅ Already Exist:
```
POST /api/mail/send       - Send email
POST /api/mail/draft      - Save draft
```

### 🆕 Need to Add:
```
GET  /api/mail/inbox/{userEmail}                   - Get inbox
GET  /api/mail/sent/{userEmail}                    - Get sent
GET  /api/mail/drafts/{userEmail}                  - Get drafts
GET  /api/mail/folder/{userEmail}/{folderName}     - Get folder
GET  /api/mail/{mailId}                            - Get by ID
PUT  /api/mail/{mailId}/read                       - Mark as read
DELETE /api/mail/{mailId}                          - Delete (→ trash)
```

**Folders:** inbox, sent, drafts, trash, spam, starred, important, scheduled, all

---

## Implementation Summary

### MailController.java
```java
@GetMapping("/inbox/{userEmail}")
public List<Mail> getInboxMails(@PathVariable String userEmail) {
    return mailService.getInboxMails(userEmail);
}
// ... repeat for sent, drafts, folder, mailId, read, delete
```

### MailService.java
```java
public List<Mail> getInboxMails(String userEmail) {
    return mailRepository.findByReceiverAndFolderNameOrderByTimestampDesc(userEmail, "inbox");
}
// ... similar for other methods
```

### MailRepository.java
```java
List<Mail> findByReceiverAndFolderNameOrderByTimestampDesc(String receiver, String folderName);
List<Mail> findBySenderAndFolderNameOrderByTimestampDesc(String sender, String folderName);
```

---

## Important Notes

1. **CORS:** Enable `@CrossOrigin(origins = "http://localhost:4200")`
2. **Sorting:** All GET endpoints: `ORDER BY timestamp DESC`
3. **Delete:** Soft delete (set `folderName = "trash"`)
4. **Response Format:** Return `List<Mail>` for collections, `Mail` for single

---

## Latest Backend Changes ✅

### Added Files:
None - only modified existing files

### Modified Files:

#### 1. MailController.java
**Added 7 new REST endpoints:**
```java
@GetMapping("/inbox/{userEmail}")           // Get inbox emails
@GetMapping("/sent/{userEmail}")            // Get sent emails
@GetMapping("/drafts/{userEmail}")          // Get draft emails
@GetMapping("/folder/{userEmail}/{folder}") // Get emails by folder name
@GetMapping("/{mailId}")                    // Get single email by ID
@PutMapping("/{mailId}/read")               // Mark email as read
@DeleteMapping("/{mailId}")                 // Delete email (soft delete to trash)
```

#### 2. MailService.java
**Added 7 service methods:**
- `getInboxMails(String userEmail)` - Returns inbox emails sorted by timestamp DESC
- `getSentMails(String userEmail)` - Returns sent emails sorted by timestamp DESC
- `getDraftMails(String userEmail)` - Returns draft emails sorted by timestamp DESC
- `getMailsByFolder(String userEmail, String folderName)` - Returns emails by folder, handles "all" folder
- `getMailById(Long mailId)` - Returns single email, throws exception if not found
- `markAsRead(Long mailId)` - Sets isRead=true and saves
- `deleteMail(Long mailId)` - Sets folderName="trash" (soft delete)

#### 3. MailRepository.java
**Added 3 query methods:**
```java
findByReceiverAndFolderNameOrderByTimestampDesc(String receiver, String folderName)
findBySenderAndFolderNameOrderByTimestampDesc(String sender, String folderName)
findByReceiverOrSenderOrderByTimestampDesc(String email1, String email2) // Custom @Query
```

All methods return results sorted newest-first.

### Build Status:
✅ `mvn clean compile` - **SUCCESS**

---

**Integration complete. Both frontend and backend are ready! 🚀**
