package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.Activity;
import fr.ans.psc.model.Structure;
import fr.ans.psc.model.WorkSituation;

public class AmarActivityAdapter extends Activity {

    public AmarActivityAdapter(WorkSituation workSituation) {
        setProfessionalModeCode(workSituation.getModeCode());
        setActivitySectorCode(workSituation.getActivitySectorCode());
        setPharmacistTableSectionCode(workSituation.getPharmacistTableSectionCode());
        setRoleCode(workSituation.getRoleCode());
        setCompanyRegistrationAuthority(workSituation.getRegistrationAuthority());
        setActivityTypeCode(workSituation.getActivityKindCode());

        Structure struct = workSituation.getStructure();
        if (struct != null) {
            setSiretSiteNumber(struct.getSiteSIRET());
            setSirenSiteNumber(struct.getSiteSIREN());
            setFinessSiteNumber(struct.getSiteFINESS());
            setFinessLegalCompanyNumber(struct.getLegalEstablishmentFINESS());
            setCompanyTechnicalIdentifier(struct.getStructureTechnicalId());
            setCompanyName(struct.getLegalCommercialName());
            setCompanyCommercialSign(struct.getPublicCommercialName());
            setCompanyAdditionalAddress(struct.getRecipientAdditionalInfo());
            setCompanyGeographicalPointComplement(struct.getGeoLocationAdditionalInfo());
            setCompanyWayNumber(struct.getStreetNumber());
            setCompanyRepeatIndex(struct.getStreetNumberRepetitionIndex());
            setCompanyWayType(struct.getStreetCategoryCode());
            setCompanyWayLabel(struct.getStreetLabel());
            setCompanyDistributionMention(struct.getDistributionMention());
            setCompanyCedexOffice(struct.getCedexOffice());
            setCompanyPostalCode(struct.getPostalCode());
            setCompanyTownCode(struct.getCommuneCode());
            setCompanyCountryCode(struct.getCountryCode());
            setCompanyPhone1(struct.getPhone());
            setCompanyPhone2(struct.getPhone2());
            setCompanyFax(struct.getFax());
            setCompanyEmail(struct.getEmail());
            setCompanyCountyCode(struct.getDepartmentCode());
            setCompanyOldIdentifier(struct.getOldStructureId());
        }

    }
}
