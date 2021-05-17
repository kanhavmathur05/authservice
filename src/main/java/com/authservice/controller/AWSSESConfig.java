//package com.authservice.controller;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
//import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
//
//@Configuration
//public class AWSSESConfig {
//
//	public static final String SECRET_KEY = "6OeAd87rX0oHQE9JD4aMHWiwUSXq5BqFd32FEkkO";
//	public static final String ACCESS_KEY = "AKIAWGUEJCIORNVIPKNG";
//
//	@Primary
//	@Bean
//	public AmazonSimpleEmailService getSESClient() {
//		return AmazonSimpleEmailServiceClientBuilder.standard()
//				// Replace US_WEST_2 with the AWS Region you're using for
//				// Amazon SES.
//				.withRegion(Regions.US_EAST_2).withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY))).build();		
//	}
//	
//
//	
////	@Primary
////	@Bean
////	public AmazonSNSClient getSnsClient() {
////		return (AmazonSNSClient) AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_2)
////				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
////				.build();
////	}
//}
