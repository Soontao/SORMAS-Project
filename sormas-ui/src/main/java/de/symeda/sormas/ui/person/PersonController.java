/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.person;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonFacade;
import de.symeda.sormas.api.person.PersonIndexDto;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.CommitListener;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.ViewMode;

public class PersonController {

	private PersonFacade personFacade = FacadeProvider.getPersonFacade();

	public PersonController() {

	}

	public void create(Consumer<PersonReferenceDto> doneConsumer) {
		create("", "", doneConsumer);
	}

	public void create(String firstName, String lastName, Consumer<PersonReferenceDto> doneConsumer) {
		PersonDto person = PersonDto.build();
		person.setFirstName(firstName);
		person.setLastName(lastName);

		person = personFacade.savePerson(person);
		doneConsumer.accept(person.toReference()); 
	}

	public void selectOrCreatePerson(String firstName, String lastName, Consumer<PersonReferenceDto> resultConsumer) {
		PersonSelectField personSelect = new PersonSelectField();
		personSelect.setFirstName(firstName);
		personSelect.setLastName(lastName);
		personSelect.setWidth(1024, Unit.PIXELS);

		if (personSelect.hasMatches()) {
			personSelect.selectBestMatch();
			// TODO add user right parameter
			final CommitDiscardWrapperComponent<PersonSelectField> selectOrCreateComponent = 
					new CommitDiscardWrapperComponent<PersonSelectField>(personSelect);

			ValueChangeListener nameChangeListener = e -> {
				selectOrCreateComponent.getCommitButton().setEnabled(!(personSelect.getFirstName() == null || personSelect.getFirstName().isEmpty()
						|| personSelect.getLastName() == null || personSelect.getLastName().isEmpty()));

			};
			personSelect.getFirstNameField().addValueChangeListener(nameChangeListener);
			personSelect.getLastNameField().addValueChangeListener(nameChangeListener);

			selectOrCreateComponent.addCommitListener(new CommitListener() {
				@Override
				public void onCommit() {
					PersonIndexDto person = personSelect.getValue();
					if (person != null) {
						if (resultConsumer != null) {
							resultConsumer.accept(person.toReference());
						}
					} else {	
						create(personSelect.getFirstName(), personSelect.getLastName(), resultConsumer);
					}
				}
			});

			personSelect.setSelectionChangeCallback((commitAllowed) -> {
				selectOrCreateComponent.getCommitButton().setEnabled(commitAllowed);
			});

			VaadinUiUtil.showModalPopupWindow(selectOrCreateComponent, I18nProperties.getString(Strings.headingPickOrCreatePerson));
		} else {
			create(personSelect.getFirstName(), personSelect.getLastName(), resultConsumer);
		}
	}

	public CommitDiscardWrapperComponent<PersonCreateForm> getPersonCreateComponent(PersonDto person, UserRight editOrCreateUserRight) {
		PersonCreateForm createForm = new PersonCreateForm(editOrCreateUserRight);
		createForm.setValue(person);
		final CommitDiscardWrapperComponent<PersonCreateForm> editComponent = new CommitDiscardWrapperComponent<PersonCreateForm>(createForm, createForm.getFieldGroup());

		editComponent.addCommitListener(new CommitListener() {
			@Override
			public void onCommit() {
				if (!createForm.getFieldGroup().isModified()) {
					PersonDto dto = createForm.getValue();
					personFacade.savePerson(dto);
				}
			}
		});

		return editComponent;
	}  


	public CommitDiscardWrapperComponent<PersonEditForm> getPersonEditComponent(String personUuid, Disease disease, String diseaseDetails, UserRight editOrCreateUserRight, final ViewMode viewMode) {
		PersonEditForm editForm = new PersonEditForm(disease, diseaseDetails, editOrCreateUserRight, viewMode);

		PersonDto personDto = personFacade.getPersonByUuid(personUuid);
		editForm.setValue(personDto);
		
		final CommitDiscardWrapperComponent<PersonEditForm> editView = new CommitDiscardWrapperComponent<PersonEditForm>(editForm, editForm.getFieldGroup());
		
		editView.addCommitListener(new CommitListener() {
			@Override
			public void onCommit() {
				if (!editForm.getFieldGroup().isModified()) {
					PersonDto dto = editForm.getValue();
					savePerson(dto);
				}
			}
		});

		return editView;
	}

	private void savePerson(PersonDto personDto) {

		PersonDto existingPerson = FacadeProvider.getPersonFacade().getPersonByUuid(personDto.getUuid());
		List<CaseDataDto> personCases = FacadeProvider.getCaseFacade().getAllCasesOfPerson(personDto.getUuid(), UserProvider.getCurrent().getUserReference().getUuid());

		onPersonChanged(existingPerson, personDto);
		
		personFacade.savePerson(personDto);
		
		// Check whether the classification of any of this person's cases has changed
		CaseClassification newClassification = null;
		for (CaseDataDto personCase : personCases) {
			CaseDataDto updatedPersonCase = FacadeProvider.getCaseFacade().getCaseDataByUuid(personCase.getUuid());
			if (personCase.getCaseClassification() != updatedPersonCase.getCaseClassification() && updatedPersonCase.getClassificationUser() == null) {
				newClassification = updatedPersonCase.getCaseClassification();
				break;
			}
		}
		
		if (newClassification != null) {
			Notification notification = new Notification(String.format(I18nProperties.getString(Strings.messagePersonSavedClassificationChanged), newClassification.toString()), Type.WARNING_MESSAGE);
			notification.setDelayMsec(-1);
			notification.show(Page.getCurrent());
		} else {
			Notification.show(I18nProperties.getString(Strings.messagePersonSaved), Type.WARNING_MESSAGE);
		}
		
		SormasUI.refreshView();
	}

	private void onPersonChanged(PersonDto existingPerson, PersonDto changedPerson) {
		// approximate age reference date
		if (existingPerson == null
				|| !DataHelper.equal(changedPerson.getApproximateAge(), existingPerson.getApproximateAge())
				|| !DataHelper.equal(changedPerson.getApproximateAgeType(), existingPerson.getApproximateAgeType())) {
			if (changedPerson.getApproximateAge() == null) {
				changedPerson.setApproximateAgeReferenceDate(null);
			} else {
				changedPerson.setApproximateAgeReferenceDate(changedPerson.getDeathDate() != null ? changedPerson.getDeathDate() : new Date());
			}
		}
	}
}
