package de.symeda.sormas.ui.caze;

import java.time.ZoneId;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.converter.LocalDateToDateConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseCriteria;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.NewCaseDateType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.utils.CssStyles;

@SuppressWarnings("serial")
public class MergeCasesFilterComponent extends VerticalLayout {

	// Layouts
	private HorizontalLayout firstRowLayout;
	private HorizontalLayout secondRowLayout;

	private DateField dfCreationDateFrom;
	private DateField dfCreationDateTo;
	private ComboBox<Disease> cbDisease;
	private TextField tfSearch;
	private TextField tfReportingUser;
	private ComboBox<RegionReferenceDto> cbRegion;
	private ComboBox<DistrictReferenceDto> cbDistrict;
	private ComboBox<NewCaseDateType> cbNewCaseDateType;
	private DateField dfNewCaseDateFrom;
	private DateField dfNewCaseDateTo;
	private Button btnConfirmFilters;
	private Button btnResetFilters;

	private Binder<CaseCriteria> binder = new Binder<>(CaseCriteria.class);
	private CaseCriteria criteria;
	private Runnable filtersUpdatedCallback;

	public MergeCasesFilterComponent(CaseCriteria criteria, Runnable filtersUpdatedCallback) {
		setSpacing(false);
		setMargin(false);
		setWidth(100, Unit.PERCENTAGE);

		this.criteria = criteria;
		this.filtersUpdatedCallback = filtersUpdatedCallback;

		addFirstRowLayout();
		addSecondRowLayout();

		binder.readBean(this.criteria);
	}

	private void addFirstRowLayout() {
		firstRowLayout = new HorizontalLayout();
		firstRowLayout.setMargin(false);

		dfCreationDateFrom = new DateField();
		dfCreationDateFrom.setId(CaseCriteria.CREATION_DATE_FROM);
		dfCreationDateFrom.setWidth(200, Unit.PIXELS);
		dfCreationDateFrom.setPlaceholder(I18nProperties.getString(Strings.promptCreationDateFrom));
		dfCreationDateFrom.setCaption(I18nProperties.getCaption(Captions.creationDate));
		binder.forField(dfCreationDateFrom)
		.withConverter(new LocalDateToDateConverter(ZoneId.systemDefault()))
		.bind(CaseCriteria.CREATION_DATE_FROM);
		firstRowLayout.addComponent(dfCreationDateFrom);

		dfCreationDateTo = new DateField();
		dfCreationDateTo.setId(CaseCriteria.CREATION_DATE_TO);
		dfCreationDateTo.setWidth(200, Unit.PIXELS);
		CssStyles.style(dfCreationDateTo, CssStyles.FORCE_CAPTION);
		dfCreationDateTo.setPlaceholder(I18nProperties.getString(Strings.promptDateTo));
		binder.forField(dfCreationDateTo)
		.withConverter(new LocalDateToDateConverter(ZoneId.systemDefault()))
		.bind(CaseCriteria.CREATION_DATE_TO);
		firstRowLayout.addComponent(dfCreationDateTo);

		cbDisease = new ComboBox<>();
		cbDisease.setId(CaseCriteria.DISEASE);
		cbDisease.setWidth(200, Unit.PIXELS);
		CssStyles.style(cbDisease, CssStyles.FORCE_CAPTION);
		cbDisease.setPlaceholder(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.DISEASE));
		cbDisease.setItems(FacadeProvider.getDiseaseConfigurationFacade().getAllActivePrimaryDiseases());
		binder.bind(cbDisease, CaseCriteria.DISEASE);
		firstRowLayout.addComponent(cbDisease);

		tfSearch = new TextField();
		tfSearch.setId(CaseCriteria.NAME_UUID_EPID_NUMBER_LIKE);
		tfSearch.setWidth(200, Unit.PIXELS);
		CssStyles.style(tfSearch, CssStyles.FORCE_CAPTION);
		tfSearch.setPlaceholder(I18nProperties.getString(Strings.promptCasesSearchField));
		binder.bind(tfSearch, CaseCriteria.NAME_UUID_EPID_NUMBER_LIKE);
		firstRowLayout.addComponent(tfSearch);

		tfReportingUser = new TextField();
		tfReportingUser.setId(CaseCriteria.REPORTING_USER_LIKE);
		tfReportingUser.setWidth(200, Unit.PIXELS);
		CssStyles.style(tfReportingUser, CssStyles.FORCE_CAPTION);
		tfReportingUser.setPlaceholder(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.REPORTING_USER));
		binder.bind(tfReportingUser, CaseCriteria.REPORTING_USER_LIKE);
		firstRowLayout.addComponent(tfReportingUser);

		addComponent(firstRowLayout);
	}

	private void addSecondRowLayout() {
		secondRowLayout = new HorizontalLayout();
		secondRowLayout.setMargin(false);

		cbRegion = new ComboBox<>();
		cbDistrict = new ComboBox<>();

		cbRegion.setId(CaseCriteria.REGION);
		cbRegion.setWidth(200, Unit.PIXELS);
		CssStyles.style(cbRegion, CssStyles.FORCE_CAPTION);
		cbRegion.setPlaceholder(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.REGION));
		cbRegion.setItems(FacadeProvider.getRegionFacade().getAllAsReference());
		binder.bind(cbRegion, CaseCriteria.REGION);
		cbRegion.addValueChangeListener(e -> {
			RegionReferenceDto region = e.getValue();
			cbDistrict.clear();
			if (region != null) {
				cbDistrict.setItems(FacadeProvider.getDistrictFacade().getAllByRegion(region.getUuid()));
				cbDistrict.setEnabled(true);
			} else {
				cbDistrict.setEnabled(false);
			}
		});
		secondRowLayout.addComponent(cbRegion);
		if (UserProvider.getCurrent().getUser().getRegion() != null) {
			cbRegion.setEnabled(false);
		}

		cbDistrict.setId(CaseCriteria.DISTRICT);
		cbDistrict.setWidth(200, Unit.PIXELS);
		CssStyles.style(cbDistrict, CssStyles.FORCE_CAPTION);
		cbDistrict.setPlaceholder(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.DISTRICT));
		binder.bind(cbDistrict, CaseCriteria.DISTRICT);
		secondRowLayout.addComponent(cbDistrict);

		cbNewCaseDateType = new ComboBox<>();
		dfNewCaseDateFrom = new DateField();
		dfNewCaseDateTo = new DateField();
		
		cbNewCaseDateType.setId(CaseCriteria.NEW_CASE_DATE_TYPE);
		cbNewCaseDateType.setWidth(200, Unit.PIXELS);
		cbNewCaseDateType.setPlaceholder(I18nProperties.getString(Strings.promptNewCaseDateType));
		cbNewCaseDateType.setCaption(I18nProperties.getCaption(Captions.caseNewCaseDate));
		cbNewCaseDateType.setItems(NewCaseDateType.values());
		binder.bind(cbNewCaseDateType, CaseCriteria.NEW_CASE_DATE_TYPE);
		cbNewCaseDateType.addValueChangeListener(event -> {
			dfNewCaseDateFrom.setEnabled(event.getValue() != null);
			dfNewCaseDateTo.setEnabled(event.getValue() != null);
		});
		secondRowLayout.addComponent(cbNewCaseDateType);
		
		dfNewCaseDateFrom.setId(CaseCriteria.NEW_CASE_DATE_FROM);
		dfNewCaseDateFrom.setWidth(200, Unit.PIXELS);
		CssStyles.style(dfNewCaseDateFrom, CssStyles.FORCE_CAPTION);
		dfNewCaseDateFrom.setPlaceholder(I18nProperties.getString(Strings.promptCasesDateFrom));
		binder.forField(dfNewCaseDateFrom)
		.withConverter(new LocalDateToDateConverter(ZoneId.systemDefault()))
		.bind(CaseCriteria.NEW_CASE_DATE_FROM);
		dfNewCaseDateFrom.setEnabled(false);
		secondRowLayout.addComponent(dfNewCaseDateFrom);

		dfNewCaseDateTo.setId(CaseCriteria.NEW_CASE_DATE_TO);
		dfNewCaseDateTo.setWidth(200, Unit.PIXELS);
		CssStyles.style(dfNewCaseDateTo, CssStyles.FORCE_CAPTION);
		dfNewCaseDateTo.setPlaceholder(I18nProperties.getString(Strings.promptDateTo));
		binder.forField(dfNewCaseDateTo)
		.withConverter(new LocalDateToDateConverter(ZoneId.systemDefault()))
		.bind(CaseCriteria.NEW_CASE_DATE_TO);
		dfNewCaseDateTo.setEnabled(false);
		secondRowLayout.addComponent(dfNewCaseDateTo);
		
		btnConfirmFilters = new Button(I18nProperties.getCaption(Captions.actionConfirmFilters));
		btnConfirmFilters.setId("confirm");
		CssStyles.style(btnConfirmFilters, CssStyles.FORCE_CAPTION, ValoTheme.BUTTON_PRIMARY);
		btnConfirmFilters.addClickListener(event -> {
			try {
				binder.writeBean(criteria);
				filtersUpdatedCallback.run();
			} catch (ValidationException e) {
				// No validation needed
			}
		});
		secondRowLayout.addComponent(btnConfirmFilters);

		btnResetFilters = new Button(I18nProperties.getCaption(Captions.actionResetFilters));
		btnResetFilters.setId("reset");
		CssStyles.style(btnResetFilters, CssStyles.FORCE_CAPTION);
		btnResetFilters.addClickListener(event -> {
			ViewModelProviders.of(MergeCasesView.class).remove(CaseCriteria.class);
			filtersUpdatedCallback.run();
		});
		secondRowLayout.addComponent(btnResetFilters);

		addComponent(secondRowLayout);
	}

}
