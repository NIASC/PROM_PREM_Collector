package niasc.servlet.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.implementation.io.AllIOTests;
import niasc.servlet.implementation.mail.AllMailTests;
import niasc.servlet.implementation.requestprocessing.AllRequestProcessingTests;

@RunWith(Suite.class)
@SuiteClasses({ ClientRequestProcesserTest.class, CryptoTest.class, LocaleSETest.class,
	MySQLDatabaseTest.class, PasswordHandleTest.class, QuestionDataTest.class,
	ServletTest.class, SHAEncryptionTest.class, UserTest.class,
	AllMailTests.class, AllIOTests.class, AllRequestProcessingTests.class })
public class AllImplementationTests {
}
