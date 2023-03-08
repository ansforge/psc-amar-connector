package fr.ans.psc.asynclistener.model;

import fr.ans.psc.amar.model.*;
import fr.ans.psc.model.Ps;

import java.util.ArrayList;
import java.util.List;

public class AmarUserAdapter extends User {

    public AmarUserAdapter(Ps ps) {
        ContactInfo contactInfo = new AmarContactInfoAdapter(ps);
        CivilStatus civilStatus = new AmarCivilStatusAdapter(ps);
        List<Practice> practices = new ArrayList<>();
        ps.getProfessions().forEach(profession -> practices.add(new AmarPracticeAdapter(profession)));
        List<AlternativeIdentifier> alternativeIdentifiers = new ArrayList<>();
        ps.getIds().forEach(id ->
        {
            AlternativeIdentifier identifier = new AlternativeIdentifier();
            identifier.setIdentifier(id);
            identifier.setOrigine(id.substring(0,1));
            identifier.setQuality(1);
            alternativeIdentifiers.add(identifier);
        });

        setNationalId(ps.getNationalId());
        setContactInfo(contactInfo);
        setCivilStatus(civilStatus);
        setPractices(practices);
        setAlternativeIdentifiers(alternativeIdentifiers);
    }
}
