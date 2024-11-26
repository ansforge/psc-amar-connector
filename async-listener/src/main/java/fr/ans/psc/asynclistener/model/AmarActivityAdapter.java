/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.Activity;
import fr.ans.psc.model.Structure;
import fr.ans.psc.model.WorkSituation;

public class AmarActivityAdapter extends Activity {

    public AmarActivityAdapter(WorkSituation workSituation) {
        setProfessionalModeCode(AttributeEncoding.encodeStringAttribute(workSituation.getModeCode()));
        setActivitySectorCode(AttributeEncoding.encodeStringAttribute(workSituation.getActivitySectorCode()));
        setPharmacistTableSectionCode(AttributeEncoding.encodeStringAttribute(workSituation.getPharmacistTableSectionCode()));
        setRoleCode(AttributeEncoding.encodeStringAttribute(workSituation.getRoleCode()));
        setCompanyRegistrationAuthority(AttributeEncoding.encodeStringAttribute(workSituation.getRegistrationAuthority()));
        setActivityTypeCode(AttributeEncoding.encodeStringAttribute(workSituation.getActivityKindCode()));

        Structure struct = workSituation.getStructure();
        if (struct != null) {
            setSiretSiteNumber(AttributeEncoding.encodeStringAttribute(struct.getSiteSIRET()));
            setSirenSiteNumber(AttributeEncoding.encodeStringAttribute(struct.getSiteSIREN()));
            setFinessSiteNumber(AttributeEncoding.encodeStringAttribute(struct.getSiteFINESS()));
            setFinessLegalCompanyNumber(AttributeEncoding.encodeStringAttribute(struct.getLegalEstablishmentFINESS()));
            setCompanyTechnicalIdentifier(AttributeEncoding.encodeStringAttribute(struct.getStructureTechnicalId()));
            setCompanyName(AttributeEncoding.encodeStringAttribute(struct.getLegalCommercialName()));
            setCompanyCommercialSign(AttributeEncoding.encodeStringAttribute(struct.getPublicCommercialName()));
            setCompanyAdditionalAddress(AttributeEncoding.encodeStringAttribute(struct.getRecipientAdditionalInfo()));
            setCompanyGeographicalPointComplement(AttributeEncoding.encodeStringAttribute(struct.getGeoLocationAdditionalInfo()));
            setCompanyWayNumber(AttributeEncoding.encodeStringAttribute(struct.getStreetNumber()));
            setCompanyRepeatIndex(AttributeEncoding.encodeStringAttribute(struct.getStreetNumberRepetitionIndex()));
            setCompanyWayType(AttributeEncoding.encodeStringAttribute(struct.getStreetCategoryCode()));
            setCompanyWayLabel(AttributeEncoding.encodeStringAttribute(struct.getStreetLabel()));
            setCompanyDistributionMention(AttributeEncoding.encodeStringAttribute(struct.getDistributionMention()));
            setCompanyCedexOffice(AttributeEncoding.encodeStringAttribute(struct.getCedexOffice()));
            setCompanyPostalCode(AttributeEncoding.encodeStringAttribute(struct.getPostalCode()));
            setCompanyTownCode(AttributeEncoding.encodeStringAttribute(struct.getCommuneCode()));
            setCompanyCountryCode(AttributeEncoding.encodeStringAttribute(struct.getCountryCode()));
            setCompanyPhone1(AttributeEncoding.encodeStringAttribute(struct.getPhone()));
            setCompanyPhone2(AttributeEncoding.encodeStringAttribute(struct.getPhone2()));
            setCompanyFax(AttributeEncoding.encodeStringAttribute(struct.getFax()));
            setCompanyEmail(AttributeEncoding.encodeStringAttribute(struct.getEmail()));
            setCompanyCountyCode(AttributeEncoding.encodeStringAttribute(struct.getDepartmentCode()));
            setCompanyOldIdentifier(AttributeEncoding.encodeStringAttribute(struct.getOldStructureId()));
        }

    }
}
