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
import fr.ans.psc.amar.model.Practice;
import fr.ans.psc.model.Expertise;
import fr.ans.psc.model.Profession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AmarPracticeAdapter extends Practice {

    public AmarPracticeAdapter(Profession profession) {
        setProfessionCode(AttributeEncoding.encodeStringAttribute(profession.getCode()));
        setProfessionalCategoryCode(AttributeEncoding.encodeStringAttribute(profession.getCategoryCode()));
        setProfessionalLastName(AttributeEncoding.encodeStringAttribute(profession.getLastName()));
        setProfessionalFirstName(AttributeEncoding.encodeStringAttribute(profession.getFirstName()));
        setProfessionalCivilityTitle(AttributeEncoding.encodeStringAttribute(profession.getSalutationCode()));

        Expertise mainExpertise = extractExpertise(profession);
        setExpertiseCode(mainExpertise != null ? mainExpertise.getCode() : null);
        setExpertiseTypeCode(mainExpertise != null ? mainExpertise.getTypeCode() : null);

        List<Activity> activities = new ArrayList<>();
        profession.getWorkSituations().forEach(workSituation -> activities.add(new AmarActivityAdapter(workSituation)));
        setActivities(activities);
    }

    private Expertise extractExpertise(Profession profession) {
        String[] acceptedExpertises = {"S", "CEX", "PAC"};
        List<Expertise> expertises = profession.getExpertises().stream()
                .filter(expertise -> Arrays.stream(acceptedExpertises)
                        .anyMatch(s -> expertise.getTypeCode().equals(s)))
                .collect(Collectors.toList());

        return expertises.isEmpty() ? null : expertises.get(0);
    }
}
