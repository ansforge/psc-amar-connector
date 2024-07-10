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
