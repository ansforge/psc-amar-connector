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
        setProfessionCode(profession.getCode());
        setProfessionalCategoryCode(profession.getCategoryCode());
        setProfessionalLastName(profession.getLastName());
        setProfessionalFirstName(profession.getFirstName());
        setProfessionalCivilityTitle(profession.getSalutationCode());

        Expertise mainExpertise = extractExpertise(profession);
        setExpertiseCode(mainExpertise != null ? mainExpertise.getCode() : "");
        setExpertiseTypeCode(mainExpertise != null ? mainExpertise.getTypeCode() : "");

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
