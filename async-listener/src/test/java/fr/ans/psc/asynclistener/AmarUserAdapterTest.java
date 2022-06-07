package fr.ans.psc.asynclistener;

import com.google.gson.Gson;
import fr.ans.psc.amar.model.User;
import fr.ans.psc.asynclistener.model.AmarUserAdapter;
import fr.ans.psc.model.Ps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Slf4j
public class AmarUserAdapterTest {

    @Test
    void amarUserAdapterTest() {

        Gson json = new Gson();
        Ps ps = json.fromJson("{\"idType\":\"8\",\"id\":\"00000000001\"," +
                "\"nationalId\":\"800000000001\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY'MIKE'ERICK-RIEGEL\",\"dateOfBirth\":\"17/12/1983\"," +
                "\"birthAddressCode\":\"57463\",\"birthCountryCode\":\"99000\",\"birthAddress\":\"METZ\",\"genderCode\":\"M\"," +
                "\"phone\":\"0601020304\",\"email\":\"toto57@hotmail.fr\",\"salutationCode\":\"MME\",\"professions\":[{\"exProId\":\"50C\"," +
                "\"code\":\"50\",\"categoryCode\":\"C\",\"salutationCode\":\"M\",\"lastName\":\"DUPONT\",\"firstName\":\"JIMMY\"," +
                "\"expertises\":[{\"expertiseId\":\"SSM69\",\"typeCode\":\"S\",\"code\":\"SM69\"}],\"workSituations\":[{\"situId\":\"SSA04\"," +
                "\"modeCode\":\"S\",\"activitySectorCode\":\"SA04\",\"pharmacistTableSectionCode\":\"AC36\",\"roleCode\":\"12\"," +
                "\"registrationAuthority\":\"ARS/ARS/ARS\", \"structure\":{\"siteSIRET\":\"125 137 196 15574\",\"siteSIREN\":\"125 137 196\"," +
                "\"siteFINESS\":null,\"legalEstablishmentFINESS\":null,\"structureTechnicalId\":\"1\"," +
                "\"legalCommercialName\":\"Structure One\",\"publicCommercialName\":\"Structure One\",\"recipientAdditionalInfo\":\"info +\"," +
                "\"geoLocationAdditionalInfo\":\"geoloc info +\",\"streetNumber\":\"1\",\"streetNumberRepetitionIndex\":\"bis\"," +
                "\"streetCategoryCode\":\"rue\",\"streetLabel\":\"Zorro\",\"distributionMention\":\"c/o Bernardo\",\"cedexOffice\":\"75117\"," +
                "\"postalCode\":\"75017\",\"communeCode\":\"75\",\"countryCode\":\"FR\",\"phone\":\"0123456789\",\"phone2\":\"0623456789\"," +
                "\"fax\":\"0198765432\",\"email\":\"structure@one.fr\",\"departmentCode\":\"99\",\"oldStructureId\":\"101\"," +
                "\"registrationAuthority\":\"CIA\"}}]}],\"otherIds\":[\"800000000001\", \"ALT-ID\"]}", Ps.class);

        log.info(json.toJson(ps, Ps.class));
        AmarUserAdapter amarUserAdapter = new AmarUserAdapter(ps);
        log.info(json.toJson(amarUserAdapter, User.class));

        assertEquals(ps.getNationalId(), amarUserAdapter.getNationalId());

        assertEquals(ps.getProfessions().get(0).getCategoryCode(),
                amarUserAdapter.getPractices().get(0).getProfessionalCategoryCode());

        assertEquals(ps.getProfessions().get(0).getExpertises().get(0).getTypeCode(),
                amarUserAdapter.getPractices().get(0).getExpertiseTypeCode());

        assertEquals(ps.getProfessions().get(0).getWorkSituations().get(0).getRegistrationAuthority(),
                amarUserAdapter.getPractices().get(0).getActivities().get(0).getCompanyRegistrationAuthority());

        assertEquals(ps.getProfessions().get(0).getWorkSituations().get(0).getStructure().getStructureTechnicalId(),
                amarUserAdapter.getPractices().get(0).getActivities().get(0).getCompanyTechnicalIdentifier());
    }
}
