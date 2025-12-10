import {Injectable} from '@angular/core';


@Injectable({
    providedIn: 'root'
})
export class MailList{
// dummy data (28 mails)
mailList = [
  {
    from: '0 - shewy',
    subject: 'Welcome to MansyMail',
    content: 'Hi Ahmed, welcome! Let us know if you need any help.',
    date: new Date(2024, 10, 25),
    hasAttachments: false,
    isRead: false
  },
  {
    from: '1 - hr@company.com',
    subject: 'Interview Schedule',
    content: 'Your technical interview is scheduled for next Monday at 10 AM.',
    date: new Date(2024, 10, 24),
    hasAttachments: true,
    isRead: true
  },
  {
    from: '2 - github',
    subject: 'Security alert',
    content: 'We detected a new sign-in to your GitHub account.',
    date: new Date(2024, 10, 23),
    hasAttachments: false,
    isRead: true
  },
  {
    from: '3 - medium',
    subject: 'Top JavaScript articles this week',
    content: 'Here are the top trending JavaScript articles for you.',
    date: new Date(2024, 10, 22),
    hasAttachments: false,
    isRead: false
  },
  {
    from: '4 - university@alexu.edu.eg',
    subject: 'Exam Schedule Released',
    content: 'The final exam schedule has been released. Please check the portal.',
    date: new Date(2024, 10, 21),
    hasAttachments: true,
    isRead: false
  },
  {
    from: '5 - noreply@google.com',
    subject: 'New login detected',
    content: 'A new login was detected on your Google account.',
    date: new Date(2024, 10, 20),
    hasAttachments: false,
    isRead: true
  },
  {
    from: '6 - linkedin',
    subject: 'You appeared in 7 searches',
    content: 'Your profile appeared in 7 searches this week.',
    date: new Date(2024, 10, 19),
    hasAttachments: false,
    isRead: true
  }
];

}