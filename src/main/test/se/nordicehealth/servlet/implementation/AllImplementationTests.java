package se.nordicehealth.servlet.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.implementation.io.AllIOTests;
import se.nordicehealth.servlet.implementation.mail.AllMailTests;
import se.nordicehealth.servlet.implementation.requestprocessing.AllRequestProcessingTests;

@RunWith(Suite.class)
@SuiteClasses({ ClientRequestProcesserTest.class, CryptoTest.class, LocaleSETest.class,
	MySQLDatabaseTest.class, PasswordHandleTest.class, QuestionDataTest.class,
	ServletTest.class, SHAEncryptionTest.class, UserTest.class,
	AllMailTests.class, AllIOTests.class, AllRequestProcessingTests.class })
public class AllImplementationTests {
}
