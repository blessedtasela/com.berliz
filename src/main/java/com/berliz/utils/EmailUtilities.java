package com.berliz.utils;

import com.berliz.JWT.JWTFilter;
import com.berliz.JWT.JWTUtility;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.User;
import com.berliz.repositories.UserRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Async
@Service
public class EmailUtilities {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    JWTUtility jwtUtility;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JWTFilter jwtFilter;

    public void composeBulkMail(String to, String subject, String body, List<String> list) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("berlizworld@gmail.com");
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(body);

        if (list != null && list.size() > 0) {
            mailMessage.setCc(getCcArray(list));
        }
        mailSender.send(mailMessage);
    }

    public void composeSimpleMail(String to, String subject, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("berlizworld@gmail.com");
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        mailSender.send(mailMessage);
    }

    private String[] getCcArray(List<String> ccList) {
        String[] cc = new String[ccList.size()];
        for (int i = 0; i < ccList.size(); i++) {
            cc[i] = ccList.get(i);
        }
        return cc;
    }


    public void resetPasswordMail(String to, String subject) throws MessagingException {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
        messageHelper.setFrom("berlizworld@gmail.com");
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        String passwordResetToken = jwtUtility.generatePasswordResetToken(to);

        User user = userRepo.findByEmail(to);
        if (user != null) {
            user.setToken(passwordResetToken);
            userRepo.save(user);
        }

        String resetPasswordLink = "\n\n<br><a href='" + BerlizConstants.BERLIZ_URL + "/login/reset-password?token=" + passwordResetToken + "'>click here to reset your password</a>";

        String emailBody = "Hello,<br>Your password reset request has been received." +
                " To reset your password, please" + resetPasswordLink + "." +
                "<br>If you did not initiate this request, please ignore this email." +
                "<br>Thank you.";
        mailMessage.setContent(emailBody, "text/html");
        mailSender.send(mailMessage);
    }

    public void validateSignupMail(String to, String subject) throws MessagingException {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
        messageHelper.setFrom("berlizworld@gmail.com");
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        String confirmAccountToken = jwtUtility.generateConfirmAccountToken();

        User user = userRepo.findByEmail(to);
        if (user != null) {
            user.setToken(confirmAccountToken);
            userRepo.save(user);
        }

        String confirmAccountLink = "\n\n<br><a href='http://localhost:4200/login/activate-account'>click here to activate your account</a>";

        String emailBody = "Hello,<br> use this code to verify your account.\n\n " + confirmAccountToken +
                "\n, please" + confirmAccountLink + "." +
                "<br>If you did not initiate this request, please ignore this email." +
                "<br>Thank you.";
        mailMessage.setContent(emailBody, "text/html");
        mailSender.send(mailMessage);
    }


    public void sendAccountDeletedMail(String user, List<String> allAdmins) {
        allAdmins.remove(jwtFilter.getCurrentUser());
        if (jwtFilter.isAdmin()) {
            composeBulkMail(user, "Account deleted", "User: " + user + " account has been deleted by admin: " + jwtFilter.getCurrentUser(), allAdmins);
        } else {
            composeSimpleMail(user, "Account deleted", "Hello " + user + ", you have deleted your account permanently by");
        }
    }

    public void sendRoleMailToUser(String role, String email) {
        String subject;
        String message;

        switch (role.toLowerCase()) {
            case "user":
                subject = "Your Account Role Has Been Upgraded!";
                message = "Congratulations! Your account role has been changed to a higher level. Enjoy new privileges!";
                break;
            case "client":
                subject = "You're Now a Valued Client!";
                message = "Welcome to our client community! Your account has been upgraded to a client's account.";
                break;
            case "center":
                subject = "Welcome to Berliz gym center!";
                message = "Exciting news! Your account has been upgraded to a center  account. Let's get started!";
                break;
            case "trainer":
                subject = "You're Now a Certified Trainer!";
                message = "Great achievement! Your account has been upgraded to a certified trainer's account.";
                break;
            case "store":
                subject = "Welcome to Berliz store world!";
                message = "Get ready to showcase your products! Your account has been upgraded to a store account.";
                break;
            case "driver":
                subject = "Welcome to Driver's Paradise!";
                message = "Get ready to showcase your delivery skills! Your account has been upgraded to a driver's account.";
                break;
            case "partner":
                subject = "Welcome to Berliz partnership!";
                message = "Get ready to transform your life, you are not a potential partner with Berliz. Get ready to complete" +
                        "your application and become an official partner.";
                break;
            case "admin":
                subject = "Welcome to Admin Paradise!";
                message = "What a privilege to behold! you are now an admin in Berliz. Get ready to transform our lives with your skills and " +
                        "passion.";
                break;
            default:
                return;
        }

        composeSimpleMail(email, subject, message);
    }

    public void sendRoleMailToAdmins(String role, String user, List<String> allAdmins) {
        allAdmins.remove(jwtFilter.getCurrentUser());
        if (role != null) {
            String subject;
            String message;

            switch (role.toLowerCase()) {
                case "admin":
                    subject = "Admin role activated";
                    break;
                case "user":
                    subject = "User role activated";
                    break;
                case "client":
                    subject = "Client role activated";
                    break;
                case "center":
                    subject = "Center role activated";
                    break;
                case "trainer":
                    subject = "Trainer role activated";
                    break;
                case "driver":
                    subject = "Driver role activated";
                    break;
                case "partner":
                    subject = "Partner role activated";
                    break;
                case "store":
                    subject = "Store role activated";
                    break;
                default:
                    subject = "Undefined role activated";
                    return;
            }

            // Compose the message
            message = "USER: " + user + "\n role has been changed by \nADMIN: " + jwtFilter.getCurrentUser();

            // Send the email
            composeBulkMail(jwtFilter.getCurrentUser(), subject, message, allAdmins);
        }
    }

    /**
     * Sends a contact us confirmation email to the user based on the status of their message.
     *
     * @param status The status of the user's message ("true" for reviewed, "false" for pending).
     * @param email  The email address of the user to send the message to.
     */
    public void sendContactUsMailToUser(String subject, String name, String status, String email, String message) {
        String body;

        if (status != null && status.equalsIgnoreCase("false")) {
            body = "🎉 Great news, " + name + "!\n\n" +
                    "We've received your message and our team is thrilled to assist you. " +
                    "🌟 Your inquiry is important to us, and we'll be working diligently to " +
                    "provide you with the information you need. Expect to hear from us soon! 🚀📬";
        } else {
            body = "Dear " + name + ",\n\n" +
                    "We're delighted to inform you that your message has been successfully reviewed by our team.\n\n" +
                    message + "" + " \n\n" +
                    " 📝 Thank you for reaching out to us! If you have any further questions or " +
                    "need assistance, feel free to contact us anytime. We're here to help! 🤝";
        }
        composeSimpleMail(email, subject, body);
    }


    /**
     * Sends a contact us notification email to all administrators based on the status of the user's message.
     *
     * @param status    The status of the user's message ("true" for reviewed, "false" for pending).
     * @param user      The email address of the user who sent the message.
     * @param allAdmins The list of all administrators' email addresses.
     */
    public void sendContactUsMailToAdmins(String status, String user, List<String> allAdmins) {
        if (jwtFilter.isAdmin()) {
            String subject;
            String body;

            if (status != null && status.equalsIgnoreCase("false")) {
                subject = "New Contact Us Message Awaiting Review";
                body = "Hello Admins,\n\n" +
                        "A new contact us message has arrived from user " + user + ". It is currently pending review. " +
                        "Please log in to the admin panel to review and respond to the user's inquiry.\n\n" +
                        "Thank you!";
            } else {
                subject = "Contact Us Message Reviewed";
                body = "Hello Admins,\n\n" +
                        "The contact us message from user " + user + " has been successfully reviewed by Admin: " +
                        jwtFilter.getCurrentUser() + ". The user has received a response to their inquiry.\n\n" +
                        "Thank you for your prompt attention to this matter!";
            }
            composeBulkMail(jwtFilter.getCurrentUser(), subject, body, allAdmins);
        }
    }


    /**
     * Sends an email notification to the user whose account status has changed.
     *
     * @param status The new status of the account ("true" for active, "false" for disabled)
     * @param email  The email address of the user
     */
    /**
     * Sends an email notification to the user whose account status has changed.
     *
     * @param status      The new status of the account ("true" for active, "false" for disabled)
     * @param accountType The type of user (e.g., "User", "Admin", "Center", "Trainer", "Partner", "Store", "Driver", "Client")
     * @param email       The email address of the user
     */
    public void sendStatusMailToUser(String status, String accountType, String email) {
        String subject;
        String message;

        // Determine the subject and message based on the account status and user type
        if ("true".equals(status)) {
            subject = "Account status changed";
            message = "Congratulations. Your " + accountType + " account has been successfully activated.";
        } else {
            subject = "Account status changed";
            message = "Sorry. Your " + accountType + " account has been deactivated.";
        }

        // Send the email notification
        composeSimpleMail(email, subject, message);
    }

    /**
     * Sends email notifications to all administrators regarding the change in account status.
     *
     * @param status    The new status of the account ("true" for active, "false" for disabled)
     * @param userEmail The email address of the user whose account status changed
     * @param allAdmins List of email addresses of all administrators
     */
    /**
     * Sends an email notification to all administrators based on account status and type.
     *
     * @param status      The new status of the account ("true" for active, "false" for disabled)
     * @param userEmail   The email address of the user whose account status changed
     * @param allAdmins   List of email addresses of all administrators
     * @param accountType The type of the account (e.g., "Trainer", "Admin", etc.)
     */
    public void sendStatusMailToAdmins(String status, String userEmail, List<String> allAdmins, String accountType) {
        // Remove the current user's email from the list of administrators
        allAdmins.remove(jwtFilter.getCurrentUser());

        // Check if the current user is an admin
        if (jwtFilter.isAdmin()) {
            String subject;
            String message;

            // Determine the subject and message based on the account status and type
            if ("true".equalsIgnoreCase(status)) {
                subject = accountType + " account activated";
                message = accountType + ": " + userEmail + " account has been approved by: " + jwtFilter.getCurrentUser();
            } else {
                subject = accountType + " account disabled";
                message = accountType + ": " + userEmail + " account has been disabled by: " + jwtFilter.getCurrentUser();
            }

            // Send an email notification to all administrators
            composeBulkMail(
                    jwtFilter.getCurrentUser(),
                    subject,
                    message,
                    allAdmins
            );
        }
    }

    /**
     * Sends an email to the user to inform them about the status of their application.
     *
     * @param status The status of the application ("true" for approved, "false" for rejected)
     * @param email  The email address of the user
     * @param role   The role of the user (e.g., "trainer", "driver")
     */

    public void sendPartnerShipStatusMailToUser(String status, String email, String role) throws MessagingException {
        String message;
        String token = jwtUtility.generatePasswordResetToken(email);
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true);
        messageHelper.setFrom("berlizworld@gmail.com");
        messageHelper.setTo(email);
        messageHelper.setSubject("Berliz application status");

        User user = userRepo.findByEmail(email);
        if (user != null) {
            user.setToken(token);
            userRepo.save(user);
        }
        String completePartnerRegistration = "<a href='" + BerlizConstants.BERLIZ_URL +
                "/dashboard/partnership?token=" + token + "'>Click here to complete application</a>";

        // Determine the subject and message based on the application status
        if ("true".equals(status)) {
            message = "<html><body>" +
                    "<p>Congratulations. Your partnership request as " + role +
                    " has been approved, you can now set up your account by following this link:</p>" +
                    completePartnerRegistration +
                    "</body></html>";
        } else {
            message = "Sorry. Your Request was rejected. You will be contacted soon." +
                    "If you think this was a mistake, please send an email to berlizworld@gmail.com.";
        }

        // Send the email
        mailMessage.setContent(message, "text/html");
        mailSender.send(mailMessage);
    }

    /**
     * Sends an email notification to the user whose partnership application was rejected.
     *
     * @param email The email address of the user
     * @param role  The role for which the partnership application was made
     */
    public void sendPartnershipFailedMail(String email, String role) {
        String subject = "Application Status"; // Subject of the email
        String message = "Sorry. Your application for " + role + " partnership was not approved. " +
                "You can reapply after 2 weeks."; // Email message content

        // Send the email using the emailUtilities instance
        composeSimpleMail(email, subject, message);
    }

    /**
     * Sends notification emails to all admins about the status of a partner's application.
     *
     * @param status    The status of the application ("true" for approved, "false" for rejected)
     * @param user      The user's email associated with the partner's application
     * @param allAdmins A list of email addresses of all admin users
     */
    public void sendPartnerShipStatusMailToAdmins(String status, String user, List<String> allAdmins) {
        // Remove the current user's email from the list of admins to avoid sending duplicate notifications
        allAdmins.remove(jwtFilter.getCurrentUser());

        // Construct email subject and message based on the application status
        String subject;
        String message;
        String adminUsername = jwtFilter.getCurrentUser(); // Admin who initiated the status update

        if (status != null && status.equalsIgnoreCase("true")) {
            subject = "Application Approved";
            message = "Partner account for user: " + user + " has been approved by admin: " + adminUsername;
        } else {
            subject = "Application Rejected";
            message = "Partner account for user: " + user + " has been rejected by admin: " + adminUsername;
        }

        // Send notification emails to all admins
        composeBulkMail(adminUsername, subject, message, allAdmins);
    }

    /**
     * Sends a newsletter status notification email to the user based on the status.
     *
     * @param status The status of the newsletter subscription ("true" for subscribed, "false" for unsubscribed)
     * @param user   The user's email address
     */
    public void sendNewsletterStatusMailToUser(String status, String user) {
        String body = ", \uD83C\uDF89 You're now part of the Berliz Newsletter family! " +
                "\uD83D\uDC8C Get ready for a thrilling journey filled with exclusive offers, exciting updates," +
                " and amazing surprises. Your inbox is about to become a treasure trove of inspiration and " +
                "information. Stay tuned, stay connected, and let the adventure begin! \uD83C\uDF1F\uD83D\uDCEC\"";

        if (status != null && status.equalsIgnoreCase("true")) {
            composeSimpleMail(user, "Berliz Newsletter", "\nCongratulations! " + user + body);
        } else {
            composeSimpleMail(user, "Unsubscribed", "Hello " + user +
                    ", you have successfully unsubscribed from our newsletter service");
        }
    }

    /**
     * Sends a newsletter to a particular user.
     *
     * @param email The user's email address
     */
    public void sendNewsletterMail(String email, String body, String subject) {
        composeSimpleMail(email, subject, body);
    }

    /**
     * Sends a newsletter to users.
     *
     * @param emails The list of user's email address
     */
    public void sendBulkNewsletterMail(List<String> emails, String body, String subject) {
        composeBulkMail(jwtFilter.getCurrentUser(), subject, body, emails);
    }
}
