package se.nordicehealth.servlet.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.PasswordHandle;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.zzphony.PhonyEncryption;

public class PasswordHandleTest {
	PasswordHandle ph;
	User user;

	@Before
	public void setUp() throws Exception {
		ph = new PasswordHandle();
		user = new User(new PhonyEncryption());
	}

	@Test
	public void testNewPassError() {
		user.password = "p4ssw0rd";
		
		Assert.assertEquals(Packet.INVALID_DETAILS,
				ph.newPassError(null, "p4ssw0rd", "s3cr3t", "s3cr3t"));
		Assert.assertEquals(Packet.INVALID_DETAILS,
				ph.newPassError(user, "p4ssword", "s3cr3t", "s3cr3t"));
		Assert.assertEquals(Packet.MISMATCH_NEW,
				ph.newPassError(user, "p4ssw0rd", "s3cr3t", "s3cret"));

		Assert.assertEquals(Packet.PASSWORD_INVALID_LENGTH,
				ph.newPassError(user, "p4ssw0rd", "short", "short"));
		Assert.assertEquals(Packet.PASSWORD_INVALID_LENGTH,
				ph.newPassError(user, "p4ssw0rd", "sh0rt", "sh0rt"));
		Assert.assertEquals(Packet.PASSWORD_INVALID_LENGTH,
				ph.newPassError(user, "p4ssw0rd", "sH0rt", "sH0rt"));
		Assert.assertEquals(Packet.PASSWORD_INVALID_LENGTH,
				ph.newPassError(user, "p4ssw0rd", "averyverylongpasswordtoolongtopass", "averyverylongpasswordtoolongtopass"));
		Assert.assertEquals(Packet.PASSWORD_INVALID_LENGTH,
				ph.newPassError(user, "p4ssw0rd", "averyveryl0ngpasswordtoolongtopass", "averyveryl0ngpasswordtoolongtopass"));
		Assert.assertEquals(Packet.PASSWORD_INVALID_LENGTH,
				ph.newPassError(user, "p4ssw0rd", "@V3ryV3ryL0ngP4$$w0rdT0OL0ngToP@ss", "@V3ryV3ryL0ngP4$$w0rdT0OL0ngToP@ss"));

		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "toosimple", "toosimple"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "TOOSIMPLE", "TOOSIMPLE"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "2987353874", "2987353874"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "!@#!$#%^_)*", "!@#!$#%^_)*"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "Español", "Español"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "日本語日本語", "日本語日本語"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "Русский", "Русский"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "Français", "Français"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "Português", "Português"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "Ελληνικά", "Ελληνικά"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "中文中文中文", "中文中文中文"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "العربية", "العربية"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "עבריתעברית", "עבריתעברית"));
		Assert.assertEquals(Packet.PASSWORD_SIMPLE,
				ph.newPassError(user, "p4ssw0rd", "ქართული", "ქართული"));
		
		Assert.assertEquals(Packet.SUCCESS,
				ph.newPassError(user, "p4ssw0rd", "notTooSimple", "notTooSimple"));
		Assert.assertEquals(Packet.SUCCESS,
				ph.newPassError(user, "p4ssw0rd", "n0tt00s1mple", "n0tt00s1mple"));
		Assert.assertEquals(Packet.SUCCESS,
				ph.newPassError(user, "p4ssw0rd", "not_too_$imple", "not_too_$imple"));
		Assert.assertEquals(Packet.SUCCESS,
				ph.newPassError(user, "p4ssw0rd", "not_too_$imple", "not_too_$imple"));
		Assert.assertEquals(Packet.SUCCESS,
				ph.newPassError(user, "p4ssw0rd", "N0t_tO0_$1mpLe", "N0t_tO0_$1mpLe"));
	}

}
