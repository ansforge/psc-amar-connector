package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.Activity;
import fr.ans.psc.amar.model.Practice;
import fr.ans.psc.model.Profession;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.List;

public class AmarPracticeAdapter extends Practice {

    public AmarPracticeAdapter(Profession profession) {
        setProfessionCode(profession.getCode());
        setProfessionalCategoryCode(profession.getCategoryCode());
        setProfessionalLastName(profession.getLastName());
        setProfessionalFirstName(profession.getFirstName());
        setProfessionalCivilityTitle(profession.getSalutationCode());
        setExpertiseCode(profession.getExpertises().get(0).getCode());
        setExpertiseTypeCode(profession.getExpertises().get(0).getTypeCode());
        List<Activity> activities = new ArrayList<>();
        profession.getWorkSituations().forEach(workSituation -> activities.add(new AmarActivityAdapter(workSituation)));
        setActivities(activities);
    }
}
