package servlet.implementation;

public enum AdminPacket {
	__NULL__,
	_TYPE,
	_DATA,
	__RESERVED0__,
	__RESERVED1__,
	__RESERVED2__,
	__RESERVED3__,
	__RESERVED4__,
	_ADMIN;
	
	public enum Admin {
		__NULL__,
		YES,
		NO
	}
	
	public static enum AdminTypes {
		__NULL__,
		GET_USER,
		GET_CLINICS,
		ADD_USER,
		ADD_CLINIC,
		RSP_REGISTR;
	}

	public enum AdminData {
		__NULL__;
		
		public static enum AdminGetUser {
			__NULL__,
			USER,
			USERNAME;
			public static enum User {
				CLINIC_ID,
				USERNAME,
				PASSWORD,
				EMAIL,
				SALT,
				UPDATE_PASSWORD;
				public static enum UpdatePassword {
					YES,
					NO
				}
			}
		} // _GetUser
		
		public static enum AdminGetClinics {
			__NULL__,
			CLINICS
		} // _GetClinics
		
		public static enum AdminAddUser {
			__NULL__,
			RESPONSE,
			DETAILS;
			
			public static enum Response {
				__NULL__,
				FAIL,
				SUCCESS
			}
			
			public static enum Details {
				__NULL__,
				CLINIC_ID,
				NAME,
				PASSWORD,
				EMAIL,
				SALT
			}
		} // _AddUser
		
		public static enum AdminAddClinic {
			__NULL__,
			RESPONSE,
			NAME;
			
			public static enum Response {
				__NULL__,
				FAIL,
				SUCCESS
			}
		} // _AddClinic
		
		public static enum AdminRespondRegistration {
			__NULL__,
			RESPONSE,
			DETAILS;
			
			public static enum Response {
				__NULL__,
				FAIL,
				SUCCESS
			}
			
			public static enum Details {
				__NULL__,
				USERNAME,
				PASSWORD,
				EMAIL
			}
		} // _RespondRegistration
	} // _Packets
}