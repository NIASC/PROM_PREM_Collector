package se.nordicehealth.servlet.impl.mail.emails;

import java.util.List;

public class EMail {
	public EMail(List<String> recipients, String subject, String body, String format) {
		this.m_recipients = recipients;
		this.m_subject = subject;
		this.m_body = body;
		this.m_format = format;
	}
	public List<String> getRecipients() { return m_recipients; }
	public String getSubject() { return m_subject; }
	public String getBody() { return m_body; }
	public String getFormat() { return m_format; }
	
	private List<String> m_recipients;
	private String m_subject;
	private String m_body;
	private String m_format;
}
