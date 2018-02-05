package common.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import common.Utilities;

public abstract class Constants
{
	public static final URL SERVER_URL;
	
	static
	{
		URL url = null;
		try (InputStream is = Utilities.getResourceStream(Constants.class, "common/implementation/settings.ini")) {
			Properties props = new Properties();
			props.load(is);
			url = new URL(props.getProperty("srvlturl"));
			props.clear();
		}
		catch (IOException | IllegalArgumentException _e) { }
		SERVER_URL = url;
	}

	public enum Packet {
		__NULL__,
		TYPE,
		DATA;

		public enum Types {
			__NULL__,
			PING,
			VALIDATE_PID,
			ADD_QANS,
			SET_PASSWORD,
			LOAD_Q,
			LOAD_QR_DATE,
			LOAD_QR,
			REQ_REGISTR,
			REQ_LOGIN,
			REQ_LOGOUT;
		}

		public enum Data {
			__NULL__;

			public enum Ping {
				__NULL__,
				RESPONSE,
				DETAILS;

				public enum Details {
					__NULL__,
					UID
				}

				public enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
			} // Ping

			public enum ValidatePatientID {
				__NULL__,
				RESPONSE,
				PATIENT,
				DETAIL;

				public enum Details {
					__NULL__,
					UID
				}

				public enum Patient {
					__NULL__,
					PERSONAL_ID
				}

				public enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
			} // ValidatePatientID

			public enum AddQuestionnaireAnswers {
				__NULL__,
				RESPONSE,
				DETAILS,
				PATIENT,
				QUESTIONS;

				public enum Details {
					__NULL__,
					UID
				}

				public enum Patient {
					__NULL__,
					FORENAME,
					SURNAME,
					PERSONAL_ID
				}

				public enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
			} // AddQuestionnaireAnswers

			public enum SetPassword {
				__NULL__,
				RESPONSE,
				DETAILS;

				public enum Details {
					__NULL__,
					UID,
					OLD_PASSWORD,
					NEW_PASSWORD1,
					NEW_PASSWORD2
				}

				public enum Response {
					__NULL__,
					ERROR,
					SUCCESS,
					INVALID_DETAILS,
					MISMATCH_NEW,
					PASSWORD_SHORT,
					PASSWORD_SIMPLE
				}
			} // SetPassword

			public enum LoadQuestions {
				__NULL__,
				QUESTIONS;

				public enum Question {
					__NULL__,
					OPTIONS,
					TYPE,
					ID,
					QUESTION,
					DESCRIPTION,
					OPTIONAL,
					MAX_VAL,
					MIN_VAL;

					public enum Optional {
						__NULL__,
						YES,
						NO
					}
				}
			} // LoadQuestions

			public enum LoadQResultDates {
				__NULL__,
				DATES,
				DETAILS;

				public enum Details {
					__NULL__,
					UID
				}
			} // LoadQResultDates

			public enum LoadQResults {
				__NULL__,
				RESULTS,
				QUESTIONS,
				DETAILS,
				BEGIN,
				END;

				public enum Details {
					__NULL__,
					UID
				}
			} // LoadQResults

			public enum RequestRegistration {
				__NULL__,
				RESPONSE,
				DETAILS;

				public enum Details {
					__NULL__,
					NAME,
					EMAIL,
					CLINIC
				}

				public enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
			} // RequestRegistration

			public enum RequestLogin {
				__NULL__,
				RESPONSE,
				UPDATE_PASSWORD,
				UID,
				DETAILS;

				public enum Response {
					__NULL__,
					ERROR,
					FAIL,
					SUCCESS,
					SERVER_FULL,
					ALREADY_ONLINE,
					INVALID_DETAILS,
					UPDATE_PASSWORD
				}

				public enum Details {
					__NULL__,
					USERNAME,
					PASSWORD
				}

				public enum UpdatePassword {
					__NULL__,
					YES,
					NO
				}
			} // RequestLogin

			public enum RequestLogout {
				__NULL__,
				RESPONSE,
				DETAILS;

				public enum Response {
					__NULL__,
					ERROR,
					SUCCESS
				}

				public enum Details {
					__NULL__,
					UID
				}
			} // RequestLogout
		} // Packets
	}
	
	public static enum QuestionTypes {
		__NULL__,
		SINGLE_OPTION,
		MULTIPLE_OPTION,
		SLIDER,
		AREA
	}

	public static <T extends Enum<T>> T getEnum(T v[], int ordinal) {
		for (T t : v) {
			if (t.ordinal() == ordinal) {
				return t;
			}
		}
		return v[0];
	}

	public static <T extends Enum<T>> T getEnum(T v[], String ordinalStr) throws NumberFormatException {
		return getEnum(v, Integer.parseInt(ordinalStr));
	}

	public static <T extends Enum<T>> boolean equal(T lhs, T rhs) {
		return lhs.compareTo(rhs) == 0;
	}
}
