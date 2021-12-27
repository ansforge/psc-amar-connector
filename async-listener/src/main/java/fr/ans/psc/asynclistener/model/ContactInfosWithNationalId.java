/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.asynclistener.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ContactInfosWithNationalId.
 */
@Getter
@Setter
@ToString
public class ContactInfosWithNationalId {
	
	private String nationalId;

	private String phone;
	
	private String email;
}
