package com.authservice.controller;

import java.io.UnsupportedEncodingException;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

//import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
//import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
//import com.amazonaws.services.simpleemail.model.Body;
//import com.amazonaws.services.simpleemail.model.Content;
//import com.amazonaws.services.simpleemail.model.Destination;
//import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.authservice.message.request.LoginForm;
import com.authservice.message.request.SignUpForm;
import com.authservice.message.response.JwtResponse;
import com.authservice.message.response.ResponseMessage;
import com.authservice.model.Role;
import com.authservice.model.RoleName;
import com.authservice.model.User;
import com.authservice.repository.RoleRepository;
import com.authservice.repository.UserRepository;
import com.authservice.security.jwt.JwtProvider;
//import com.amazonaws.services.simpleemail.model.Message;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthRestAPIs {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtProvider jwtProvider;
	
//	@Autowired
//	AmazonSimpleEmailService amazonSimpleEmailServiceClient;

	Logger logger=LoggerFactory.getLogger(AuthRestAPIs.class);
	
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = jwtProvider.generateJwtToken(authentication);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		logger.info("Login Successful for User",userDetails.getUsername());
		
		return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) throws MessagingException {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity<>(new ResponseMessage("Fail -> Username is already taken!"),
					HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return new ResponseEntity<>(new ResponseMessage("Fail -> Email is already in use!"),
					HttpStatus.BAD_REQUEST);
		}

		// Creating user's account
		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();
 
		strRoles.forEach(role -> {
			switch (role) {
			case "admin":
				Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
				roles.add(adminRole);

				break;
//			case "pm":
//				Role pmRole = roleRepository.findByName(RoleName.ROLE_PM)
//						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
//				roles.add(pmRole);
//
//				break;
			default:
				Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
				roles.add(userRole);
			}
		});

		user.setRoles(roles);
		userRepository.save(user);
		logger.info("User successful registered");

		//TODO- Send email to user
		String From="kanhavmathur05@gmail.com";
		String To=user.getEmail();
		
		String subject="Successful Registration";
		
		String HtmlBody="<h1>Congratulations "+user.getName()+" !!</h1>"
		+"<p>You are successfully registered to Online Clothing Store!</p>";
		
		String textbody="This is text body";
		
//		sendRegistrationEmail(To,subject,HtmlBody);
		
		return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
	}
	
//	public static void sendRegistrationEmail(String recepientEmail, String subject, String body) {
//		try {
//			AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
//					// Replace US_WEST_2 with the AWS Region you're using for
//					// Amazon SES.
//					.withRegion(Regions.US_EAST_2).build();
//			SendEmailRequest request = new SendEmailRequest()
//					.withDestination(new Destination().withToAddresses(recepientEmail))
//					.withMessage(new Message()
//							.withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(body)))
//							.withSubject(new Content().withCharset("UTF-8").withData(subject)))
//					.withSource("kanhavmathur05@gmail.com");
//			client.sendEmail(request);
//			System.out.println("Email sent!");
//		} catch (Exception ex) {
//			System.out.println("The email was not sent. Error message: " + ex.getMessage());
//		}
//	}
	
//	public static void sendRegistrationEmail(String to) throws MessagingException {
//		
//		  String FROM = "kanhavmathur05@gmail.com";
//		  String FROMNAME = "Admin OnlineClothingStore";
//			
//		    // Replace recipient@example.com with a "To" address. If your account 
//		    // is still in the sandbox, this address must be verified.
//		    String TO = to;
//		    
//		    // Replace smtp_username with your Amazon SES SMTP user name.
//		  String SMTP_USERNAME = "AKIAWGUEJCIOWQ4YWU4R";
//		    
//		    // Replace smtp_password with your Amazon SES SMTP password.
//		  String SMTP_PASSWORD = "BHAcuJvf9SjLSl+yQAMUM4OXBjlQmHZcYq2ft7rlL7WQ";
//		    
//		    // The name of the Configuration Set to use for this message.
//		    // If you comment out or remove this variable, you will also need to
//		    // comment out or remove the header below.
////		  String CONFIGSET = "ConfigSet";
//		    
//		    // Amazon SES SMTP host name. This example uses the US West (Oregon) region.
//		    // See https://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html#region-endpoints
//		    // for more information.
//		  String HOST = "email-smtp.us-east-2.amazonaws.com";
//		    
//		    // The port you will connect to on the Amazon SES SMTP endpoint. 
//		  int PORT = 587;
//		    
//		  String SUBJECT = "Registration Successful";
//		
//		  String BODY = String.join(
//		    	    System.getProperty("line.separator"),
//		    	    "<h1>Welcome To Online Clothing store</h1>",
//		    	    "<p>Congratulations!! Your registration was successfull. "
////		    	    , 
////		    	    "<a href='https://github.com/javaee/javamail'>Visit Our Web</a>",
////		    	    " for <a href='https://www.java.com'>Java</a>."
//		    	);
//		
//		    
//		    Properties props = System.getProperties();
//	    	props.put("mail.transport.protocol", "smtp");
//	    	props.put("mail.smtp.port", PORT); 
//	    	props.put("mail.smtp.starttls.enable", "true");
//	    	props.put("mail.smtp.auth", "true");
//
//	        // Create a Session object to represent a mail session with the specified properties. 
//	    	Session session = Session.getDefaultInstance(props);
//
//	        // Create a message with the specified information. 
//	        MimeMessage msg = new MimeMessage(session);
//	        try {
//				msg.setFrom(new InternetAddress(FROM,FROMNAME));
//				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
//		        msg.setSubject(SUBJECT);
//		        msg.setContent(BODY,"text/html");
//		        
//		        // Add a configuration set header. Comment or delete the 
//		        // next line if you are not using a configuration set
////		        msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);
//	        } catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (MessagingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	            
//	        // Create a transport.
//	        Transport transport=null;
//			try {
//				transport = session.getTransport();
//			} catch (NoSuchProviderException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	                    
//	        // Send the message.
//	        try
//	        {
//	            System.out.println("Sending...");
//	            
//	            // Connect to Amazon SES using the SMTP username and password you specified above.
//	            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
//	        	
//	            // Send the email.
//	            transport.sendMessage(msg, msg.getAllRecipients());
//	            System.out.println("Email sent!");
//	        }
//	        catch (Exception ex) {
//	            System.out.println("The email was not sent.");
//	            System.out.println("Error message: " + ex.getMessage());
//	        }
//	        finally
//	        {
//	            // Close and terminate the connection.
//	            transport.close();
//	        }
//	    }
}