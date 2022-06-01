package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.*;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.List;

public class AmarUserAdapter extends fr.ans.psc.amar.model.User {

    public AmarUserAdapter(Ps ps) {
        ContactInfo contactInfo = new AmarContactInfoAdapter(ps);
        CivilStatus civilStatus = new AmarCivilStatusAdapter(ps);
        List<Practice> practices = new ArrayList<>();
        ps.getProfessions().forEach(profession -> practices.add(new AmarPracticeAdapter(profession)));
        List<AlternativeIdentifier> alternativeIdentifiers = new ArrayList<>();
        //TODO get otherIds

        setNationalId(ps.getNationalId());
        setContactInfo(contactInfo);
        setCivilStatus(civilStatus);
        setPractices(practices);
        setAlternativeIdentifiers(alternativeIdentifiers);
    }
}
