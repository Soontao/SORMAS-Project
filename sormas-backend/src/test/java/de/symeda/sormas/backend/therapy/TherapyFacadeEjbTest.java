package de.symeda.sormas.backend.therapy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.therapy.PrescriptionDto;
import de.symeda.sormas.api.therapy.PrescriptionIndexDto;
import de.symeda.sormas.api.therapy.TreatmentDto;
import de.symeda.sormas.api.therapy.TreatmentIndexDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator.RDCF;

public class TherapyFacadeEjbTest extends AbstractBeanTest {

	@Test
	public void testPrescriptionDeletion() {
		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, UserRole.SURVEILLANCE_SUPERVISOR, UserRole.CASE_SUPERVISOR);
		UserDto admin = creator.createUser(rdcf, UserRole.ADMIN);
		PersonDto casePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), casePerson.toReference(), rdcf);
		PrescriptionDto prescription = creator.createPrescription(caze);

		// Database should contain the created prescription
		assertNotNull(getTherapyFacade().getPrescriptionByUuid(prescription.getUuid()));

		getTherapyFacade().deletePrescription(prescription.getUuid(), admin.getUuid());

		// Database should not contain the deleted visit
		assertNull(getTherapyFacade().getPrescriptionByUuid(prescription.getUuid()));
	}

	@Test
	public void testTreatmentDeletion() {
		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, UserRole.SURVEILLANCE_SUPERVISOR, UserRole.CASE_SUPERVISOR);
		UserDto admin = creator.createUser(rdcf, UserRole.ADMIN);
		PersonDto casePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), casePerson.toReference(), rdcf);
		TreatmentDto treatment = creator.createTreatment(caze);

		// Database should contain the created prescription
		assertNotNull(getTherapyFacade().getTreatmentByUuid(treatment.getUuid()));

		getTherapyFacade().deleteTreatment(treatment.getUuid(), admin.getUuid());

		// Database should not contain the deleted visit
		assertNull(getTherapyFacade().getTreatmentByUuid(treatment.getUuid()));
	}
	
	@Test
	public void testPrescriptionIndexListGeneration() {
		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, UserRole.SURVEILLANCE_SUPERVISOR, UserRole.CASE_SUPERVISOR);
		PersonDto casePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), casePerson.toReference(), rdcf);
		creator.createPrescription(caze);
		
		List<PrescriptionIndexDto> results = getTherapyFacade().getPrescriptionIndexList(null);
		
		assertEquals(1, results.size());
	}

	@Test
	public void testTreatmentIndexListGeneration() {
		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, UserRole.SURVEILLANCE_SUPERVISOR, UserRole.CASE_SUPERVISOR);
		PersonDto casePerson = creator.createPerson("Case", "Person");
		CaseDataDto caze = creator.createCase(user.toReference(), casePerson.toReference(), rdcf);
		creator.createTreatment(caze);
		
		List<TreatmentIndexDto> results = getTherapyFacade().getTreatmentIndexList(null);
		
		assertEquals(1, results.size());
	}	

}
