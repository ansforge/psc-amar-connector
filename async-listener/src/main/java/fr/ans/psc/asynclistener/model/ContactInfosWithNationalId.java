package fr.ans.psc.asynclistener.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ContactInfosWithNationalId {
	
	private String nationalId;

	private String phone;
	
	private String email;
}
