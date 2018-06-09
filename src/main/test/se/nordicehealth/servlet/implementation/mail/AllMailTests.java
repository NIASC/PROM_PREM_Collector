package se.nordicehealth.servlet.implementation.mail;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.implementation.mail.emails.AllEMailTests;

@RunWith(Suite.class)
@SuiteClasses({ MessageGeneratorTest.class, CredentialsTest.class, MailManTest.class, AllEMailTests.class })
public class AllMailTests {
}
