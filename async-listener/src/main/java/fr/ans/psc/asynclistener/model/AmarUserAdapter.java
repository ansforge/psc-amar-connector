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
        ps.getOtherIds().forEach(otherId ->
        {
            AlternativeIdentifier identifier = new AlternativeIdentifier();
            alternativeIdentifiers.add(identifier.identifier(otherId));
        });

        setNationalId(ps.getNationalId());
        setContactInfo(contactInfo);
        setCivilStatus(civilStatus);
        setPractices(practices);
        setAlternativeIdentifiers(alternativeIdentifiers);
    }
}
