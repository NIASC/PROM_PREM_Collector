package se.nordicehealth.servlet.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.impl.io.AllIOTests;
import se.nordicehealth.servlet.impl.mail.AllMailTests;
import se.nordicehealth.servlet.impl.request.AllRequestProcessingTests;

@RunWith(Suite.class)
@SuiteClasses({
	ServletLoggerTest.class,
	ClientRequestProcesserTest.class, CryptoTest.class, LocaleSETest.class,
	MySQLDatabaseTest.class, PasswordHandleTest.class, QuestionDataTest.class,
	SHAEncryptionTest.class, UserTest.class, AllMailTests.class, AllIOTests.class,
	AllRequestProcessingTests.class })
public class AllImplementationTests {
}
