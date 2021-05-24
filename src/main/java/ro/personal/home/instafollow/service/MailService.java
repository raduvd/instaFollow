package ro.personal.home.instafollow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ro.personal.home.instafollow.persistance.model.ProcessResult;

@Service
public class MailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ProcessResultService processResultService;

    public void sendSimpleMessage(String subject, String content) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("instaFollow@home.group");
        message.setTo("r.vancea@yahoo.com");
        message.setSubject(subject);
        message.setText(generateProcessResult() + "\n" + content);
        //TODO replace \n with String.format()
        emailSender.send(message);
    }

    //TODO replace \n with String.format()
    private String generateProcessResult() {
        StringBuilder processResultString = new StringBuilder();
        for (ProcessResult processResult : processResultService.getAllProcessesFromToday()) {
            processResultString.append("\n" + processResult.toString());
        }
        return processResultString.toString();
    }
}
