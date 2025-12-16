package eg.edu.alexu.cse.mail_server.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Refactor instead of using strings
public class MailFilterDTO {
    private Long userId; // Required: The user performing the search
    private Optional<List<String>> sender = Optional.empty();
    private Optional<List<String>> receiver = Optional.empty();
    private Optional<String> subject = Optional.empty();
    private Optional<String> body = Optional.empty();
    private Optional<LocalDateTime> exactDate = Optional.empty();
    private Optional<LocalDateTime> afterDate = Optional.empty();
    private Optional<LocalDateTime> beforeDate = Optional.empty();
    private Optional<Boolean> isRead = Optional.empty();
    private Optional<Integer> priority = Optional.empty();
    private Optional<Boolean> hasAttachment  = Optional.empty();
    private Optional<String> folder = Optional.empty();
    // Attachement will be added soon


    public MailFilterDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Optional<List<String>> getSender() {
        return sender;
    }

    public void setSender(Optional<List<String>> sender) {
        this.sender = sender;
    }

    public Optional<List<String>> getReceiver() {
        return receiver;
    }

    public void setReceiver(Optional<List<String>> receiver) {
        this.receiver = receiver;
    }

    public Optional<String> getSubject() {
        return subject;
    }

    public void setSubject(Optional<String> subject) {
        this.subject = subject;
    }

    public Optional<String> getBody() {
        return body;
    }

    public void setBody(Optional<String> body) {
        this.body = body;
    }

    public Optional<LocalDateTime> getExactDate() {
        return exactDate;
    }

    public void setExactDate(Optional<LocalDateTime> exactDate) {
        this.exactDate = exactDate;
    }

    public Optional<LocalDateTime> getAfterDate() {
        return afterDate;
    }

    public void setAfterDate(Optional<LocalDateTime> afterDate) {
        this.afterDate = afterDate;
    }

    public Optional<LocalDateTime> getBeforeDate() {
        return beforeDate;
    }

    public void setBeforeDate(Optional<LocalDateTime> beforeDate) {
        this.beforeDate = beforeDate;
    }

    public Optional<Boolean> getIsRead() {
        return isRead;
    }

    public void setIsRead(Optional<Boolean> isRead) {
        this.isRead = isRead;
    }

    public Optional<Integer> getPriority() {
        return priority;
    }

    public void setPriority(Optional<Integer> priority) {
        this.priority = priority;
    }

    public Optional<Boolean> getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(Optional<Boolean> hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public Optional<String> getFolder() {
        return folder;
    }

    public void setFolder(Optional<String> folder) {
        this.folder = folder;
    }
}